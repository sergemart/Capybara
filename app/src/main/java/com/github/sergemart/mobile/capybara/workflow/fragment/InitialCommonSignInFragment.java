package com.github.sergemart.mobile.capybara.workflow.fragment;

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
import com.github.sergemart.mobile.capybara.workflow.dialog.RetrySignInDialogFragment;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.model.CurrentUser;
import com.github.sergemart.mobile.capybara.data.repo.CurrentUserRepo;
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

    private InitialCommonSharedViewModel mSharedViewModel;
    private Throwable mCause;
    private boolean mSignInStarted;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialCommonSharedViewModel.class);
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
                    mSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
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
                    if (BuildConfig.DEBUG) Log.d(TAG, "SignIn.SUCCESS event received; getting device token");
                    this.getDeviceToken();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "SignIn.FAILURE event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showRetrySignInDialog(mCause);
                    break;
                default:
            }
        }));

        // Set a listener to the "GetDeviceToken" event
        pInstanceDisposable.add(AuthService.get().getGetDeviceTokenSubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceToken.SUCCESS event received; publishing device token");
                    this.updateCurrentUser();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceToken.FAILURE event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showRetrySignInDialog(mCause);
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
     * Show retry sign-in dialog
     */
    private void showRetrySignInDialog(Throwable cause) {
        RetrySignInDialogFragment.newInstance(cause).show(Objects.requireNonNull(
            super.getChildFragmentManager()),
            Constants.TAG_RETRY_SIGN_IN_DIALOG
        );
    }


    /**
     * Get a Firebase Messaging device token
     */
    private void getDeviceToken() {
        AuthService.get().getTokenAsync();
    }


    /**
     * Update the user data with Firebase Messaging device token and the app mode
     */
    private void updateCurrentUser() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setAppMode(PreferenceStore.getAppMode());
        currentUser.setDeviceToken(CurrentUserRepo.get().readSync().getDeviceToken());              // read authoritative data
        pViewDisposable.add(CurrentUserRepo.get().updateAsync(currentUser).subscribe(
            () -> {                                                                                 // update the repo with the authoritative data
                if (BuildConfig.DEBUG) Log.d(TAG, "Current user data successfully updated; emitting CommonSetupFinished event");
                mSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
            },
            e -> {
                if (BuildConfig.DEBUG) Log.d(TAG, "Error while updating the current user data; invoking retry dialog");
                mCause = e;
                this.showRetrySignInDialog(mCause);
            }
        ));
    }

}
