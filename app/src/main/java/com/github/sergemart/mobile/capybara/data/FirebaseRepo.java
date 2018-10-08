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
import com.github.sergemart.mobile.capybara.exceptions.FirebaseConnectionException;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseFunctionException;
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
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


// Singleton
public class FirebaseRepo {

    private static final String TAG = FirebaseRepo.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static FirebaseRepo sInstance;


    // Private constructor
    private FirebaseRepo() {
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
    }


    // Factory method
    public static FirebaseRepo get() {
        if(sInstance == null) sInstance = new FirebaseRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Subject<FirebaseUser> mSigninSubject = PublishSubject.create();
    private final CompletableSubject mSignoutSubject = CompletableSubject.create();
    private final CompletableSubject mSendLocationSubject = CompletableSubject.create();

    private final Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;
    private FirebaseFunctions mFirebaseFunctions;


    // --------------------------- Observable getters

    public Subject<FirebaseUser> getSigninSubject() {
        return mSigninSubject;
    }


    public CompletableSubject getSignoutSubject() {
        return mSignoutSubject;
    }


    public CompletableSubject getSendLocatioSubject() {
        return mSendLocationSubject;
    }


    // --------------------------- Repository interface

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
     * Emit an event carrying the authenticated Firebase user
     */
    public void proceedWithFirebaseAuth(Intent responseIntent) {
        // Process the response intent from Google client
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(responseIntent);
        GoogleSignInAccount googleSignInAccount;
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_failed);
            mSigninSubject.onError(new GoogleSigninException(errorMessage, e));
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
            return;
        }
        if (googleSignInAccount == null) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_account_is_null);
            mSigninSubject.onError(new GoogleSigninException(errorMessage));
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    String errorMessage = mContext.getString(R.string.exception_firebase_client_connection_failed);
                    mSigninSubject.onError(new FirebaseConnectionException(errorMessage));
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_is_null);
                    mSigninSubject.onError(new FirebaseConnectionException(errorMessage));
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Firebase sign-in succeeded with username: " + mUsername);
                mSigninSubject.onNext(mFirebaseUser);                                               // notify subscribers on sign in
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

        mSignoutSubject.onComplete();                                                               // notify subscribers on sign out
    }


    /**
     * Send a location
     */
    public void sendLocation(Location location) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_LOCATION, Tools.get().getJsonableLocation(location));

        mFirebaseFunctions
            .getHttpsCallable("sendLocation")
            .call(data)
            .continueWith(task -> {                                                                 // the Continuation
                String result = null;
                try {
                    result = (String) task.getResult().getData();
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_invalid_function_call);
                    mSendLocationSubject.onError(new FirebaseFunctionException(errorMessage, e));
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    mSendLocationSubject.onError(new FirebaseFunctionException(errorMessage, e));
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    mSendLocationSubject.onError(new FirebaseFunctionException(errorMessage));
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                }
            })
            .addOnFailureListener(e -> {
                String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                mSendLocationSubject.onError(new FirebaseFunctionException(errorMessage, e));
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
            })
        ;
    }



}
