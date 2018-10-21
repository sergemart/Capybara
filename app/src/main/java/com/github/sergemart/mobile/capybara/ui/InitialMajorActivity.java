package com.github.sergemart.mobile.capybara.ui;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialMajorActivity
    extends AppCompatActivity
{

    private static final String TAG = InitialMajorActivity.class.getSimpleName();

    private InitialMajorSharedViewModel mInitialMajorSharedViewModel;
    private CompositeDisposable mInstanceDisposable;


    // --------------------------- Override activity event handlers

    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_initial_major);

        mInstanceDisposable = new CompositeDisposable();
        mInitialMajorSharedViewModel = ViewModelProviders.of(this).get(InitialMajorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * Start up actions, incl. entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart() called");

        // App start-up actions
        CloudRepo.get().createFamilyAsync();
    }


    // Instance clean-up
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-unrelated subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Activity lifecycle subroutines

    private void setInstanceListeners() {

        // Set a listener to the "MajorSetupFinished" event
        mInstanceDisposable.add(mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().subscribe(event -> {
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

    // Create properly configured intent intended to invoke this activity
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, InitialMajorActivity.class);
    }

}
