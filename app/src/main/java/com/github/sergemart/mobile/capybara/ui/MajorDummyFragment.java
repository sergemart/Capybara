package com.github.sergemart.mobile.capybara.ui;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


public class MajorDummyFragment extends Fragment {

    private static final String TAG = MajorDummyFragment.class.getSimpleName();

    private MaterialButton mCreateFamilyMemberButton;
    private MaterialButton mDeleteFamilyMemberButton;

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
        View fragmentView = inflater.inflate(R.layout.fragment_major_dummy, container, false);

        mDeleteFamilyMemberButton = fragmentView.findViewById(R.id.button_delete_family_member);
        mCreateFamilyMemberButton = fragmentView.findViewById(R.id.button_create_family_member);

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
                if ( GeoRepo.get().isLocationPermissionGranted() ) this.locateMe();                 // 2nd try, if granted
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

        // Set a listener to the "Delete family member" button
        mViewDisposable.add(
            RxView.clicks(mDeleteFamilyMemberButton).subscribe(event -> this.deleteFamilyMember())
        );

        // Set a listener to the "Create family member" button
        mViewDisposable.add(
            RxView.clicks(mCreateFamilyMemberButton).subscribe(event -> this.createFamilyMember())
        );

    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "CreateFamily" result
        mInstanceDisposable.add(CloudRepo.get().getCreateFamilySubject().subscribe(
            event -> {}
        ));

    }


    // --------------------------- Use cases

    /**
     * Send my location
     */
    private void sendMyLocation() {
        this.locateMe();
        if (mCurrentLocation == null) return;
        CloudRepo.get().sendLocationAsync(mCurrentLocation);
    }


    /**
     * Create family data
     */
    private void createFamily() {
        CloudRepo.get().createFamilyAsync();
    }


    /**
     * Create family member
     */
    private void createFamilyMember() {
        CloudRepo.get().createFamilyMemberAsync("serge.martynov@gmail.com");
    }


    /**
     * Create family member
     */
    private void deleteFamilyMember() {
        CloudRepo.get().deleteFamilyMemberAsync("serge.martynov@gmail.com");
    }


    /**
     *
     */
    private void locateMe() {
        if (GeoRepo.get().isLocationPermissionGranted() ) {
            GeoRepo.get().startLocationUpdates();
        } else {
            super.requestPermissions(Constants.LOCATION_PERMISSIONS, Constants.REQUEST_CODE_LOCATION_PERMISSIONS);
        }
    }

}
