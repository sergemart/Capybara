package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.data.model.CurrentUser;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;


// Singleton
public class CurrentUserRepo {

    private static CurrentUserRepo sInstance;


    // Private constructor
    private CurrentUserRepo() {
    }


    // Factory method
    public static CurrentUserRepo get() {
        if(sInstance == null) sInstance = new CurrentUserRepo();
        return sInstance;
    }


    // --------------------------- The interface


    /**
     * Sync read the current user record
     */
    public CurrentUser readSync() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setAppMode(PreferenceStore.getAppMode());
        currentUser.setDeviceToken(AuthService.get().getCurrentDeviceToken());
        return currentUser;
    }


    /**
     * Async update the current user record
     */
    public Completable updateAsync(CurrentUser currentUser) {
        Map<String, Object> userData = new HashMap<>();                                             // the mapping
        if (currentUser.getAppMode() != null) userData.put(Constants.KEY_APP_MODE, currentUser.getAppMode());
        if (currentUser.getDeviceToken() != null) {
            AuthService.get().setCurrentDeviceToken(currentUser.getDeviceToken());                  // update the app data
            userData.put(Constants.KEY_DEVICE_TOKEN, currentUser.getDeviceToken());
        }
        if (currentUser.isFake() != null) userData.put(Constants.KEY_IS_FAKE, currentUser.isFake());
        return FirestoreService.get().updateCurrentUserLocalReplicaAsync(userData);                 // async update the backend data
    }


}
