package com.github.sergemart.mobile.capybara.ui.fragments;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.disposables.CompositeDisposable;


public class MajorLocatorFragment
    extends SupportMapFragment
{

    private static final String TAG = MajorLocatorFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;

    private Location mCurrentLocation;
    private CompositeDisposable mViewDisposable;
    private CompositeDisposable mInstanceDisposable;


    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        super.setRetainInstance(true);

        super.getMapAsync(googleMap -> mGoogleMap = googleMap);
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
        this.setViewListeners();
        if (GeoRepo.get().isPermissionGranted() ) GeoRepo.get().startLocationUpdates();
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    /**
     * Pause
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


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {
    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "GOT A LOCATION" event
        mInstanceDisposable.add(GeoRepo.get().getLocationSubject().subscribe(location -> {
            mCurrentLocation = location;
            this.updateMap();
        }));

    }


    // --------------------------- Use cases

    /**
     * Update the map
     */
    private void updateMap() {
        if (mGoogleMap == null) return;

        LatLng myPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        MarkerOptions myPositionMarkerOptions = new MarkerOptions().position(myPosition);
        mGoogleMap.clear();
        mGoogleMap.addMarker(myPositionMarkerOptions);

        LatLngBounds bounds = new LatLngBounds.Builder()                                            // a rectangle around a set of points
            .include(myPosition)
            .build()
        ;
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(cameraUpdate);
    }

}
