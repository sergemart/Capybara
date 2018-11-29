package com.github.sergemart.mobile.capybara.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseFunctionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

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

    private final PublishSubject<GenericEvent> mPublishDeviceTokenSubject = PublishSubject.create();

    private final Context mContext;
    private FirebaseFirestore mFirebaseFirestore;


    // --------------------------- Observable getters

    public PublishSubject<GenericEvent> getPublishDeviceTokenSubject() {
        return mPublishDeviceTokenSubject;
    }


    // --------------------------- The interface

    /**
     * Publish device token on a backend
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void publishDeviceTokenAsync(String deviceToken) {
        if (deviceToken == null || deviceToken.equals("")) {
            if (BuildConfig.DEBUG) Log.e(TAG, "No device token set while attempting to publish it on backend; skipping");
            return;
        }
        if (!AuthService.get().isAuthenticated()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to publish device token on backend; skipping");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_DEVICE_TOKEN, deviceToken);

        mFirebaseFirestore
            .collection(Constants.FIRESTORE_COLLECTION_USERS)
            .document(AuthService.get().getCurrentUser().getUid())
            .set(data, SetOptions.merge())
            .addOnSuccessListener(result -> {
                if (BuildConfig.DEBUG) Log.d(TAG, "Device token published :" + deviceToken);
                mPublishDeviceTokenSubject.onNext(GenericEvent.of(SUCCESS));
            })
            .addOnFailureListener(e -> {
                String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
                mPublishDeviceTokenSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e)) );
            })
        ;
    }

}
