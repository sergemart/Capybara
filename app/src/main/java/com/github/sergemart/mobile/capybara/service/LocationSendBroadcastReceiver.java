package com.github.sergemart.mobile.capybara.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.GeoRepo;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;


public class LocationSendBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LocationSendBroadcastReceiver.class.getSimpleName();

    private LocationCallback mLocationCallback;


    @Override
    public void onReceive(Context context, Intent intent) {
        if ( !GeoRepo.get().isPermissionGranted() ) return;
        if (BuildConfig.DEBUG) Log.d(TAG, "onReceive() invoked, sending the location");

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                GeoRepo.get().stopLocationUpdates(mLocationCallback);
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Got a fix: " + location);
                    CloudRepo.get().sendLocationAsync(location);
                }
            }
        };
        GeoRepo.get().startLocationUpdates(mLocationCallback);
    }


    // --------------------------- Static encapsulation-leveraging methods

    public static Intent getIntent() {
        Intent intent = new Intent(App.getContext(), LocationSendBroadcastReceiver.class);
        intent.setAction(Constants.INTENT_ACTION_SEND_LOCATION);
        return intent;
    }

}
