package com.github.sergemart.mobile.capybara.controller.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.controller.dialog.SignInRetryDialogFragment;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.data.repo.DeviceTokenRepo;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


public class InitialCommonSignInFragment
    extends AbstractFragment
{

    private MaterialButton mSignInButton;

    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;
    private Throwable mCause;
    private boolean mSignInStarted;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialCommonSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialCommonSharedViewModel.class);
        mSignInStarted = false;

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_initial_common_sign_in, inflater, container);

        mSignInButton = fragmentView.findViewById(R.id.button_sign_in);

        this.indicateSignInInProgress();

        this.setViewListeners();
        return fragmentView;
    }


    /**
     * Used uncommonly as a callback for the embedded dialog fragment
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.REQUEST_CODE_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {                                             // retry
                    this.signIn();
                } else if (resultCode == Activity.RESULT_CANCELED) {                                // fatal
                    mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                }
                break;
            default:
        }
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "SignIn" event
        pInstanceDisposable.add(AuthService.get().getSignInSubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "SignInResult.SUCCESS event received; getting device token");
                    this.getDeviceToken();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "SignInResult.FAILURE event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showSigninRetryDialog(mCause);
                    break;
                default:
            }
        }));

        // Set a listener to the "GetDeviceToken" event
        pInstanceDisposable.add(AuthService.get().getGetDeviceTokenSubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceTokenResult.SUCCESS event received; publishing device token");
                    this.updateDeviceToken();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceTokenResult.FAILURE event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showSigninRetryDialog(mCause);
                    break;
                default:
            }
        }));

    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "Sign In" button
        pViewDisposable.add(RxView.clicks(mSignInButton).subscribe(event ->
            this.signIn()
        ));

    }


    // --------------------------- Use cases

    /**
     * Sign in with Google account
     */
    private void signIn() {
        mSignInStarted = true;
        this.indicateSignInInProgress();
        AuthService.get().sendSignInIntent(Objects.requireNonNull( super.getActivity() ));
    }


    /**
     * Indicate that sign-in process is started and in progress
     */
    private void indicateSignInInProgress() {
        if (!mSignInStarted) return;
        super.showWaitingState();
        mSignInButton.setVisibility(View.GONE);
    }


    /**
     * Show sign-in retry dialog
     */
    private void showSigninRetryDialog(Throwable cause) {
        SignInRetryDialogFragment.newInstance(cause).show(Objects.requireNonNull(
            super.getChildFragmentManager()),
            Constants.TAG_SIGN_IN_RETRY_DIALOG
        );
    }


    /**
     * Get a Firebase Messaging device token
     */
    private void getDeviceToken() {
        AuthService.get().getTokenAsync();
    }


    /**
     * Update the Firebase Messaging device token in the app and on the backend
     */
    private void updateDeviceToken() {
        String deviceToken = DeviceTokenRepo.get().getCurrentDeviceToken();
        pViewDisposable.add(DeviceTokenRepo.get().updateDeviceToken(deviceToken).subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Device token successfully updated; emitting CommonSetupFinished event");
                    mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Error while updating the device token; invoking retry dialog");
                    mCause = event.getException();
                    this.showSigninRetryDialog(mCause);
                    break;
                default:
            }
        }));
    }

}
