package com.github.sergemart.mobile.capybara;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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
}
