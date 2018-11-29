package com.github.sergemart.mobile.capybara;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;

import com.github.sergemart.mobile.capybara.data.PreferenceRepo;
import com.github.sergemart.mobile.capybara.controller.activity.InitialMajorActivity;
import com.github.sergemart.mobile.capybara.controller.activity.InitialMinorActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


// Singleton
public class Tools {

    private static Tools sInstance = new Tools();


    // Private constructor
    private Tools() {
        mGson = new Gson();
    }


    // Factory method
    public static Tools get() {
        if(sInstance == null) sInstance = new Tools();
        return sInstance;
    }


    // --------------------------- Member variables

    private Gson mGson;


    // --------------------------- Tools

    /**
     * @param location Location
     * @return Location JSON
     */
    public String getLocationJson(Location location) {
        if (location == null) return null;
        Map<String, Object> locationMap = new HashMap<>();

        locationMap.put(Constants.KEY_LOCATION_LAT, String.valueOf( location.getLatitude() ));
        locationMap.put(Constants.KEY_LOCATION_LON, String.valueOf( location.getLongitude() ));
        locationMap.put(Constants.KEY_LOCATION_TIME, String.valueOf( location.getTime() ));

        return mGson.toJson(locationMap);
    }


    /**
     * @param locationJson Location JSON
     * @return Location
     */
    public Location getLocationFromJson(String locationJson) {
        if (locationJson == null || locationJson.isEmpty()) return null;

        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> locationMap = mGson.fromJson(locationJson, mapType);

        Location location = new Location(Constants.CLOUD_MESSAGING_LOCATION_PROVIDER);
        location.setLatitude(Double.valueOf(Objects.requireNonNull(locationMap.get(Constants.KEY_LOCATION_LAT))));
        location.setLongitude(Double.valueOf(Objects.requireNonNull(locationMap.get(Constants.KEY_LOCATION_LON))));
        location.setTime(Long.valueOf(Objects.requireNonNull(locationMap.get(Constants.KEY_LOCATION_TIME))));

        return location;
    }


    /**
     * Show a general notification
     */
    public void showGeneralNotification(String contentTitle, String contentText) {
        Intent intent;
        if (PreferenceRepo.getAppMode() == Constants.APP_MODE_MAJOR) {
            intent = InitialMajorActivity.newIntent(App.getContext());
        } else {
            intent = InitialMinorActivity.newIntent(App.getContext());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            App.getContext(),
            Constants.REQUEST_CODE_NOTIFICATION_CONTENT,
            intent,
            0
        );
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
            App.getContext(), Constants.NOTIFICATION_CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.icon_location_request)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)                                                                    // remove a notification on tap
        ;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
        notificationManager.notify(0, notificationBuilder.build());
    }

}
