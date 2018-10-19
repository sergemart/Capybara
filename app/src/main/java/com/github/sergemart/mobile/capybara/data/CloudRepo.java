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
import com.github.sergemart.mobile.capybara.events.CreateFamilyResult;
import com.github.sergemart.mobile.capybara.events.GenericResult;
import com.github.sergemart.mobile.capybara.events.SignInResult;
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

    private final Subject<SignInResult> mSignInSubject = PublishSubject.create();
    private final Subject<GenericResult> mGetDeviceTokenSubject = PublishSubject.create();
    private final Subject<GenericResult> mPublishDeviceTokenSubject = PublishSubject.create();
    private final Subject<CreateFamilyResult> mCreateFamilySubject = PublishSubject.create();
    private final Subject<Boolean> mSendLocationSubject = PublishSubject.create();
    private final Subject<Boolean> mSignOutSubject = PublishSubject.create();

    private final Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;
    private FirebaseFunctions mFirebaseFunctions;
    private FirebaseInstanceId mFirebaseInstanceId;
    private String mDeviceToken;


    // --------------------------- Observable getters

    public Subject<SignInResult> getSignInSubject() {
        return mSignInSubject;
    }


    public Subject<GenericResult> getGetDeviceTokenSubject() {
        return mGetDeviceTokenSubject;
    }


    public Subject<GenericResult> getPublishDeviceTokenSubject() {
        return mPublishDeviceTokenSubject;
    }


    public Subject<CreateFamilyResult> getCreateFamilySubject() {
        return mCreateFamilySubject;
    }


    public Subject<Boolean> getSignOutSubject() {
        return mSignOutSubject;
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
     * Send an event carrying the authenticated Firebase user
     */
    public void proceedWithFirebaseAuthAsync(Intent responseIntent) {
        // Process the response intent from Google client
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(responseIntent);
        GoogleSignInAccount googleSignInAccount;
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);                      // throwa an exception on sign-in error
        } catch (ApiException e) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_failed);
            mSignInSubject.onNext(SignInResult.FAILURE.setException( new GoogleSigninException(errorMessage, e)) );
            if (BuildConfig.DEBUG) Log.e(TAG, "SigninSubject emitted a failure event: " + errorMessage + " caused by: " +  e.getMessage());
            return;
        }
        if (googleSignInAccount == null) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_account_is_null);
            mSignInSubject.onNext(SignInResult.FAILURE.setException( new GoogleSigninException(errorMessage)) );
            if (BuildConfig.DEBUG) Log.e(TAG, "SigninSubject emitted a failure event: " + errorMessage);
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    String errorMessage = mContext.getString(R.string.exception_firebase_client_connection_failed);
                    mSignInSubject.onNext(SignInResult.FAILURE.setException( new FirebaseSigninException(errorMessage)) );
                    if (BuildConfig.DEBUG) Log.e(TAG, "SigninSubject emitted a failure event: " + errorMessage);
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_is_null);
                    mSignInSubject.onNext(SignInResult.FAILURE.setException( new FirebaseSigninException(errorMessage)) );
                    if (BuildConfig.DEBUG) Log.e(TAG, "SigninSubject emitted a failure event: " + errorMessage);
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Firebase sign-in succeeded with username: " + mUsername);
                mSignInSubject.onNext(SignInResult.SUCCESS.setFirebaseUser(mFirebaseUser));                                               // send "USER SIGNED IN" event
                if (BuildConfig.DEBUG) Log.d(TAG, "SigninSubject emitted a success event: " + mUsername);
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

        mSignOutSubject.onNext(Boolean.TRUE);                                                       // send "USER SIGNED OUT" event
    }


    // --------------------------- Repository interface: Device token for the Firebase messaging

    /**
     * Explicitly get Firebase Messaging device token from the cloud.
     * Send an event notifying on success or failure
     */
    public void getTokenAsync() {
        mFirebaseInstanceId
            .getInstanceId()
            .addOnSuccessListener(instanseIdResult -> {
                mDeviceToken = instanseIdResult.getToken();
                if (BuildConfig.DEBUG) Log.d(TAG, "Got Firebase Messaging device token: " + mDeviceToken);
                mGetDeviceTokenSubject.onNext(GenericResult.SUCCESS);
                if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceTokenSubject emitted a success event");
            })
            .addOnFailureListener(e -> {
                String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_received);
                mGetDeviceTokenSubject.onNext(GenericResult.FAILURE.setException( new FirebaseMessagingException(errorMessage, e)) );
                if (BuildConfig.DEBUG) Log.e(TAG, "GetDeviceTokenSubject emitted a failure event: " + errorMessage + " caused by: " +  e.getMessage());
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
     * Send an event notifying on success or failure
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
                    mPublishDeviceTokenSubject.onNext(GenericResult.SUCCESS);
                    if (BuildConfig.DEBUG) Log.d(TAG, "PublishDeviceTokenSubject emitted a success event");
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    mPublishDeviceTokenSubject.onNext(GenericResult.FAILURE.setException( new FirebaseMessagingException(errorMessage, e)) );
                    if (BuildConfig.DEBUG) Log.e(TAG, "PublishDeviceTokenSubject emitted a failure event: " + errorMessage + "caused by: " +  e.getMessage());
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    mPublishDeviceTokenSubject.onNext(GenericResult.FAILURE.setException( new FirebaseMessagingException(errorMessage, e)) );
                    if (BuildConfig.DEBUG) Log.e(TAG, "PublishDeviceTokenSubject emitted a failure event: " + errorMessage + "caused by: " +  e.getMessage());
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    mPublishDeviceTokenSubject.onNext(GenericResult.FAILURE.setException( new FirebaseMessagingException(errorMessage)) );
                    if (BuildConfig.DEBUG) Log.e(TAG, "PublishDeviceTokenSubject emitted a failure event: " + errorMessage);
                }
            })
        ;
    }


    // --------------------------- Repository interface: Model CRUD

    /**
     * Create family data on a backend using custom Firebase callable function
     * Send the "CreateFamily" result
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
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    String familyUid;
                    switch (returnCode) {
                        case "00":
                            familyUid = (String)Objects.requireNonNull(result.get("familyUid"));
                            if (BuildConfig.DEBUG) Log.d(TAG, "Created family data on backend");
                            mCreateFamilySubject.onNext(CreateFamilyResult.CREATED.setFamilyUid(familyUid));
                            break;
                        case "01":
                            familyUid = (String)Objects.requireNonNull(result.get("familyUid"));
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family data already exist on backend");
                            mCreateFamilySubject.onNext(CreateFamilyResult.EXIST.setFamilyUid(familyUid));
                            break;
                        case "90":
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more the one family data sets stored on backend");
                            mCreateFamilySubject.onNext(CreateFamilyResult.EXIST_MORE_THAN_ONE);
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, "Unknown return code received from Firebase Function");
                            mCreateFamilySubject.onNext(CreateFamilyResult.BACKEND_ERROR.setException( new FirebaseDatabaseException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onNext(CreateFamilyResult.BACKEND_ERROR.setException( new FirebaseDatabaseException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onNext(CreateFamilyResult.BACKEND_ERROR.setException( new FirebaseMessagingException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mCreateFamilySubject.onNext(CreateFamilyResult.BACKEND_ERROR.setException( new FirebaseMessagingException(errorMessage) ));
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
