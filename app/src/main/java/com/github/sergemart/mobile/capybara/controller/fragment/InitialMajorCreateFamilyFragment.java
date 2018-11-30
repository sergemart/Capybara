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
import com.github.sergemart.mobile.capybara.controller.dialog.CreateFamilyRetryDialogFragment;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.datastore.FunctionsService;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMajorSharedViewModel;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


public class InitialMajorCreateFamilyFragment
    extends AbstractFragment
{

    private InitialMajorSharedViewModel mInitialMajorSharedViewModel;
    private Throwable mCause;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialMajorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialMajorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_initial_major_create_family, inflater, container);
        this.setViewListeners();
        return fragmentView;
    }


    /**
     * Startup use cases
     */
    @Override
    public void onStart() {
        super.onStart();
        this.createFamily();
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
            default:
        }
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "CreateFamilySubject" event
        pInstanceDisposable.add(FunctionsService.get().getCreateFamilySubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    PreferenceStore.storeFamilyCreated(true);
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.CREATED event received; emitting MajorSetupFinished event");
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                    break;
                case EXIST:
                    PreferenceStore.storeFamilyCreated(true);
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.EXIST event received; emitting MajorSetupFinished event");
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                    break;
                case INTEGRITY_ERROR:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.INTEGRITY_ERROR event received; emitting MajorSetupFinished event");
                    mCause = event.getException();
                    mInitialMajorSharedViewModel.getMajorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                    break;
                case BACKEND_ERROR:
                    if (BuildConfig.DEBUG) Log.d(TAG, "CreateFamilyResult.BACKEND_ERROR event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showCreateFamilyRetryDialog(mCause);
                default:
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
        super.showWaitingState();
        FunctionsService.get().createFamilyAsync();
    }


    /**
     * Show create family retry dialog
     */
    private void showCreateFamilyRetryDialog(Throwable cause) {
        CreateFamilyRetryDialogFragment.newInstance(cause).show(
            Objects.requireNonNull(super.getChildFragmentManager()),
            Constants.TAG_CREATE_FAMILY_RETRY_DIALOG
        );
    }

}
