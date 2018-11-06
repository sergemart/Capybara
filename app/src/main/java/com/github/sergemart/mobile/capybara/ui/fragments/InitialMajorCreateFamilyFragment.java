package com.github.sergemart.mobile.capybara.ui.fragments;

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

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.ResRepo;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMajorSharedViewModel;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import static com.github.sergemart.mobile.capybara.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.events.Result.SUCCESS;


public class InitialMajorCreateFamilyFragment
    extends AbstractFragment
{

    private static final String TAG_CREATE_FAMILY_RETRY_DIALOG = "createFamilyRetryDialog";

    private ProgressBar mProgressBar;

    private InitialMajorSharedViewModel mInitialMajorSharedViewModel;
    private Throwable mCause;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialMajorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(super.getActivity())).get(InitialMajorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_initial_major_create_family, container, false);

        pBackgroundImageView = fragmentView.findViewById(R.id.imageView_background);
        mProgressBar = fragmentView.findViewById(R.id.progressBar_waiting);

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
                    this.createFamily();
                } else if (resultCode == Activity.RESULT_CANCELED) {                                // fatal
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                }
                break;
        }
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "CreateFamilySubject" event
        pInstanceDisposable.add(CloudRepo.get().getCreateFamilySubject().subscribe(event -> {
            switch (event.getResult()) {
                case CREATED:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.CREATED event received; emitting MajorSetupFinished event");
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                    break;
                case EXIST:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.EXIST event received; emitting MajorSetupFinished event");
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                    break;
                case EXIST_MORE_THAN_ONE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.EXIST_MORE_THAN_ONE event received; emitting MajorSetupFinished event");
                    mCause = event.getException();
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                    break;
                case BACKEND_ERROR:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.BACKEND_ERROR event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showCreateFamilyRetryDialog(mCause);
            }
        }));
    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {
    }


    // --------------------------- Use cases

    /**
     * Create family data on backend
     */
    private void createFamily() {
        mProgressBar.setVisibility(View.VISIBLE);
        CloudRepo.get().createFamilyAsync();
    }


    /**
     * Show create family retry dialog
     */
    private void showCreateFamilyRetryDialog(Throwable cause) {
        CreateFamilyRetryDialogFragment.newInstance(cause).show(Objects.requireNonNull(super.getChildFragmentManager()), TAG_CREATE_FAMILY_RETRY_DIALOG);
    }


    // --------------------------- Inner classes: Create family retry dialog fragment

    public static class CreateFamilyRetryDialogFragment extends DialogFragment {

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
                .setTitle(ResRepo.get().getCreateFamilyRetryDialogTitleR(mCause))
                .setMessage(ResRepo.get().getCreateFamilyRetryDialogMessageR(mCause))
                .setIcon(ResRepo.get().getCreateFamilyRetryDialogIconR(mCause))
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
        static CreateFamilyRetryDialogFragment newInstance(Throwable cause) {
            CreateFamilyRetryDialogFragment instance = new CreateFamilyRetryDialogFragment();
            instance.setCause(cause);
            return instance;
        }

    }
}
