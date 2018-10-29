package com.github.sergemart.mobile.capybara.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMajorSharedViewModel;

import java.lang.ref.WeakReference;

import androidx.lifecycle.ViewModelProviders;


public class InitialMajorActivity
    extends AbstractActivity
{

    private InitialMajorSharedViewModel mInitialMajorSharedViewModel;


    // --------------------------- Override activity event handlers

    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_initial_major);

        mInitialMajorSharedViewModel = ViewModelProviders.of(this).get(InitialMajorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * Start up actions, incl. entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        CloudRepo.get().createFamilyAsync();
    }


    // --------------------------- Activity lifecycle subroutines


    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        // Set a listener to the "MajorSetupFinished" event
        pInstanceDisposable.add(mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().subscribe(event -> {
            switch (event) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "MajorSetupFinished.SUCCESS event received; leaving nav graph");
                    this.leaveNavGraph();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "MajorSetupFinished.FAILURE event received; navigating to fatal error page");
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
        Intent intent = MajorActivity.newIntent(this);
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


    // --------------------------- Static encapsulation-leveraging methods

    /**
     * Create properly configured intent intended to invoke this activity
     */
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, InitialMajorActivity.class);
    }

}
