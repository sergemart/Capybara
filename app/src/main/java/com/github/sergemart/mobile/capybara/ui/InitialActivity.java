package com.github.sergemart.mobile.capybara.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialSharedViewModel;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialActivity
    extends AppCompatActivity
{

    private static final String TAG = InitialActivity.class.getSimpleName();

    private CompositeDisposable mDisposable;


    // --------------------------- Override activity event handlers

    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        mDisposable = new CompositeDisposable();

        // Set a listener to the "APP IS COMPLETELY INITIALIZED" event
        InitialSharedViewModel initialSharedViewModel = ViewModelProviders.of(this).get(InitialSharedViewModel.class);
        mDisposable.add(initialSharedViewModel.getAppIsInitializedSubject()
            .subscribe(this::leaveInitialGraph)
        );

        // Set local nav graph supplemental error handlers
        mDisposable.add(CloudRepo.get().getSigninSubject()
            .subscribe(event -> {}, e -> App.setLastFatalException( new WeakReference<>(e) ))
        );
        mDisposable.add(CloudRepo.get().getSignoutSubject()
            .subscribe(event -> {}, e -> App.setLastFatalException( new WeakReference<>(e) ))
        );
        mDisposable.add(CloudRepo.get().getGetDeviceTokenSubject()
            .subscribe(event -> {}, e -> App.setLastFatalException( new WeakReference<>(e) ))
        );
        mDisposable.add(CloudRepo.get().getPublishDeviceTokenSubject()
            .subscribe(event -> {}, e -> App.setLastFatalException( new WeakReference<>(e) ))
        );
        mDisposable.add(CloudRepo.get().getCreateFamilySubject()
            .subscribe(event -> {}, e -> App.setLastFatalException( new WeakReference<>(e) ))
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
        if ( PreferenceStore.getStoredIsAppModeSet() && CloudRepo.get().isAuthenticated() ) this.leaveInitialGraph();
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


    // --------------------------- Use cases

    /**
     * Leave the initial nav graph
     */
    private void leaveInitialGraph() {
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
