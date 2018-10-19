package com.github.sergemart.mobile.capybara.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.Tools;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseDatabaseException;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseMessagingException;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseSigninException;
import com.github.sergemart.mobile.capybara.exceptions.GoogleSigninException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


// Singleton
public class CloudRepo {

    private static final String TAG = CloudRepo.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static CloudRepo sInstance;


    // Private constructor
    private CloudRepo() {

        // Init member variables
        mContext = App.getContext();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(mContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) mUsername = mFirebaseUser.getDisplayName();
        mFirebaseFunctions = FirebaseFunctions.getInstance();
        mFirebaseInstanceId = FirebaseInstanceId.getInstance();
        mDeviceToken = "";
    }


    // Factory method
    public static CloudRepo get() {
        if(sInstance == null) sInstance = new CloudRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Subject<FirebaseUser> mSigninSuccessSubject = PublishSubject.create();
    private final Subject<Throwable> mSigninErrorSubject = PublishSubject.create();
    private final Subject<Boolean> mSignoutSubject = PublishSubject.create();
    private final Subject<Boolean> mGetDeviceTokenSuccessSubject = PublishSubject.create();
    private final Subject<Throwable> mGetDeviceTokenErrorSubject = PublishSubject.create();
    private final Subject<Boolean> mPublishDeviceTokenSuccessSubject = PublishSubject.create();
    private final Subject<Throwable> mPublishDeviceTokenErrorSubject = PublishSubject.create();
    private final Subject<Boolean> mSendLocationSubject = PublishSubject.create();
    private final Subject<Boolean> mCreateFamilySubject = PublishSubject.create();

    private final Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;
    private FirebaseFunctions mFirebaseFunctions;
    private FirebaseInstanceId mFirebaseInstanceId;
    private String mDeviceToken;


    // --------------------------- Observable getters

    public Subject<FirebaseUser> getSigninSuccessSubject() {
        return mSigninSuccessSubject;
    }


    public Subject<Throwable> getSigninErrorSubject() {
        return mSigninErrorSubject;
    }


    public Subject<Boolean> getGetDeviceTokenSuccessSubject() {
        return mGetDeviceTokenSuccessSubject;
    }


    public Subject<Throwable> getGetDeviceTokenErrorSubject() {
        return mGetDeviceTokenErrorSubject;
    }


    public Subject<Boolean> getPublishDeviceTokenSuccessSubject() {
        return mPublishDeviceTokenSuccessSubject;
    }


    public Subject<Throwable> getPublishDeviceTokenErrorSubject() {
        return mPublishDeviceTokenErrorSubject;
    }


    public Subject<Boolean> getCreateFamilySubject() {
        return mCreateFamilySubject;
    }


    public Subject<Boolean> getSignoutSubject() {
        return mSignoutSubject;
    }


    public Subject<Boolean> getSendLocationSubject() {
        return mSendLocationSubject;
    }


    // --------------------------- Repository interface: User authentication

    /**
     * @return true if not authenticated
     */
    public boolean isAuthenticated() {
        return mFirebaseUser != null;
    }


    /**
     * Get current username
     */
    public String getCurrentUsername() {
        if (mUsername.equals(Constants.DEFAULT_USERNAME)) return "";
        else return mUsername;
    }


    /**
     * Send sign-in intent
     */
    public void sendSignInIntent(Activity activity) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, Constants.REQUEST_CODE_SIGN_IN);
    }


    /**
     * Process the response intent from Google client and proceed with Firebase authentication.
     * Send "USER SIGNED IN" event carrying the authenticated Firebase user
     */
    public void proceedWithFirebaseAuthAsync(Intent responseIntent) {
        // Process the response intent from Google client
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(responseIntent);
        GoogleSignInAccount googleSignInAccount;
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);                      // throwa an exception on sign-in error
        } catch (ApiException e) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_failed);
            mSigninErrorSubject.onNext(new GoogleSigninException(errorMessage, e));                 // send "USER SIGN IN ERROR" event
            if (BuildConfig.DEBUG) Log.e(TAG, "SigninErrorSubject emitted an event: " + errorMessage + " caused by: " +  e.getMessage());
            return;
        }
        if (googleSignInAccount == null) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_account_is_null);
            mSigninErrorSubject.onNext(new GoogleSigninException(errorMessage));                    // send "USER SIGN IN ERROR" event
            if (BuildConfig.DEBUG) Log.e(TAG, "SigninErrorSubject emitted an event: " + errorMessage);
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    String errorMessage = mContext.getString(R.string.exception_firebase_client_connection_failed);
                    mSigninErrorSubject.onNext(new FirebaseSigninException(errorMessage));          // send "USER SIGN IN ERROR" event
                    if (BuildConfig.DEBUG) Log.e(TAG, "SigninErrorSubject emitted an event: " + errorMessage);
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_is_null);
                    mSigninErrorSubject.onNext(new FirebaseSigninException(errorMessage));          // send "USER SIGN IN ERROR" event
                    if (BuildConfig.DEBUG) Log.e(TAG, "SigninErrorSubject emitted an event: " + errorMessage);
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Firebase sign-in succeeded with username: " + mUsername);
                mSigninSuccessSubject.onNext(mFirebaseUser);                                               // send "USER SIGNED IN" event
                if (BuildConfig.DEBUG) Log.d(TAG, "SigninSubject emitted an event: " + mUsername);
            })
        ;
    }


    /**
     * Sign out
     */
    public void signOut() {
        mFirebaseAuth.signOut();
        mGoogleSignInClient.signOut();
        mUsername = Constants.DEFAULT_USERNAME;

        mSignoutSubject.onNext(Boolean.TRUE);                                                       // send "USER SIGNED OUT" event
    }


    // --------------------------- Repository interface: Device token for the Firebase messaging

    /**
     * Explicitly get Firebase Messaging device token from the cloud.
     * Send the "DEVICE TOKEN RECEIVED" event
     */
    public void getTokenAsync() {
        mFirebaseInstanceId
            .getInstanceId()
            .addOnSuccessListener(instanseIdResult -> {
                mDeviceToken = instanseIdResult.getToken();
                if (BuildConfig.DEBUG) Log.d(TAG, "Got Firebase Messaging device token: " + mDeviceToken);
                mGetDeviceTokenSuccessSubject.onNext(Boolean.TRUE);                                 // send "DEVICE TOKEN RECEIVED" event
                if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceTokenSuccessSubject emitted an event");
            })
            .addOnFailureListener(e -> {
                String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_received);
                mGetDeviceTokenErrorSubject.onNext(new FirebaseMessagingException(errorMessage, e));// send "DEVICE TOKEN RECEIVE ERROR" event
                if (BuildConfig.DEBUG) Log.e(TAG, "GetDeviceTokenErrorSubject emitted an event: " + errorMessage + " caused by: " +  e.getMessage());
            })
        ;
    }


    /**
     * Update device token when externally received one.
     * Init publishing the token
     */
    public void onTokenReceivedByMessagingService(String deviceToken) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onTokenReceivedByMessagingService() called");
        if (deviceToken.equals(mDeviceToken)) {                                                     // break the possible loop
            if (BuildConfig.DEBUG) Log.e(TAG, "Token already known; skipping.");
            return;
        }
        mDeviceToken = deviceToken;
        this.publishDeviceTokenAsync();
    }


    /**
     * Publish device token on a backend using custom Firebase callable function
     * Send the "DEVICE TOKEN PUBLISHED" event
     */
    @SuppressWarnings("unchecked")
    public void publishDeviceTokenAsync() {
        if (mDeviceToken == null || mDeviceToken.equals("")) {
            if (BuildConfig.DEBUG) Log.e(TAG, "No device token set while attempting to publish it on backend; skipping.");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to publish device token on backend; skipping.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_DEVICE_TOKEN, mDeviceToken);

        mFirebaseFunctions
            .getHttpsCallable("updateDeviceToken")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Published Firebase Messaging device token: " + mDeviceToken);
                    mPublishDeviceTokenSuccessSubject.onNext(Boolean.TRUE);                         // send "DEVICE TOKEN PUBLISHED" event
                    if (BuildConfig.DEBUG) Log.d(TAG, "PublishDeviceTokenSuccessSubject emitted an event");
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    mPublishDeviceTokenErrorSubject.onNext(new FirebaseMessagingException(errorMessage, e)); // send "DEVICE TOKEN PUBLISH ERROR" event
                    if (BuildConfig.DEBUG) Log.e(TAG, "PublishDeviceTokenErrorSubject emitted an event: " + errorMessage + "caused by: " +  e.getMessage());
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    mPublishDeviceTokenErrorSubject.onNext(new FirebaseMessagingException(errorMessage, e)); // send "DEVICE TOKEN PUBLISH ERROR" event
                    if (BuildConfig.DEBUG) Log.e(TAG, "PublishDeviceTokenErrorSubject emitted an event: " + errorMessage + "caused by: " +  e.getMessage());
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    mPublishDeviceTokenErrorSubject.onNext(new FirebaseMessagingException(errorMessage)); // send "DEVICE TOKEN PUBLISH ERROR" event
                    if (BuildConfig.DEBUG) Log.e(TAG, "PublishDeviceTokenErrorSubject emitted an event: " + errorMessage);
                }
            })
        ;
    }


    // --------------------------- Repository interface: Model CRUD

    /**
     * Create family data on a backend using custom Firebase callable function
     * Send the "FAMILY DATA CREATED" event
     */
    @SuppressWarnings("unchecked")
    public void createFamilyAsync() {
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to create family data on backend; skipping.");
            return;
        }

        Map<String, Object> data = new HashMap<>();

        mFirebaseFunctions
            .getHttpsCallable("createFamily")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Created family data on backend");
                    mCreateFamilySubject.onNext(Boolean.TRUE);                                      // send "FAMILY DATA CREATED" event
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onError(new FirebaseDatabaseException(errorMessage, e));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mPublishDeviceTokenSuccessSubject.onError(new FirebaseMessagingException(errorMessage, e));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mPublishDeviceTokenSuccessSubject.onError(new FirebaseMessagingException(errorMessage));
                }
            })
        ;
    }


    // --------------------------- Repository interface: Firebase messaging

    /**
     * Send a location to a backend using custom Firebase callable function
     * Send no resulting events, the silent task
     */
    @SuppressWarnings("unchecked")
    public void sendLocationAsync(Location location) {
        if (location == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Location is null while attempting to send it to backend; skipping.");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to send location to backend; skipping.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_LOCATION, Tools.get().getJsonableLocation(location));

        mFirebaseFunctions
            .getHttpsCallable("sendLocation")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Sent location: " + location);
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                }
            })
        ;
    }


    // --------------------------- Subroutines


}
