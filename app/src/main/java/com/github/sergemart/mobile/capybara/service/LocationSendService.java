package com.github.sergemart.mobile.capybara.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.GeoRepo;

import io.reactivex.disposables.CompositeDisposable;


public class LocationSendService extends Service {

    private static final String TAG = LocationSendService.class.getSimpleName();

    private CompositeDisposable mDisposable;


    // --------------------------- Override service event handlers


    /**
     * Prepare member variables and listeners
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() invoked");
        mDisposable = new CompositeDisposable();
        this.setInstanceListeners();
    }


    /**
     * Act and wait for the explicit stop call
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand() invoked");
        if ( !GeoRepo.get().isPermissionGranted() ) {                                               // no permission, stop service
            super.stopSelf();
            return START_NOT_STICKY;
        }
        GeoRepo.get().startLocationUpdates();
        return START_NOT_STICKY;
    }


    /**
     * No bind supposed
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Clean up
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy() invoked");
        mDisposable.clear();
    }


    // --------------------------- Service lifecycle subroutines

    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        mDisposable.add(GeoRepo.get().getLocationSubject().subscribe(location -> {
            GeoRepo.get().stopLocationUpdates();
            CloudRepo.get().sendLocationAsync(location);
            LocationSendAlarmController.get().setAlarm();                                           // reschedule the alarm calling the service
            super.stopSelf();                                                                       // all done, exit
        }));

    }


    // --------------------------- Static encapsulation-leveraging methods

    public static PendingIntent getPendingIntent() {
        Intent intent = new Intent(App.getContext(), LocationSendService.class);

        return PendingIntent.getService(
            App.getContext(),
            Constants.REQUEST_CODE_SEND_LOCATION,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

}
