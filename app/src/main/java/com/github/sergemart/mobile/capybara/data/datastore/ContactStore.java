package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exception.ContactsException;
import com.github.sergemart.mobile.capybara.data.model.ContactData;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import androidx.core.content.ContextCompat;
import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


// Singleton
public class ContactStore {

    private static final String TAG = ContactStore.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static ContactStore sInstance;


    // Private constructor
    private ContactStore() {

        // Init member variables
        mContext = App.getContext();
        mContentResolver = mContext.getContentResolver();
        mBitmapCache = new ConcurrentHashMap<>();
        mIdsByEmail = new ConcurrentHashMap<>();
    }


    // Factory method
    public static ContactStore get() {
        if(sInstance == null) sInstance = new ContactStore();
        return sInstance;
    }


    // --------------------------- Member variables

    private Context mContext;
    private ContentResolver mContentResolver;
    private final List<ContactData> mContacts = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Bitmap> mBitmapCache;
    private Map<String, String> mIdsByEmail;

    private ConnectableObservable<GenericEvent> mContactsObservable;


    // --------------------------- The interface

    /**
     * @return A connectable observable emitting a contact list
     */
    public ConnectableObservable<GenericEvent> getContactsAsync() {
        if (mContactsObservable != null) return mContactsObservable;

        Observable<GenericEvent> observable = Observable.create(emitter -> {
            try {
                this.loadContacts();
                if (BuildConfig.DEBUG) Log.d(TAG, "Contacts loaded, emitting them");
                emitter.onNext(GenericEvent.of(SUCCESS).setData(mContacts));
            } catch (SecurityException e) {
                String errorMessage = mContext.getString(R.string.exception_contacts_no_permission);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                emitter.onNext(GenericEvent.of(FAILURE).setException(new ContactsException(errorMessage, e)));
            } catch (Exception e) {
                emitter.onNext(GenericEvent.of(FAILURE).setException(new ContactsException(e)));
            }
        });
        mContactsObservable = observable
            .subscribeOn(Schedulers.io())                                                           // takes effect only before turning into connectable
            .replay()                                                                               // last operator in the chain: here occurs the multicasting
        ;
        return mContactsObservable;
    }


    /**
     * @return A cold observable emitting a contact data structure used as a photo container
     */
    public Observable<GenericEvent> getEnrichedContactAsync(String contactEmail) {
        return Observable.create(emitter -> {
            ContactData auxContactData = new ContactData();
            String contactId = mIdsByEmail.get(contactEmail);
            auxContactData.setId(Objects.requireNonNull(contactId));
            auxContactData.setEmail(contactEmail);

            if (mBitmapCache.containsKey(contactId) && mBitmapCache.get(contactId) != null) {
                auxContactData.setPhoto(mBitmapCache.get(contactId));
                emitter.onNext(GenericEvent.of(SUCCESS).setData(auxContactData));                   // emit a cached bitmap
            } else {
                try {
                    Bitmap contactPhoto = this.getContactPhoto(contactId);                          // data provider op
                    mBitmapCache.put(contactId, contactPhoto);                                      // cache the bitmap
                    auxContactData.setPhoto(contactPhoto);
                    emitter.onNext(GenericEvent.of(SUCCESS).setData(auxContactData));               // emit a fetched bitmap
                } catch (SecurityException e) {
                    String errorMessage = mContext.getString(R.string.exception_contacts_no_permission);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onNext(GenericEvent.of(FAILURE).setException(new ContactsException(errorMessage, e)));
                } catch (Exception e) {
                    emitter.onNext(GenericEvent.of(FAILURE).setException(new ContactsException(e)));
                }
            }
        });
    }


    /**
     * Check if the required runtime permissions have been granted
     */
    public boolean isPermissionGranted() {
        int result = ContextCompat.checkSelfPermission(
            mContext,
            Constants.CONTACTS_PERMISSIONS[0]                                                       // it is enough to check one permission from the group
        );
        return result == PackageManager.PERMISSION_GRANTED;
    }


    // --------------------------- Subroutines

    /**
     * Load a member collection variable with contacts containing email address
     */
    private void loadContacts() {
        mContacts.clear();                                                                          // no caching
        try (Cursor contactsCursor = mContentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,                                                  // URI of the Contacts table of ContactsContract database
            null,                                                                          // SELECTed columns
            null,                                                                           // WHERE
            null,                                                                        // WHERE parameter
            null
        )) {
            if (contactsCursor != null && contactsCursor.getCount() > 0) {
                while (contactsCursor.moveToNext()) {
                    String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    try (Cursor emailCursor = mContentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,                         // URI of the CommonDataKinds.Email table of ContactsContract database
                        null,                                                              // SELECTed columns
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",         // WHERE
                        new String[]{contactId},                                                    // WHERE parameter
                        null
                    )) {
                        if (emailCursor != null && emailCursor.getCount() > 0) {
                            while (emailCursor.moveToNext()) {                                      // could be more than one email
                                String contactEmail = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                                ContactData contactData = new ContactData();
                                contactData.setId(contactId);
                                contactData.setName(contactName);
                                contactData.setEmail(contactEmail);
                                contactData.setPhoto(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.capybara_bighead)); // a placeholder
                                mContacts.add(contactData);
                                mIdsByEmail.put(contactEmail, contactId);
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(
            mContacts,
            (contactA, contactB) -> String.CASE_INSENSITIVE_ORDER.compare(
                contactA.getName(),
                contactB.getName()
            )
        );
    }


    /**
     * @param contactId Contact ID
     * @return Contact photo bitmap, if exists, otherwise null
     */
    private Bitmap getContactPhoto(String contactId) {
        Bitmap result = null;

        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        try (Cursor photoCursor = mContentResolver.query(
            photoUri,
            new String[] {ContactsContract.Contacts.Photo.PHOTO},                                   // SELECTed columns
            null,                                                                           // WHERE
            null,                                                                        // WHERE parameters
            null
        )) {
            if (photoCursor != null && photoCursor.moveToFirst()) {
                byte[] photoBytes = photoCursor.getBlob(0);
                if (photoBytes != null) {
                    result = BitmapFactory.decodeStream(new ByteArrayInputStream(photoBytes));
                }
            }
        }
        return result;
    }


}
