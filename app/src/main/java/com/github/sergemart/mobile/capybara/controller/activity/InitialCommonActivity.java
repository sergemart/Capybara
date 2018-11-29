package com.github.sergemart.mobile.capybara.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.source.CloudService;
import com.github.sergemart.mobile.capybara.data.PreferenceRepo;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;

import java.lang.ref.WeakReference;

import androidx.lifecycle.ViewModelProviders;


public class InitialCommonActivity
    extends AbstractActivity
{

    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;


    // --------------------------- Override activity event handlers

    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_initial_common);

        mInitialCommonSharedViewModel = ViewModelProviders.of(this).get(InitialCommonSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * Start up use cases, including the nav graph entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Leave the common initial graph if the APP MODE IS SET and the USER IS AUTHENTICATED.
        // Otherwise implicitly delegate control to the local nav AAC
        if (
            (
                PreferenceRepo.getAppMode() == Constants.APP_MODE_MAJOR ||
                PreferenceRepo.getAppMode() == Constants.APP_MODE_MINOR
            ) &&
                CloudService.get().isAuthenticated()
        ){
            this.leaveNavGraph();
        }
    }


    /**
     * Process responses from intent requests
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent responseIntent) {
        super.onActivityResult(requestCode, resultCode, responseIntent);
        // The result returned from launching the intent from CloudRepo.sendSignInIntent()
        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {
            CloudService.get().proceedWithFirebaseAuthAsync(responseIntent);
        }
    }


    // --------------------------- Activity lifecycle subroutines

    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        // Set a listener to the "CommonSetupFinished" event
        pInstanceDisposable.add(mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().subscribe(event -> {
            switch (event.getResult()) {
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
        if (PreferenceRepo.getAppMode() == Constants.APP_MODE_MAJOR) {
            intent = InitialMajorActivity.newIntent(this);
        } else {
            intent = InitialMinorActivity.newIntent(this);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        super.startActivity(intent);
    }


    /**
     * Navigate to the fatal error page
     */
    private void navigateToFatalErrorPage(Throwable cause) {
        App.setLastFatalException(new WeakReference<>(cause));
        super.startActivity(ErrorActivity.newIntent( this));
    }


}
