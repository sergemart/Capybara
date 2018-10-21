package com.github.sergemart.mobile.capybara.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialCommonActivity
    extends AppCompatActivity
{

    private static final String TAG = InitialCommonActivity.class.getSimpleName();

    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;
    private CompositeDisposable mEventDisposable;


    // --------------------------- Override activity event handlers

    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_initial_common);

        mEventDisposable = new CompositeDisposable();
        mInitialCommonSharedViewModel = ViewModelProviders.of(this).get(InitialCommonSharedViewModel.class);

        this.setEventListeners();
    }


    /**
     * Start up actions, incl. the app entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart() called");

        // Leave the common initial graph if the APP IS SET UP and the USER IS AUTHENTICATED.
        // Otherwise implicitly delegate control to the local nav AAC
        if ( PreferenceStore.getStoredIsAppModeSet() && CloudRepo.get().isAuthenticated() ) this.leaveNavGraph();
    }


    /**
     * Process responses from intent requests
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent responseIntent) {
        super.onActivityResult(requestCode, resultCode, responseIntent);
        // The result returned from launching the intent from CloudRepo.sendSignInIntent()
        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {
            CloudRepo.get().proceedWithFirebaseAuthAsync(responseIntent);
        }
    }


    // Instance clean-up
    @Override
    public void onDestroy() {
        mEventDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Event subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Activity lifecycle subroutines

    private void setEventListeners() {

        // Set a listener to the "CommonSetupFinished" event
        mEventDisposable.add(mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().subscribe(event -> {
            switch (event) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CommonSetupFinished.SUCCESS event received; leaving nav graph");
                    this.leaveNavGraph();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CommonSetupFinished.FAILURE event received; navigating to fatal error page");
                    this.navigateToFatalErrorPage(event.getException());
            }
        }));

    }


    // --------------------------- Use cases

    /**
     * Leave the nav graph
     */
    private void leaveNavGraph() {
        if (BuildConfig.DEBUG) Log.d(TAG, "Leaving the nav graph");
        Intent intent;
        if (PreferenceStore.getStoredAppMode() == Constants.APP_MODE_MAJOR) {
            intent = InitialMajorActivity.newIntent(this);
        } else {
            intent = MinorActivity.newIntent(this);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        super.startActivity(intent);
    }


    /**
     * Navigate to the fatal error page
     */
    private void navigateToFatalErrorPage(Throwable cause) {
        App.setLastFatalException(new WeakReference<>(cause));
        super.startActivity(ErrorActivity.newIntent( this, cause.getLocalizedMessage() ));
    }


}
