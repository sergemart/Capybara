package com.github.sergemart.mobile.capybara.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exceptions.LocationPermissionException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.core.content.ContextCompat;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


// Singleton
public class GeoRepo {

    private static final String TAG = GeoRepo.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static GeoRepo sInstance;


    // Private constructor
    private GeoRepo() {
        mContext = App.getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    mLocationSubject.onNext(location);
                    if (BuildConfig.DEBUG) Log.d(TAG, "Got a fix: " + location);
                }
            }
        };
    }


    // Factory method
    public static GeoRepo get() {
        if(sInstance == null) sInstance = new GeoRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Subject<Location> mLocationSubject = PublishSubject.create();

    private final Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;


    // --------------------------- Observable getters

    public Subject<Location> getLocationSubject() {
        return mLocationSubject;
    }


    // --------------------------- Repository interface

    /**
     * Start location updates
     */
    public void startLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,null);
            if (BuildConfig.DEBUG) Log.d(TAG, "Location updates started.");
        } catch (SecurityException e) {
            String errorMessage = mContext.getString(R.string.exception_location_no_permission);
            mLocationSubject.onError(new LocationPermissionException(errorMessage));
            if (BuildConfig.DEBUG) Log.e(TAG, "LocationSubject emitted an error: " + errorMessage + "caused by: " +  e.getMessage());
        }
    }


    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (BuildConfig.DEBUG) Log.d(TAG, "Location updates stopped.");
    }


    /**
     * Check if the required runtime permissions have been granted
     */
    public boolean isLocationPermissionGranted() {
        int result = ContextCompat.checkSelfPermission(
            mContext,
            Constants.LOCATION_PERMISSIONS[0]                                                       // it is enough to check one permission from the group
        );
        return result == PackageManager.PERMISSION_GRANTED;
    }



}
