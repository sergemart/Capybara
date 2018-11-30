package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;


// Singleton
public class DeviceTokenRepo {

    private static DeviceTokenRepo sInstance = new DeviceTokenRepo();


    // Private constructor
    private DeviceTokenRepo() {
    }


    // Factory method
    public static DeviceTokenRepo get() {
        if(sInstance == null) sInstance = new DeviceTokenRepo();
        return sInstance;
    }


    // --------------------------- The interface

    /**
     * Get a current device token
     */
    public String getCurrentDeviceToken() {
        return AuthService.get().getCurrentDeviceToken();
    }


    /**
     * Async update the device token in the app and on the backend
     */
    public Observable<GenericEvent> updateDeviceToken(String deviceToken) {
        AuthService.get().setCurrentDeviceToken(deviceToken);                                       // update the app data
        Map<String, Object> userData = new HashMap<>();                                             // the mapping
        userData.put(Constants.KEY_DEVICE_TOKEN, deviceToken);
        return FirestoreService.get().updateUserAsync(userData);                                    // async update the backend data
    }


}
