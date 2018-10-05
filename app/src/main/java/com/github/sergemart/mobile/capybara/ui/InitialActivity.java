package com.github.sergemart.mobile.capybara.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.engine.CloudEngine;
import com.github.sergemart.mobile.capybara.viewmodel.SharedStartupViewModel;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialActivity
    extends AppCompatActivity
    implements CloudEngine
{

    private static final String TAG = InitialActivity.class.getSimpleName();

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;
    private SharedStartupViewModel mSharedStartupViewModel;
    private CompositeDisposable mDisposable;


    // --------------------------- Override activity event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        mSharedStartupViewModel = ViewModelProviders.of(this).get(SharedStartupViewModel.class);
        mDisposable = new CompositeDisposable();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) mUsername = mFirebaseUser.getDisplayName();

        // Set a listener to the "APP IS COMPLETELY INITIALIZED" event
        mDisposable.add(mSharedStartupViewModel.getAppIsInitializedSubject()
            .subscribe(this::leaveInitialGraph)
        );

    }


    /**
     * Perform the app entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Leave the initial graph if the APP IS SET UP and the USER IS AUTHENTICATED.
        // Otherwise implicitly delegate control to the local nav AAC
        if ( PreferenceStore.getStoredIsAppModeSet() && this.isAuthenticated() ) this.leaveInitialGraph();
    }


    /**
     * Process responses from intent requests
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent responseIntent) {
        super.onActivityResult(requestCode, resultCode, responseIntent);
        // The result returned from launching the intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {
            this.proceedWithFirebaseAuth(responseIntent);
        }
    }


    // Instance clean-up
    @Override
    public void onDestroy() {
        mDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Subscriptions are disposed.");
        super.onDestroy();
    }


    // --------------------------- Implement a combined CloudEngine interface

    @Override
    public boolean isAuthenticated() {
        return mFirebaseUser != null;
    }


    @Override
    public String getCurrentUsername() {
        if (mUsername.equals(Constants.DEFAULT_USERNAME)) return "";
        else return mUsername;
    }


    @Override
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        super.startActivityForResult(signInIntent, Constants.REQUEST_CODE_SIGN_IN);
    }


    @Override
    public void signOut() {
        mFirebaseAuth.signOut();
        mGoogleSignInClient.signOut();
        mUsername = Constants.DEFAULT_USERNAME;

        mSharedStartupViewModel.emitFirebaseUser(mFirebaseUser);                                    // Notify subscribers
    }


    // --------------------------- Subroutines

    /**
     * Process the response intent from Google client and proceed with Firebase authentication.
     * Emit an event carrying the authenticated Firebase user
     */
    private void proceedWithFirebaseAuth(Intent responseIntent) {
        // Process the response intent from Google client
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(responseIntent);
        GoogleSignInAccount googleSignInAccount;
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            Toast.makeText(this, this.getString(R.string.msg_google_client_connection_error), Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG) Log.e(TAG, "Google sign-in failed: " + e.getStatusCode());
            return;
        }
        if (googleSignInAccount == null) {
            Toast.makeText(this, this.getString(R.string.msg_google_client_connection_error), Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG) Log.e(TAG, "GoogleSignInAccount is null.");
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(this, task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    Toast.makeText(this, this.getString(R.string.msg_firebase_client_connection_error), Toast.LENGTH_SHORT).show();
                    if (BuildConfig.DEBUG) Log.e(TAG, "Firebase sign-in failed.");
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    Toast.makeText(this, this.getString(R.string.msg_firebase_client_connection_error), Toast.LENGTH_SHORT).show();
                    if (BuildConfig.DEBUG) Log.e(TAG, "Firebase user is null.");
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Firebase sign-in succeeded with username: " + mUsername);
                mSharedStartupViewModel.emitFirebaseUser(mFirebaseUser);                            // notify subscribers
            })
        ;
    }


    /**
     * Leave the initial nav graph: into the prod nav graph on startup, or leave for exit on return from the prod graph
     */
    private void leaveInitialGraph() {
        // Finish the app when returning from the prod nav graph
        if(App.finishOnReturnToInitialGraphEnabled()) {
            App.setFinishOnReturnToInitialGraphEnabled(false);                                      // explicitly drop the flag, as the app remains in RAM for a while
            super.finish();
            return;
        }
        // Enter the prod nav graph when launching
        Intent intent;
        if (PreferenceStore.getStoredAppMode() == Constants.APP_MODE_MAJOR) {
            intent = MajorActivity.newIntent(this);
        } else {
            intent = MinorActivity.newIntent(this);
        }
        App.setFinishOnReturnToInitialGraphEnabled(true);
        super.startActivity(intent);
    }

}
