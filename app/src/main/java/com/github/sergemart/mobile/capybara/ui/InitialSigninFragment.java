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

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialSharedViewModel;
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
import androidx.navigation.fragment.NavHostFragment;
import io.reactivex.disposables.CompositeDisposable;


public class InitialSigninFragment extends Fragment {

    private static final String TAG = InitialSigninFragment.class.getSimpleName();
    private static final String TAG_SIGN_IN_RETRY_DIALOG = "sign_in_error_dialog";

    private MaterialButton mSignInButton;

    private CompositeDisposable mDisposable;
    private InitialSharedViewModel mInitialSharedViewModel;
    private Throwable mCause;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRetainInstance(true);

        mDisposable = new CompositeDisposable();
        mInitialSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(super.getActivity())).get(InitialSharedViewModel.class);
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_initial_signin, container, false);
        mSignInButton = fragmentView.findViewById(R.id.button_sign_in);

        this.setListeners();
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
                    this.navigateToFatalErrorPage(mCause);
                }
                break;
        }
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mDisposable.clear();
        super.onDestroy();
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to widgets and events
     */
    private void setListeners() {

        // Set a listener to the "Sign In" button
        mDisposable.add(RxView.clicks(mSignInButton).subscribe(
            event -> this.signIn()
        ));

        // Set a listener to the "USER SIGNED IN" event
        mDisposable.add(CloudRepo.get().getSigninSubject().subscribe(event -> {
            if (BuildConfig.DEBUG) Log.d(TAG, "SigninSubject event received in InitialSigninFragment, getting device token");
            this.getDeviceToken();
        }));

        // Set a listener to the "USER SIGNED IN ERROR" event
        mDisposable.add(CloudRepo.get().getSigninErrorSubject().subscribe(e -> {
            if (BuildConfig.DEBUG) Log.d(TAG, "SigninErrorSubject error received in InitialSigninFragment, invoking retry dialog.");
            mCause = e;
            this.showSigninRetryDialog(mCause);
        }));

        // Set a listener to the "DEVICE TOKEN RECEIVED" event
        mDisposable.add(CloudRepo.get().getGetDeviceTokenSubject().subscribe(
            event -> this.publishDeviceToken(),
            this::navigateToFatalErrorPage
        ));

        // Set a listener to the "DEVICE TOKEN PUBLISHED" event
        mDisposable.add(CloudRepo.get().getPublishDeviceTokenSubject().subscribe(
            event -> this.navigateToNextPage(),
            this::navigateToFatalErrorPage
        ));

    }


    // --------------------------- Use cases

    /**
     * Sign in with Google account
     */
    private void signIn() {
        CloudRepo.get().sendSignInIntent(Objects.requireNonNull( super.getActivity() ));
    }


    /**
     * Show sign-in retry dialog
     */
    private void showSigninRetryDialog(Throwable cause) {
        RetrySigninDialogFragment.newInstance(cause).show(Objects.requireNonNull(super.getChildFragmentManager()), TAG_SIGN_IN_RETRY_DIALOG);
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


    /**
     * Return to the initial setup page, if the app mode is not set.
     * Otherwise, notify subscribers that the app is completely initialized
     */
    private void navigateToNextPage() {
        if (!PreferenceStore.getStoredIsAppModeSet()) {
            NavHostFragment.findNavController(this).popBackStack();
        } else {
            mInitialSharedViewModel.emitAppIsInitialized();
        }
    }


    /**
     * Navigate to the fatal error page
     */
    private void navigateToFatalErrorPage(Throwable cause) {
        App.setLastFatalException(new WeakReference<>(cause));
        super.startActivity(ErrorActivity.newIntent( Objects.requireNonNull(super.getActivity()), cause.getLocalizedMessage() ));
    }


    // --------------------------- Inner classes: Sign-in retry dialog fragment

    public static class RetrySigninDialogFragment extends DialogFragment {

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
                .setTitle(R.string.title_google_signin_failed)
                .setMessage(R.string.msg_google_signin_canceled_by_user)
                .setIcon(R.mipmap.ic_launcher)
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


        // +++++++++++++++++++++++ Static encapsulation-leveraging methods

        /**
         * The dialog fragment factory
         */
        static RetrySigninDialogFragment newInstance(Throwable cause) {
            RetrySigninDialogFragment instance = new RetrySigninDialogFragment();
            instance.setCause(cause);
            return instance;
        }

    }
}
