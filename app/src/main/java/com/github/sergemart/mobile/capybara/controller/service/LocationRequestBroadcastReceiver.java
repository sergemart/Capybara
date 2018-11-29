package com.github.sergemart.mobile.capybara.controller.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.source.CloudService;
import com.github.sergemart.mobile.capybara.data.source.GeoService;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;


public class LocationRequestBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LocationRequestBroadcastReceiver.class.getSimpleName();

    private LocationCallback mLocationCallback;


    @Override
    public void onReceive(Context context, Intent intent) {
        if ( !GeoService.get().isPermissionGranted() ) return;
        if (BuildConfig.DEBUG) Log.d(TAG, "onReceive() invoked, sending the location");

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                GeoService.get().stopLocationUpdates(mLocationCallback);
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Got a fix: " + location);
                    CloudService.get().sendLocationAsync(location);
                }
            }
        };
        GeoService.get().startLocationUpdates(mLocationCallback);
    }


    // --------------------------- Static encapsulation-leveraging methods

    public static Intent getIntent() {
        Intent intent = new Intent(App.getContext(), LocationRequestBroadcastReceiver.class);
        intent.setAction(Constants.INTENT_ACTION_SEND_LOCATION);
        return intent;
    }

}
