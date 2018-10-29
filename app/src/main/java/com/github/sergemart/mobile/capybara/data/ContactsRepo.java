package com.github.sergemart.mobile.capybara.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.events.GenericResult;
import com.github.sergemart.mobile.capybara.exceptions.ContactsException;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import io.reactivex.Observable;


// Singleton
public class ContactsRepo {

    private static final String TAG = ContactsRepo.class.getSimpleName();

    private static final String[] FROM_COLUMNS = {
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };
    private static final String[] PROJECTION_EMAILS = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Email.DATA
    };

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static ContactsRepo sInstance = new ContactsRepo();


    // Private constructor
    private ContactsRepo() {

        // Init member variables
        mContext = App.getContext();
        mContentResolver = mContext.getContentResolver();
        mContacts = new ArrayList<>();
    }


    // Factory method
    public static ContactsRepo get() {
        if(sInstance == null) sInstance = new ContactsRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private Context mContext;
    private ContentResolver mContentResolver;
    private List<ContactsRepo.Contact> mContacts;


    // --------------------------- Repository interface

//    /**
//     * @return An observable emitting a contact list
//     */
//    public Observable<List<ContactsRepo.Contact>> getContactsObservable() {
//        return Observable.fromCallable(() -> {
//            if (mContacts.isEmpty()) {
//                try {
//                    this.readContacts();
//                    if (BuildConfig.DEBUG) Log.d(TAG, "Contacts loaded");
//                } catch (SecurityException e) {
//                    String errorMessage = mContext.getString(R.string.exception_contacts_no_permission);
//                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
//                }
//            }
//            return mContacts;
//        });
//    }


    /**
     * @return An observable emitting a contact list
     */
    public Observable<GenericResult> getContactsObservable() {
        return Observable.create(emitter -> {
            try {
                this.readContacts();
                if (BuildConfig.DEBUG) Log.d(TAG, "Contacts loaded, emitting them");
                emitter.onNext(GenericResult.SUCCESS.setData(mContacts));
            } catch (SecurityException e) {
                String errorMessage = mContext.getString(R.string.exception_contacts_no_permission);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                emitter.onNext(GenericResult.FAILURE.setException(new ContactsException(errorMessage, e)));
            } catch (Exception e) {
                emitter.onNext(GenericResult.FAILURE.setException(new ContactsException(e)));
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

    private void readContacts() {

        if (BuildConfig.DEBUG) Log.d(TAG, ">>>>>>>>>>>>>>>>>> 1");

        mContacts.clear();                                                                          // no caching
        try (Cursor contactsCursor = mContentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,                                                  // URI of the Contacts table of ContactsContract database
            null,                                                                          // SELECTed column
            null,                                                                           // WHERE
            null,                                                                        // WHERE parameter
            null
        )) {
            if (contactsCursor != null && contactsCursor.getCount() > 0) {

                if (BuildConfig.DEBUG) Log.d(TAG, ">>>>>>>>>>>>>>>>>> 2");

                while (contactsCursor.moveToNext()) {
                    String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (BuildConfig.DEBUG) Log.d(TAG, ">>>>>>>>>>>>>>>>>> 3");

                    try (Cursor emailCursor = mContentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,                         // URI of the CommonDataKinds.Email table of ContactsContract database
                        null,                                                              // SELECTed column
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",         // WHERE
                        new String[]{contactId},                                                    // WHERE parameter
                        null
                    )) {
                        if (emailCursor != null && emailCursor.getCount() > 0) {
                            while (emailCursor.moveToNext()) {                                              // could be more than one email
                                String contactEmail = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                Contact contact = new Contact();
                                contact.id = contactId;
                                contact.name = contactName;
                                contact.email = contactEmail;
                                mContacts.add(contact);

                                if (BuildConfig.DEBUG) Log.d(TAG, ">>>>>>>>>>>>>>>>>> 4");
                            }
                        }
                    }
                }
            }
        }
    }


    // --------------------------- Inner classes: A structure to carry contact attributes

    public class Contact {

        public String id;
        public String name;
        public String email;
    }


}
