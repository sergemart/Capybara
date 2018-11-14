package com.github.sergemart.mobile.capybara.service;

import android.app.AlarmManager;
import android.content.Context;

import com.github.sergemart.mobile.capybara.App;


// Singleton
public class LocationSendAlarmController {

    private static LocationSendAlarmController sInstance = new LocationSendAlarmController();


    // Private constructor
    private LocationSendAlarmController() {
        mAlarmManager = (AlarmManager) App.getContext().getSystemService(Context.ALARM_SERVICE);
    }


    // Factory method
    public static LocationSendAlarmController get() {
        if(sInstance == null) sInstance = new LocationSendAlarmController();
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
        long scheduleTime = System.currentTimeMillis() + (1000 * 30);                               // 30 sec from now
        mAlarmManager.set(                                                                          // one-time alarm; will be rescheduled from the service itself
            AlarmManager.RTC_WAKEUP, // TODO: check ELAPSED
            scheduleTime,
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
