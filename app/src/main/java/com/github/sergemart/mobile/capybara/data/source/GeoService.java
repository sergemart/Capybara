package com.github.sergemart.mobile.capybara.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exceptions.PermissionException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.core.content.ContextCompat;
import io.reactivex.subjects.PublishSubject;


// Singleton
public class GeoService {

    private static final String TAG = GeoService.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static GeoService sInstance;


    // Private constructor
    private GeoService() {
        mContext = App.getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Got a fix: " + location + "; emitting");
                    mLocateMeSubject.onNext(location);
                }
            }
        };
    }


    // Factory method
    public static GeoService get() {
        if(sInstance == null) sInstance = new GeoService();
        return sInstance;
    }


    // --------------------------- Member variables

    private final PublishSubject<Location> mLocateMeSubject = PublishSubject.create();

    private final Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;


    // --------------------------- Observable getters

    public PublishSubject<Location> getLocateMeSubject() {
        return mLocateMeSubject;
    }


    // --------------------------- The interface

    /**
     * Start location updates
     */
    public void startLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,null);
            if (BuildConfig.DEBUG) Log.d(TAG, "Location updates started");
        } catch (SecurityException e) {
            String errorMessage = mContext.getString(R.string.exception_location_no_permission);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
            mLocateMeSubject.onError(new PermissionException(errorMessage));
        }
    }


    /**
     * Start location updates with external callback
     */
    public void startLocationUpdates(LocationCallback locationCallback) {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback,null);
            if (BuildConfig.DEBUG) Log.d(TAG, "Location updates started");
        } catch (SecurityException e) {
            String errorMessage = mContext.getString(R.string.exception_location_no_permission);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
        }
    }


    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (BuildConfig.DEBUG) Log.d(TAG, "Location updates stopped");
    }


    /**
     * Stop location updates with external callback
     */
    public void stopLocationUpdates(LocationCallback locationCallback) {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        if (BuildConfig.DEBUG) Log.d(TAG, "Location updates stopped");
    }


    /**
     * Check if the required runtime permissions have been granted
     */
    public boolean isPermissionGranted() {
        int result = ContextCompat.checkSelfPermission(
            mContext,
            Constants.LOCATION_PERMISSIONS[0]                                                       // it is enough to check one permission from the group
        );
        return result == PackageManager.PERMISSION_GRANTED;
    }



}
