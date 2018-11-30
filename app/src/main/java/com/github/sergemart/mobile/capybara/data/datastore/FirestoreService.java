package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseDbException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

import io.reactivex.Observable;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


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
     * Create or update a user on a backend
     * Send an event notifying on success or failure
     */
    public Observable<GenericEvent> updateUserAsync(Map<String, Object> userData) {
        return Observable.create(emitter -> {
            if (userData == null) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "No user data provided while attempting to create or update it on backend; skipping");
                return;
            }
            if (!AuthService.get().isAuthenticated()) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "User not authenticated while attempting to create or update it on backend; skipping");
                return;
            }
            String userUid = AuthService.get().getCurrentUser().getUid();
            mFirebaseFirestore
                .collection(Constants.FIRESTORE_COLLECTION_USERS)
                .document(userUid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "User created or updated :" + userUid);
                    emitter.onNext(GenericEvent.of(SUCCESS));
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_not_updated) + ": " + userUid;
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onNext(GenericEvent.of(FAILURE).setException(new FirebaseDbException(errorMessage, e)));
                })
            ;
        });
    }

}
