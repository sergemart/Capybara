package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exception.FirebaseDbException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;


// Singleton
public class FirestoreService {

    private static final String TAG = FirestoreService.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static FirestoreService sInstance;


    // Private constructor
    private FirestoreService() {
        // Init member variables
        mContext = App.getContext();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings firestoreSettings = new FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)                                                  // Preferred way to store timestamps (instead of Date)
            .build()
        ;
        mFirestore.setFirestoreSettings(firestoreSettings);
    }


    // Factory method
    public static FirestoreService get() {
        if(sInstance == null) sInstance = new FirestoreService();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Context mContext;
    private FirebaseFirestore mFirestore;


    // --------------------------- The interface

    /**
     * Create or update a user document on a backend in a way which works for the online and offline modes as well
     */
    public Completable updateUserAsync(Map<String, Object> userData) {
        return Completable.create(emitter -> {
            if (userData == null) {
                String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            String userUid = AuthService.get().getCurrentUser().getUid();
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_USERS)
                .document(userUid)
                .set(userData, SetOptions.merge())                                                  // here used the local replica
            ;
            if (BuildConfig.DEBUG) Log.d(TAG, "User created or updated :" + userUid);
            emitter.onComplete();
        });
    }


    /**
     * Read a system/database document on a backend
     */
    public Single<DocumentSnapshot> readSystemDatabaseAsync() {
        return Single.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_SYSTEM)
                .document(Constants.FIRESTORE_DOCUMENT_DATABASE)
                .get()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Have read system/database document");
                    emitter.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_system_data_not_read);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


    /**
     * Read a family document on a backend
     */
    public Single<QuerySnapshot> readFamilyAsync(String creatorId) {
        return Single.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_FAMILIES)
                .whereEqualTo(Constants.FIRESTORE_FIELD_CREATOR, creatorId)
                .get()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Have read family document(s)");
                    emitter.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_read);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }

}
