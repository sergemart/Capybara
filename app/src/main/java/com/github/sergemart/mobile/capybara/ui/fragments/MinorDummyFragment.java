package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;


public class MinorDummyFragment extends Fragment {

    private static final String TAG = MinorDummyFragment.class.getSimpleName();

    private MaterialButton mRequestLocationsButton;
    private MaterialButton mCheckFamilyMembershipButton;

    private CompositeDisposable mViewDisposable;
    private CompositeDisposable mInstanceDisposable;



    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRetainInstance(true);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");

        mViewDisposable = new CompositeDisposable();
        mInstanceDisposable = new CompositeDisposable();

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_minor_dummy, container, false);

        mRequestLocationsButton = fragmentView.findViewById(R.id.button_request_locations);
        mCheckFamilyMembershipButton = fragmentView.findViewById(R.id.button_check_family_membership);

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
                if ( GeoRepo.get().isPermissionGranted() ) this.startLocationTracking();            // 2nd try, if granted
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     *
     */
    @Override
    public void onPause() {
        GeoRepo.get().stopLocationUpdates();
        super.onPause();
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        mViewDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-related subscriptions are disposed");
        super.onDestroyView();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-unrelated subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Widget controls

    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "Send My Location" button
        mViewDisposable.add(
            RxView.clicks(mRequestLocationsButton).subscribe(event -> this.requestLocations())
        );

        // Set a listener to the "Check Family Membership" button
        mViewDisposable.add(
            RxView.clicks(mCheckFamilyMembershipButton).subscribe(event -> this.checkFamilyMembership())
        );

    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {
    }


    // --------------------------- Use cases

    /**
     * Send my location
     */
    private void startLocationTracking() {
        if (GeoRepo.get().isPermissionGranted() ) {
            Toast.makeText(super.getActivity(), "Not implemented!", Toast.LENGTH_LONG).show();
        } else {
            super.requestPermissions(Constants.LOCATION_PERMISSIONS, Constants.REQUEST_CODE_LOCATION_PERMISSIONS);
        }
    }


    /**
     * Request locations
     */
    private void requestLocations() {
        CloudRepo.get().requestLocationsAsync();
    }


    /**
     * Check family membership
     */
    private void checkFamilyMembership() {
        CloudRepo.get().checkFamilyMembershipAsync();
    }


}
