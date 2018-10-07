package com.github.sergemart.mobile.capybara.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
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

import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


// Singleton
public class GoogleRepo {

    private static final String TAG = GoogleRepo.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static GoogleRepo sRepository;


    // Private constructor
    private GoogleRepo() {
        mContext = App.getContext();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(mContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) mUsername = mFirebaseUser.getDisplayName();

    }


    // Factory method
    public static GoogleRepo get() {
        if(sRepository == null) sRepository = new GoogleRepo();
        return sRepository;
    }


    // --------------------------- Member variables

    private final Subject<FirebaseUser> mSigninSubject = PublishSubject.create();
    private final CompletableSubject mSignoutSubject = CompletableSubject.create();

    private final Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;


    // --------------------------- Observable getters

    public Subject<FirebaseUser> getSigninSubject() {
        return mSigninSubject;
    }


    public CompletableSubject getSignoutSubject() {
        return mSignoutSubject;
    }


    // --------------------------- Repository interface

    public boolean isAuthenticated() {
        return mFirebaseUser != null;
    }


    public String getCurrentUsername() {
        if (mUsername.equals(Constants.DEFAULT_USERNAME)) return "";
        else return mUsername;
    }


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
            mSigninSubject.onError(new RuntimeException( mContext.getString(R.string.exception_google_sign_in_failed), e ));
            if (BuildConfig.DEBUG) Log.e(TAG, "Google sign-in failed: " + e.getStatusCode());
            return;
        }
        if (googleSignInAccount == null) {
            mSigninSubject.onError(new RuntimeException( mContext.getString(R.string.exception_google_sign_in_failed) ));
            if (BuildConfig.DEBUG) Log.e(TAG, "GoogleSignInAccount is null.");
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    mSigninSubject.onError(new RuntimeException( mContext.getString(R.string.exception_firebase_client_connection_failed) ));
                    if (BuildConfig.DEBUG) Log.e(TAG, "Firebase sign-in failed.");
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    mSigninSubject.onError(new RuntimeException( mContext.getString(R.string.exception_firebase_client_connection_failed) ));
                    if (BuildConfig.DEBUG) Log.e(TAG, "Firebase user is null.");
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Firebase sign-in succeeded with username: " + mUsername);
                mSigninSubject.onNext(mFirebaseUser);                                               // notify subscribers on sign in
            })
        ;
    }


    public void signOut() {
        mFirebaseAuth.signOut();
        mGoogleSignInClient.signOut();
        mUsername = Constants.DEFAULT_USERNAME;

        mSignoutSubject.onComplete();                                                               // notify subscribers on sign out
    }


}
