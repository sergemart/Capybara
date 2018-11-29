package com.github.sergemart.mobile.capybara.controller.fragment;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.source.CloudService;
import com.github.sergemart.mobile.capybara.data.source.GeoService;
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
    private MaterialButton mSendInviteButton;

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
        mSendInviteButton = fragmentView.findViewById(R.id.button_send_invite);

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
                if ( GeoService.get().isPermissionGranted() ) this.locateMe();                 // 2nd try, if granted
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     *
     */
    @Override
    public void onPause() {
        GeoService.get().stopLocationUpdates();
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

        // Set a listener to the "Send invite" button
        mViewDisposable.add(
            RxView.clicks(mSendInviteButton).subscribe(event -> this.sendInvite())
        );

    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "CreateFamily" result
        mInstanceDisposable.add(CloudService.get().getCreateFamilySubject().subscribe(
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
        CloudService.get().sendLocationAsync(mCurrentLocation);
    }


    /**
     * Create family data
     */
    private void createFamily() {
        CloudService.get().createFamilyAsync();
    }


    /**
     * Create family member
     */
    private void createFamilyMember() {
        CloudService.get().createFamilyMemberAsync("serge.martynov@gmail.com");
    }


    /**
     * Create family member
     */
    private void deleteFamilyMember() {
        CloudService.get().deleteFamilyMemberAsync("serge.martynov@gmail.com");
    }


    /**
     * Create family member
     */
    private void sendInvite() {
        CloudService.get().sendInviteAsync("serge.martynov@gmail.com");
    }


    /**
     *
     */
    private void locateMe() {
        if (GeoService.get().isPermissionGranted() ) {
            GeoService.get().startLocationUpdates();
        } else {
            super.requestPermissions(Constants.LOCATION_PERMISSIONS, Constants.REQUEST_CODE_LOCATION_PERMISSIONS);
        }
    }

}
