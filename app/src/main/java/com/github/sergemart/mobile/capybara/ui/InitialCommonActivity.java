package com.github.sergemart.mobile.capybara.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialCommonActivity
    extends AppCompatActivity
{

    private static final String TAG = InitialCommonActivity.class.getSimpleName();

    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;
    private CompositeDisposable mDisposable;


    // --------------------------- Override activity event handlers

    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_common);

        mDisposable = new CompositeDisposable();
        mInitialCommonSharedViewModel = ViewModelProviders.of(this).get(InitialCommonSharedViewModel.class);

        this.setListeners();
    }


    /**
     * Perform the app entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();

        // App start-up actions
        CloudRepo.get().getTokenAsync();

        // Leave the initial graph if the APP IS SET UP and the USER IS AUTHENTICATED.
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
        mDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Subscriptions are disposed.");
        super.onDestroy();
    }


    // --------------------------- Activity lifecycle subroutines

    private void setListeners() {

        // Set a listener to the "COMMON SETUP FINISHED" event
        mDisposable.add(mInitialCommonSharedViewModel.getCommonSetupFinishedSubject()
            .subscribe(this::leaveNavGraph)
        );

    }


    // --------------------------- Use cases

    /**
     * Leave the nav graph
     */
    private void leaveNavGraph() {
        Intent intent;
        if (PreferenceStore.getStoredAppMode() == Constants.APP_MODE_MAJOR) {
            intent = MajorActivity.newIntent(this);
        } else {
            intent = MinorActivity.newIntent(this);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        super.startActivity(intent);
    }

}
