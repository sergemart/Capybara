package com.github.sergemart.mobile.capybara.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.ResRepo;
import com.github.sergemart.mobile.capybara.events.GenericResult;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.lang.ref.WeakReference;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class InitialCommonSignInFragment extends Fragment {

    private static final String TAG = InitialCommonSignInFragment.class.getSimpleName();
    private static final String TAG_SIGN_IN_RETRY_DIALOG = "signInRetryDialog";

    private MaterialButton mSignInButton;
    private ProgressBar mProgressBar;

    private CompositeDisposable mWidgetDisposable;
    private CompositeDisposable mEventDisposable;
    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;
    private Throwable mCause;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        super.setRetainInstance(true);

        mWidgetDisposable = new CompositeDisposable();
        mEventDisposable = new CompositeDisposable();
        mInitialCommonSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(super.getActivity())).get(InitialCommonSharedViewModel.class);

        this.setEventListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_initial_common_sign_in, container, false);
        mSignInButton = fragmentView.findViewById(R.id.button_sign_in);
        mProgressBar = fragmentView.findViewById(R.id.progressBar_waiting);

        this.setWidgetListeners();
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
                    mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericResult.FAILURE.setException(mCause));
                }
                break;
        }
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        mWidgetDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Widget subscriptions are disposed");
        super.onDestroyView();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mEventDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Event subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to widgets
     */
    private void setWidgetListeners() {

        // Set a listener to the "Sign In" button
        mWidgetDisposable.add(RxView.clicks(mSignInButton).subscribe(
            event -> this.signIn()
        ));

    }


    /**
     * Set listeners to events
     */
    private void setEventListeners() {

        // Set a listener to the "SignInResult" event
        mEventDisposable.add(CloudRepo.get().getSignInSubject().subscribe(event -> {
            switch (event) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "SignInResult.SUCCESS event received in InitialCommonSignInFragment, getting device token");
                    this.getDeviceToken();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "SignInResult.FAILURE event received in InitialCommonSignInFragment, invoking retry dialog");
                    mCause = event.getException();
                    this.showSigninRetryDialog(mCause);
            }
        }));

        // Set a listener to the "GetDeviceTokenResult" event
        mEventDisposable.add(CloudRepo.get().getGetDeviceTokenSubject().subscribe(event -> {
            switch (event) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceTokenResult.SUCCESS event received in InitialCommonSignInFragment, publishing device token");
                    this.publishDeviceToken();
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "GetDeviceTokenResult.FAILURE event received in InitialCommonSignInFragment, invoking retry dialog");
                    mCause = event.getException();
                    this.showSigninRetryDialog(mCause);
            }
        }));

        // Set a listener to the "PublishDeviceTokenResult" event
        mEventDisposable.add(CloudRepo.get().getPublishDeviceTokenSubject().subscribe(event -> {
            switch (event) {
                case SUCCESS:
                    if (BuildConfig.DEBUG) Log.d(TAG, "PublishDeviceTokenResult.SUCCESS event received in InitialCommonSignInFragment; emmitting CommonSetupFinished event");
                    mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericResult.SUCCESS);
                    break;
                case FAILURE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "PublishDeviceTokenResult.FAILURE event received in InitialCommonSignInFragment; invoking retry dialog");
                    mCause = event.getException();
                    this.showSigninRetryDialog(mCause);
            }
        }));

    }


    // --------------------------- Use cases

    /**
     * Sign in with Google account
     */
    private void signIn() {
        mSignInButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        CloudRepo.get().sendSignInIntent(Objects.requireNonNull( super.getActivity() ));
    }


    /**
     * Show sign-in retry dialog
     */
    private void showSigninRetryDialog(Throwable cause) {
        SignInRetryDialogFragment.newInstance(cause).show(Objects.requireNonNull(super.getChildFragmentManager()), TAG_SIGN_IN_RETRY_DIALOG);
    }


    /**
     * Get a Firebase Messaging device token
     */
    private void getDeviceToken() {
        CloudRepo.get().getTokenAsync();
    }


    /**
     * Publish the Firebase Messaging device token
     */
    private void publishDeviceToken() {
        CloudRepo.get().publishDeviceTokenAsync();
    }


    // --------------------------- Inner classes: Sign-in retry dialog fragment

    public static class SignInRetryDialogFragment extends DialogFragment {

        private Throwable mCause;


        // +++++++++++++++++++++++ Getters/ setters

        void setCause(Throwable cause) {
            mCause = cause;
        }


        // +++++++++++++++++++++++ Override dialog fragment lifecycle event handlers

        /**
         * View-unrelated startup actions
         */
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }


        /**
         * The dialog factory
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull( super.getActivity() ))
                .setTitle(ResRepo.get().getSignInRetryDialogTitleR(mCause))
                .setMessage(ResRepo.get().getSignInRetryDialogMessageR(mCause))
                .setIcon(ResRepo.get().getSignInRetryDialogIconR(mCause))
                .setPositiveButton(R.string.action_retry, (dialog, button) ->
                    Objects.requireNonNull(super.getParentFragment()).onActivityResult(             // use Fragment#onActivityResult() as a callback
                        Constants.REQUEST_CODE_DIALOG_FRAGMENT,
                        Activity.RESULT_OK,
                        super.getActivity().getIntent()
                    )
                )
                .setNegativeButton(R.string.action_thanks_no, (dialog, button) -> dialog.cancel())
                .create()
            ;
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);                                                       // a kind of modal (not really)
            return alertDialog;
        }


        /**
         * On cancel
         */
        @Override
        public void onCancel(DialogInterface dialog) {
            Objects.requireNonNull(super.getParentFragment()).onActivityResult(                     // use Fragment#onActivityResult() as a callback
                Constants.REQUEST_CODE_DIALOG_FRAGMENT,
                Activity.RESULT_CANCELED,
                Objects.requireNonNull(super.getActivity()).getIntent()
            );
        }


        /**
         * Fix a compat lib bug causing the dialog dismiss on rotate
         */
        @Override
        public void onDestroyView() {
            if (super.getDialog() != null && super.getRetainInstance()) super.getDialog().setDismissMessage(null);
            super.onDestroyView();
        }


        // +++++++++++++++++++++++ Static encapsulation-leveraging methods

        /**
         * The dialog fragment factory
         */
        static SignInRetryDialogFragment newInstance(Throwable cause) {
            SignInRetryDialogFragment instance = new SignInRetryDialogFragment();
            instance.setCause(cause);
            return instance;
        }

    }
}
