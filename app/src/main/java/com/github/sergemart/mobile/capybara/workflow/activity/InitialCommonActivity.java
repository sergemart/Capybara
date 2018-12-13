package com.github.sergemart.mobile.capybara.workflow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;

import java.lang.ref.WeakReference;
import java.util.Objects;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;


public class InitialCommonActivity
    extends AbstractActivity
{

    private NavController mNavController;
    private InitialCommonSharedViewModel mSharedViewModel;


    // --------------------------- Override activity event handlers

    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_initial_common);

        mNavController = Navigation.findNavController(this, R.id.fragment_nav_host_initial_common);
        mSharedViewModel = ViewModelProviders.of(this).get(InitialCommonSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * Start up use cases, including the nav graph entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        // The initial setup is done when
        // - the APP MODE IS SET and
        // - the USER IS AUTHENTICATED
        boolean initialSetupDone = (
                PreferenceStore.getAppMode() == Constants.APP_MODE_MAJOR ||
                PreferenceStore.getAppMode() == Constants.APP_MODE_MINOR
            )
            && AuthService.get().isAuthenticated()

        ;
        // The backend schema upgrade is not needed when
        // - the CURRENT BACKEND VERSION CORRESPONDS THE APP VERSION
        boolean backendSchemaOk = PreferenceStore.getCurrentBackendVersion() == BuildConfig.VERSION_CODE;
        // The initial app graph may be left when
        // - the INITIAL SETUP IS DONE and
        // - the BACKEND SCHEMA UPGRADE IN NOT NEEDED
        boolean mayLeaveNavGraph = initialSetupDone && backendSchemaOk;

        if (initialSetupDone && !backendSchemaOk) {
            this.navigateToUpgradeBackendPage();
        }
        else if (mayLeaveNavGraph) {
            this.leaveNavGraph();
        }
        // Otherwise implicitly delegate control to the local nav AAC
    }


    /**
     * Process responses from intent requests
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent responseIntent) {
        super.onActivityResult(requestCode, resultCode, responseIntent);
        // The result returned from launching the intent from CloudRepo.sendSignInIntent()
        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {
            AuthService.get().proceedWithFirebaseAuthAsync(responseIntent);
        }
    }


    // --------------------------- Activity lifecycle subroutines

    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        // Set a listener to the "CommonSetupFinished" event
        pInstanceDisposable.add(mSharedViewModel.getCommonSetupFinishedSubject().subscribe(event -> {
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
        if (PreferenceStore.getAppMode() == Constants.APP_MODE_MAJOR) {
            intent = InitialMajorActivity.newIntent(this);
        } else {
            intent = InitialMinorActivity.newIntent(this);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        super.startActivity(intent);
    }


    /**
     * Navigate to the upgrade backend page
     */
    private void navigateToUpgradeBackendPage() {
        NavOptions navOptions = new NavOptions.Builder()
            .setPopUpTo(                                                                            // clear the entire task TODO: Works not as expected: clears nav graph fragment also. Action-based nav could be broken!
                Objects.requireNonNull(mNavController.getCurrentDestination()).getId(),             // docs recommend use nav graph id here. Does not work
                true
            )
            .build()
        ;
        mNavController.navigate(R.id.fragment_initial_common_upgrade_backend, null, navOptions);
    }


    /**
     * Navigate to the fatal error page
     */
    private void navigateToFatalErrorPage(Throwable cause) {
        App.setLastFatalException(new WeakReference<>(cause));
        super.startActivity(ErrorActivity.newIntent( this));
    }


}
