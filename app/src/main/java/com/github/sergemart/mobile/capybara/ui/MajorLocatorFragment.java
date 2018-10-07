package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class MajorLocatorFragment extends SupportMapFragment {

    private static final String TAG = MajorLocatorFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;


    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.initMemberVariables();
        this.setAttributes();
        this.setListeners();

        this.locateMe();

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    /**
     * A callback on process the runtime permission dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_LOCATION_PERMISSIONS:
                if ( GeoRepo.get().isLocationPermissionGranted() ) this.locateMe();                 // 2nd try, if granted
                else {} // TODO: Notify about restricted functionality
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


    // --------------------------- Widget controls

    /**
     * Init member variables
     */
    private void initMemberVariables() {
        super.getMapAsync(googleMap -> mGoogleMap = googleMap);
    }


    /**
     * Set attributes
     */
    private void setAttributes() {
    }


    /**
     * Set listeners to widgets and containers
     */
    private void setListeners() {
    }


    // --------------------------- Subroutines

    private void locateMe() {
        if (GeoRepo.get().isLocationPermissionGranted() ) {
            GeoRepo.get().startLocationUpdates();
        } else {
            super.requestPermissions(Constants.LOCATION_PERMISSIONS, Constants.REQUEST_CODE_LOCATION_PERMISSIONS);
        }
    }

}
