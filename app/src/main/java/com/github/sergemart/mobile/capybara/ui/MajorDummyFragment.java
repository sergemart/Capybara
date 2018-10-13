package com.github.sergemart.mobile.capybara.ui;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.github.sergemart.mobile.capybara.data.Repository;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.CompositeDisposable;


public class MajorDummyFragment extends Fragment {

    private static final String TAG = MajorDummyFragment.class.getSimpleName();

    private MaterialButton mSendMyLocationButton;
    private MaterialButton mCreateFamilyButton;

    private Location mCurrentLocation;
    private CompositeDisposable mDisposable;


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
        View fragmentView = inflater.inflate(R.layout.fragment_major_dummy, container, false);

        this.initMemberVariables(fragmentView);
        this.setAttributes();
        this.setListeners();

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


    // Instance clean-up
    @Override
    public void onDestroy() {
        mDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Subscriptions are disposed.");
        super.onDestroy();
    }


    // --------------------------- Widget controls

    /**
     * Init member variables
     */
    private void initMemberVariables(View fragmentView) {
        mSendMyLocationButton = fragmentView.findViewById(R.id.button_send_my_location);
        mCreateFamilyButton = fragmentView.findViewById(R.id.button_create_family);

        mDisposable = new CompositeDisposable();

    }


    /**
     * Set attributes
     */
    private void setAttributes() {
        super.setRetainInstance(true);
    }


    /**
     * Set listeners to widgets and containers
     */
    private void setListeners() {
        // Set a listener to the "Send My Location" button
        mDisposable.add(
            RxView.clicks(mSendMyLocationButton).subscribe(event -> this.sendMyLocation())
        );

        // Set a listener to the "Create family data" button
        mDisposable.add(
            RxView.clicks(mCreateFamilyButton).subscribe(event -> this.createFamily())
        );

        // Set a listener to the "DEVICE TOKEN PUBLISHED" event
        mDisposable.add(CloudRepo.get().getCreateFamilySubject().subscribe(
            event -> {},
            e -> super.startActivity(ErrorActivity.newIntent( Objects.requireNonNull(super.getActivity()), e.getLocalizedMessage() ))
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
