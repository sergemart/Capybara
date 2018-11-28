package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.github.sergemart.mobile.capybara.data.MessagingRepo;
import com.github.sergemart.mobile.capybara.data.model.FamilyMember;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class CommonLocatorFragment
    extends AbstractFragment
{

    private static final String TAG = CommonLocatorFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private FloatingActionButton mRequestLocationsFab;

    private FamilyMember mMe;
    private Map<String, FamilyMember> mTrackedFamilyMembers;


    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMe = new FamilyMember();
        mTrackedFamilyMembers = new ConcurrentHashMap<>();

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_common_locator, inflater, container);
        mRequestLocationsFab = fragmentView.findViewById(R.id.fab_request_locations);

        SupportMapFragment mapFragment = (SupportMapFragment) super.getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment != null ) mapFragment.getMapAsync(googleMap -> {
            mGoogleMap = googleMap;
            mGoogleMap.setMaxZoomPreference(Constants.MAP_MAX_ZOOM);
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mGoogleMap.setOnMarkerClickListener(marker -> {
                mRequestLocationsFab.hide();
                return false;                                                                       // default behavior should also occur
            });
            mGoogleMap.setOnMapClickListener(point -> mRequestLocationsFab.show());
        });

        this.setViewListeners();
        if (GeoRepo.get().isPermissionGranted() ) GeoRepo.get().startLocationUpdates();
        return fragmentView;
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        GeoRepo.get().stopLocationUpdates();
        super.onDestroyView();
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

        // Set a listener to the "Request Locations" FAB
        // Make it send a request for family members' locations
        pViewDisposable.add(RxView.clicks(mRequestLocationsFab).subscribe(event ->
            CloudRepo.get().requestLocationsAsync()
        ));

        // Set a listener to the "LocateMe" event
        // Discover my location and update the map
        pViewDisposable.add(GeoRepo.get().getLocateMeSubject().subscribe(location -> {
            mMe.setLocation(location);
            this.updateMap();
        }));

        // Set a listener to the "LocationReceived" event
        // Add a responded family member into the collection and update the map
        pViewDisposable.add(MessagingRepo.get().getLocationReceivedSubject()
            .observeOn(AndroidSchedulers.mainThread())                                              // Maps API requires it
            .subscribe(event -> {
                FamilyMember familyMember = new FamilyMember();
                familyMember.setEmail(event.getSenderEmail());
                familyMember.setLocation(event.getLocation());
                mTrackedFamilyMembers.put(event.getSenderEmail(), familyMember);                    // use email as a key
                this.updateMap();
            })
        );

    }


    // --------------------------- Use cases

    /**
     * Update the map
     */
    private void updateMap() {
        if (mMe.getLocation() == null) return;
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
        mRequestLocationsFab.show();
    }

}
