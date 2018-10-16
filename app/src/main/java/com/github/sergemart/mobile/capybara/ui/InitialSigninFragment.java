package com.github.sergemart.mobile.capybara.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.InitialSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

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

        this.setEventListeners();
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

        this.setWidgetListeners();
        return fragmentView;
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
     * Set listeners to events
     */
    private void setEventListeners() {
        // Set a listener to the "USER SIGNED IN" event
        mDisposable.add(CloudRepo.get().getSigninSubject().subscribe(event -> {
            if (BuildConfig.DEBUG) Log.d(TAG, "SigninSubject event received in InitialSigninFragment, getting device token");
            this.getDeviceToken();
        }));

        // Set a listener to the "USER SIGNED IN ERROR" event
        mDisposable.add(CloudRepo.get().getSigninErrorSubject().subscribe(e -> {
            if (BuildConfig.DEBUG) Log.d(TAG, "SigninErrorSubject error received in InitialSigninFragment, invoking retry dialog.");
            this.showSigninRetryDialog(e);
        }));

        // Set a listener to the "DEVICE TOKEN RECEIVED" event
        mDisposable.add(CloudRepo.get().getGetDeviceTokenSubject().subscribe(
            event -> this.publishDeviceToken(),
            e -> super.startActivity(ErrorActivity.newIntent( Objects.requireNonNull(super.getActivity()), e.getLocalizedMessage() ))
        ));

        // Set a listener to the "DEVICE TOKEN PUBLISHED" event
        mDisposable.add(CloudRepo.get().getPublishDeviceTokenSubject().subscribe(
            event -> this.navigateToNextPage(),
            e -> super.startActivity(ErrorActivity.newIntent( Objects.requireNonNull(super.getActivity()), e.getLocalizedMessage() ))
        ));
    }


    /**
     * Set listeners to widgets
     */
    private void setWidgetListeners() {
        // Set a listener to the "Sign In" button
        mDisposable.add(RxView.clicks(mSignInButton).subscribe(
            event -> this.signIn()
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
        RetrySigninDialogFragment.newInstance(cause).show(Objects.requireNonNull(super.getFragmentManager()), TAG_SIGN_IN_RETRY_DIALOG);
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
                .setPositiveButton(R.string.action_retry, (dialog, button) -> this.signIn())
                .setNegativeButton(R.string.action_thanks_no, (dialog, button) -> this.navigateToFatalErrorPage())
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
            this.navigateToFatalErrorPage();
        }


        // +++++++++++++++++++++++ Use cases

        /**
         * Sign in with Google account
         */
        private void signIn() {
            CloudRepo.get().sendSignInIntent(Objects.requireNonNull( super.getActivity() ));
        }


        /**
         * Navigate to the fatal error page
         */
        private void navigateToFatalErrorPage() {
            super.startActivity(ErrorActivity.newIntent( Objects.requireNonNull(super.getActivity()), mCause.getLocalizedMessage() ));
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
