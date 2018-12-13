package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exception.FirebaseFunctionException;
import com.github.sergemart.mobile.capybara.exception.FirebaseSigninException;
import com.github.sergemart.mobile.capybara.exception.GoogleSigninException;
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
import com.google.firebase.iid.FirebaseInstanceId;

import io.reactivex.Completable;
import io.reactivex.subjects.PublishSubject;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


// Singleton
public class AuthService {

    private static final String TAG = AuthService.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static AuthService sInstance;


    // Private constructor
    private AuthService() {

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
        mFirebaseInstanceId = FirebaseInstanceId.getInstance();
        mCurrentDeviceToken = "";
    }


    // Factory method
    public static AuthService get() {
        if(sInstance == null) sInstance = new AuthService();
        return sInstance;
    }


    // --------------------------- Member variables

    private final PublishSubject<GenericEvent> mSignInSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mGetDeviceTokenSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mSignOutSubject = PublishSubject.create();

    private final Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;
    private FirebaseInstanceId mFirebaseInstanceId;
    private String mCurrentDeviceToken;


    // --------------------------- Observable getters

    public PublishSubject<GenericEvent> getSignInSubject() {
        return mSignInSubject;
    }


    public PublishSubject<GenericEvent> getGetDeviceTokenSubject() {
        return mGetDeviceTokenSubject;
    }


    public PublishSubject<GenericEvent> getSignOutSubject() {
        return mSignOutSubject;
    }


    // --------------------------- The interface: User authentication

    /**
     * @return true if authenticated, false if not
     */
    public boolean isAuthenticated() {
        return mFirebaseUser != null;
    }


    /**
     * @return Current user
     */
    public FirebaseUser getCurrentUser() {
        return mFirebaseUser;
    }


    /**
     * @return Current username
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
            googleSignInAccount = completedTask.getResult(ApiException.class);                      // throws an exception on sign-in error
        } catch (ApiException e) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_failed);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
            mSignInSubject.onNext(GenericEvent.of(FAILURE).setException( new GoogleSigninException(errorMessage, e)) );
            return;
        }
        if (googleSignInAccount == null) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_account_is_null);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSignInSubject.onNext(GenericEvent.of(FAILURE).setException( new GoogleSigninException(errorMessage)) );
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    String errorMessage = mContext.getString(R.string.exception_firebase_client_connection_failed);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSignInSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseSigninException(errorMessage)) );
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_is_null);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSignInSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseSigninException(errorMessage)) );
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Signed in successfully: " + mUsername);
                mSignInSubject.onNext(GenericEvent.of(SUCCESS).setData(mFirebaseUser));
            })
        ;
    }


    /**
     * Sign in with email and password.
     * Out-of-workflow method to use in tests
     */
    public Completable signInWithEmailAndPassword(String email, String password) {
        return Completable.create(emitter ->
            mFirebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if ( !task.isSuccessful() ) {                                                   // error check
                        String errorMessage = mContext.getString(R.string.exception_firebase_client_connection_failed);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new FirebaseSigninException(errorMessage));
                        return;
                    }
                    mFirebaseUser = mFirebaseAuth.getCurrentUser();
                    if (mFirebaseUser == null) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_user_is_null);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new FirebaseSigninException(errorMessage));
                        return;
                    }
                    mUsername = mFirebaseUser.getDisplayName();
                    if (BuildConfig.DEBUG) Log.d(TAG, "Signed in successfully: " + mUsername);
                    emitter.onComplete();
                })
        );
    }


    /**
     * Sign out the current user.
     * Send an event notifying on success or failure
     */
    public void signOut() {
        try {
            mFirebaseAuth.signOut();
            mGoogleSignInClient.signOut();
            mFirebaseUser = null;
            mUsername = Constants.DEFAULT_USERNAME;
            mSignOutSubject.onNext(GenericEvent.of(SUCCESS));
        } catch (Exception e) {
            mSignOutSubject.onNext(GenericEvent.of(FAILURE).setException(new FirebaseSigninException(e)));
        }
    }


    // --------------------------- The interface: Manage the device token


    /**
     * The current device token getter
     */
    public String getCurrentDeviceToken() {
        return mCurrentDeviceToken;
    }


    /**
     * The current device token setter
     */
    public void setCurrentDeviceToken(String deviceToken) {
        mCurrentDeviceToken = deviceToken;
    }


    /**
     * Explicitly get Firebase Messaging device token from the cloud.
     * Send an event notifying on success or failure
     */
    public void getTokenAsync() {
        mFirebaseInstanceId
            .getInstanceId()
            .addOnSuccessListener(instanseIdResult -> {
                mCurrentDeviceToken = instanseIdResult.getToken();
                if (BuildConfig.DEBUG) Log.d(TAG, "Got device token: " + mCurrentDeviceToken);
                mGetDeviceTokenSubject.onNext(GenericEvent.of(SUCCESS));
            })
            .addOnFailureListener(e -> {
                String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_received);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
                mGetDeviceTokenSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e)) );
            })
        ;
    }

}
