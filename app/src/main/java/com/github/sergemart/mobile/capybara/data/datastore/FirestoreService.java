package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseDbException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
        mFirebaseFirestore = FirebaseFirestore.getInstance();
    }


    // Factory method
    public static FirestoreService get() {
        if(sInstance == null) sInstance = new FirestoreService();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Context mContext;
    private FirebaseFirestore mFirebaseFirestore;


    // --------------------------- The interface

    /**
     * Create or update a user document on a backend in a way which works for the online and offline modes as well
     */
    public Completable updateUserAsync(Map<String, Object> userData) {
        return Completable.create(emitter -> {
            if (userData == null) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "No user data provided while attempting to update user on backend; skipping");
                return;
            }
            if (!AuthService.get().isAuthenticated()) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "User not authenticated while attempting to update user on backend; skipping");
                return;
            }
            String userUid = AuthService.get().getCurrentUser().getUid();
            mFirebaseFirestore
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
    @SuppressWarnings("unchecked")
    public Single<DocumentSnapshot> readSystemDatabaseAsync() {
        return Single.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "User not authenticated while attempting to read system data on backend; skipping");
                return;
            }
            mFirebaseFirestore
                .collection(Constants.FIRESTORE_COLLECTION_SYSTEM)
                .document(Constants.FIRESTORE_DOCUMENT_DATABASE)
                .get()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Read system/database document");
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

}
