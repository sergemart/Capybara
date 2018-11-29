package com.github.sergemart.mobile.capybara.controller.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.source.CloudService;
import com.github.sergemart.mobile.capybara.data.source.GeoService;
import com.github.sergemart.mobile.capybara.data.PreferenceRepo;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.controller.dialog.GrantPermissionRetryDialogFragment;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


public class InitialCommonSetupFragment
    extends AbstractFragment
{

    private MaterialButton mIAmMajorButton;
    private MaterialButton mIAmMinorButton;

    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;


    // --------------------------- Override fragment event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialCommonSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialCommonSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_initial_common_setup, inflater, container);

        mIAmMajorButton = fragmentView.findViewById(R.id.button_i_am_major);
        mIAmMinorButton = fragmentView.findViewById(R.id.button_i_am_minor);

        this.setViewListeners();
        return fragmentView;
    }


    /**
     * A callback on process the runtime permission dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_LOCATION_PERMISSIONS:
                if ( GeoService.get().isPermissionGranted() ) {
                    this.navigateToNextPage();
                } else {
                    this.showGrantPermissionRetryDialog();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Used uncommonly as a callback for the embedded dialog fragment
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.REQUEST_CODE_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {                                             // retry
                    this.requestLocationPermissions();
                } else if (resultCode == Activity.RESULT_CANCELED) {                                // no permission, go further
                    this.navigateToNextPage();
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

        // Set a listener to the "I Am a Major" button
        pViewDisposable.add(RxView.clicks(mIAmMajorButton).subscribe(event -> {
            PreferenceRepo.storeAppMode(Constants.APP_MODE_MAJOR);
            this.requestLocationPermissions();
        }));

        // Set a listener to the "I Am a Minor" button
        pViewDisposable.add(RxView.clicks(mIAmMinorButton).subscribe(event -> {
            PreferenceRepo.storeAppMode(Constants.APP_MODE_MINOR);
            this.requestLocationPermissions();
        }));
    }


    // --------------------------- Use cases

    /**
     * Navigate to the login page, if not authenticated.
     * Otherwise, notify subscribers (hosting activity) that the app is completely initialized
     */
    private void navigateToNextPage() {
        if (!CloudService.get().isAuthenticated()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_initialSetup_to_initialSignin);
        } else {
            mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
        }
    }


    /**
     * Request location permissions
     */
    private void requestLocationPermissions() {
        super.requestPermissions(Constants.LOCATION_PERMISSIONS, Constants.REQUEST_CODE_LOCATION_PERMISSIONS);
    }


    /**
     * Show grant permission retry dialog
     */
    private void showGrantPermissionRetryDialog() {
        GrantPermissionRetryDialogFragment.newInstance().show(Objects.requireNonNull(
            super.getChildFragmentManager()),
            Constants.TAG_GRANT_PERMISSION_RETRY_DIALOG
        );
    }

}
