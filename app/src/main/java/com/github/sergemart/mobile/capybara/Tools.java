package com.github.sergemart.mobile.capybara;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;


// Singleton
public class Tools {

    private static Tools sInstance = new Tools();


    // Private constructor
    private Tools() {
    }


    // Factory method
    public static Tools get() {
        if(sInstance == null) sInstance = new Tools();
        return sInstance;
    }


    // --------------------------- Tools

    public Map<String, Object> getJsonableLocation(Location location) {
        if (location == null) return null;
        Map<String, Object> result = new HashMap<>();

        result.put(Constants.KEY_LOCATION_LAT, String.valueOf( location.getLatitude() ));
        result.put(Constants.KEY_LOCATION_LON, String.valueOf( location.getLongitude() ));
        result.put(Constants.KEY_LOCATION_TIME, String.valueOf( location.getTime() ));

        return result;
    }

}
