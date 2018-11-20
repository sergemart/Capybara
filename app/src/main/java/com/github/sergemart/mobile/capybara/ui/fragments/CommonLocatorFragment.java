package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.github.sergemart.mobile.capybara.data.MessagingRepo;
import com.github.sergemart.mobile.capybara.model.FamilyMember;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class CommonLocatorFragment
    extends SupportMapFragment
{

    private static final String TAG = CommonLocatorFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;

    private FamilyMember mMe;
    private Map<String, FamilyMember> mTrackedFamilyMembers;
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
        mMe = new FamilyMember();
        mTrackedFamilyMembers = new ConcurrentHashMap<>();
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
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        GeoRepo.get().stopLocationUpdates();
        mViewDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroyView() called, view-related subscriptions disposed");
        super.onDestroyView();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy() called, instance subscriptions disposed");
        super.onDestroy();
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

//        // Set a listener to the "LocateMe" event
//        // Discover my location an update the map
//        mInstanceDisposable.add(GeoRepo.get().getLocateMeSubject().subscribe(location -> {
//            mMe.setLocation(location);
//            this.updateMap();
//        }));
//
//        // Set a listener to the "PollLocations" event
//        // Send location requests to family members
//        mInstanceDisposable.add(MessagingRepo.get().getPollLocationsTimerObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(counter -> {
//            if (BuildConfig.DEBUG) Log.d(TAG, "Poll locations tick is emitted");
//            CloudRepo.get().requestLocationsAsync();
//        }));
//
//        // Set a listener to the "LocationReceived" event
//        // Add a responded family member into the collection and update the map
//        mInstanceDisposable.add(MessagingRepo.get().getLocationReceivedSubject().subscribe(event -> {
//            FamilyMember familyMember = new FamilyMember();
//            familyMember.setEmail(event.getSenderEmail());
//            familyMember.setLocation(event.getLocation());
//            mTrackedFamilyMembers.put(event.getSenderEmail(), familyMember);                        // use email as a key
//            this.updateMap();
//        }));

    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "LocateMe" event
        // Discover my location an update the map
        mViewDisposable.add(GeoRepo.get().getLocateMeSubject().subscribe(location -> {
            mMe.setLocation(location);
            this.updateMap();
        }));

        // Set a listener to the "PollLocations" event
        // Send location requests to family members
        mViewDisposable.add(MessagingRepo.get().getPollLocationsTimerObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(counter -> {
            if (BuildConfig.DEBUG) Log.d(TAG, "Poll locations tick is emitted");
            CloudRepo.get().requestLocationsAsync();
        }));

        // Set a listener to the "LocationReceived" event
        // Add a responded family member into the collection and update the map
        mViewDisposable.add(MessagingRepo.get().getLocationReceivedSubject().subscribe(event -> {
            FamilyMember familyMember = new FamilyMember();
            familyMember.setEmail(event.getSenderEmail());
            familyMember.setLocation(event.getLocation());
            mTrackedFamilyMembers.put(event.getSenderEmail(), familyMember);                        // use email as a key
            this.updateMap();
        }));

    }


    // --------------------------- Use cases

    /**
     * Update the map
     */
    private void updateMap() {
        if (mGoogleMap == null) return;
        mGoogleMap.clear();

        // Add me onto the map
        LatLng myPosition = new LatLng(mMe.getLocation().getLatitude(), mMe.getLocation().getLongitude());
        MarkerOptions myPositionMarkerOptions = new MarkerOptions()
            .position(myPosition)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        ;
        mGoogleMap.addMarker(myPositionMarkerOptions);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder().include(myPosition);        // a rectangle around a set of points

        // Add tracked family members onto the map
        for (String familyMemberKey : mTrackedFamilyMembers.keySet()) {
            FamilyMember familyMember = mTrackedFamilyMembers.get(familyMemberKey);
            if (familyMember == null) continue;
            LatLng familyMemberPosition = new LatLng(familyMember.getLocation().getLatitude(), familyMember.getLocation().getLongitude());
            MarkerOptions familyMemberPositionMarkerOptions = new MarkerOptions()
                .position(familyMemberPosition)
            ;
            mGoogleMap.addMarker(familyMemberPositionMarkerOptions);
            boundsBuilder.include(familyMemberPosition);
        }
        LatLngBounds bounds = boundsBuilder.build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(cameraUpdate);
    }

}
