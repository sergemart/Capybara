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
import io.reactivex.disposables.CompositeDisposable;


public class InitialMajorCreateFamilyFragment extends Fragment {

    private static final String TAG = InitialMajorCreateFamilyFragment.class.getSimpleName();
    private static final String TAG_SIGN_IN_RETRY_DIALOG = "signInRetryDialog";

    private MaterialButton mSignInButton;
    private ProgressBar mProgressBar;

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
        mProgressBar = fragmentView.findViewById(R.id.progressBar_waiting);

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
                } else if (resultCode == Activity.RESULT_CANCELED) {                                // fatal
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
    }

    // --------------------------- Use cases



    // --------------------------- Inner classes: Sign-in retry dialog fragment

    public static class SigninRetryDialogFragment extends DialogFragment {

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
                .setTitle(ResRepo.get().getSigninRetryDialogTitleR(mCause))
                .setMessage(ResRepo.get().getSigninRetryDialogMessageR(mCause))
                .setIcon(ResRepo.get().getSigninRetryDialogIconR(mCause))
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
        static SigninRetryDialogFragment newInstance(Throwable cause) {
            SigninRetryDialogFragment instance = new SigninRetryDialogFragment();
            instance.setCause(cause);
            return instance;
        }

    }
}
