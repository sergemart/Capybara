package com.github.sergemart.mobile.capybara.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMajorSharedViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialMajorActivity
    extends AppCompatActivity
{

    private static final String TAG = InitialMajorActivity.class.getSimpleName();

    private InitialMajorSharedViewModel mInitialMajorSharedViewModel;
    private CompositeDisposable mDisposable;


    // --------------------------- Override activity event handlers

    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_initial_major);

        mDisposable = new CompositeDisposable();
        mInitialMajorSharedViewModel = ViewModelProviders.of(this).get(InitialMajorSharedViewModel.class);

        this.setListeners();
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
        mDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Activity lifecycle subroutines

    private void setListeners() {

        // Set a listener to the "MajorSetupFinished" event
        mDisposable.add(mInitialMajorSharedViewModel.getMajorSetupFinishedSubject()
            .subscribe(this::leaveNavGraph)
        );

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


    // --------------------------- Static encapsulation-leveraging methods

    // Create properly configured intent intended to invoke this activity
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, InitialMajorActivity.class);
    }

}
