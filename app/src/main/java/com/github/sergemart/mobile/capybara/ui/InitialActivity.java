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
import com.github.sergemart.mobile.capybara.viewmodel.SharedStartupViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialActivity
    extends AppCompatActivity
{

    private static final String TAG = InitialActivity.class.getSimpleName();

    private CompositeDisposable mDisposable;


    // --------------------------- Override activity event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        mDisposable = new CompositeDisposable();

        // Set a listener to the "APP IS COMPLETELY INITIALIZED" event
        SharedStartupViewModel sharedStartupViewModel = ViewModelProviders.of(this).get(SharedStartupViewModel.class);
        mDisposable.add(sharedStartupViewModel.getAppIsInitializedSubject()
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


    // --------------------------- Subroutines

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
