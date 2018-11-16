package com.github.sergemart.mobile.capybara.service;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.Constants;


// Singleton
public class LocationSendServiceController {

    private static LocationSendServiceController sInstance = new LocationSendServiceController();


    // Private constructor
    private LocationSendServiceController() {
        mAlarmManager = (AlarmManager) App.getContext().getSystemService(Context.ALARM_SERVICE);
    }


    // Factory method
    public static LocationSendServiceController get() {
        if(sInstance == null) sInstance = new LocationSendServiceController();
        return sInstance;
    }


    // --------------------------- Member variables

    private AlarmManager mAlarmManager;


    // --------------------------- Controller interface

    /**
     * Set the alarm
     */
    public void setAlarm() {
        this.cancelAlarm();
        mAlarmManager.set(                                                                          // one-time alarm; will be rescheduled from the service itself
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + (1000 * Constants.LOCATION_SEND_INTERVAL),   // X sec from now
            LocationSendService.getPendingIntent()
        );
    }


    /**
     * Cancel the alarm
     */
    public void cancelAlarm() {
        mAlarmManager.cancel(LocationSendService.getPendingIntent());
    }

}
