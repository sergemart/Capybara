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
import com.github.sergemart.mobile.capybara.workflow.dialog.UpgradeBackendRetryDialogFragment;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


public class InitialCommonUpgradeBackendFragment
    extends AbstractFragment
{

    private InitialCommonSharedViewModel mSharedViewModel;
    private Throwable mCause;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialCommonSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_initial_common_upgrade_backend, inflater, container);
        this.setViewListeners();
        return fragmentView;
    }


    /**
     * Startup use cases
     */
    @Override
    public void onStart() {
        super.onStart();
        super.showWaitingState();
        this.upgradeBackend();
    }


    /**
     * Used uncommonly as a callback for the embedded dialog fragment
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.REQUEST_CODE_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {                                             // retry
                    this.upgradeBackend();
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
    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {
    }


    // --------------------------- Use cases

    /**
     * Upgrade the backend database schema
     */
    private void upgradeBackend() {
        pInstanceDisposable.add(mSharedViewModel.upgradeBackendObservableAsync()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "UpgradeBackend success event received; emitting CommonSetupFinished event");
                    mSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                },
                e -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "UpgradeBackend error received; invoking retry dialog");
                    mCause = e;
                    this.showRetryDialog(mCause);
                }
            )
        );
    }


    /**
     * Show sign-in retry dialog
     */
    private void showRetryDialog(Throwable cause) {
        UpgradeBackendRetryDialogFragment.newInstance(cause).show(Objects.requireNonNull(
            super.getChildFragmentManager()),
            Constants.TAG_SIGN_IN_RETRY_DIALOG
        );
    }

}
