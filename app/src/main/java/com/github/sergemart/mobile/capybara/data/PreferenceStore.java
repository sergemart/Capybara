package com.github.sergemart.mobile.capybara.data;

import android.preference.PreferenceManager;

import com.github.sergemart.mobile.capybara.App;

import java.util.UUID;


public class PreferenceStore {

    private static final String PREF_APP_MODE = "appMode";


    // --------------------------- Public methods

    /**
     * Get an app mode stored in shared preferences
     */
    public static int getStoredAppMode() {
        return getIntPreference(PREF_APP_MODE);
    }


    /**
     * Store an app mode in shared preferences
     */
    public static void storeAppMode(int appMode) {
        storePreference(PREF_APP_MODE, appMode);
    }


    // --------------------------- Subroutines

    /**
     * Get a stored boolean preference
     */
    private static boolean getBooleanPreference(String preferenceKey) {
        return PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .getBoolean(preferenceKey, false);
    }


    /**
     * Get a stored boolean preference
     */
    private static int getIntPreference(String preferenceKey) {
        return PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .getInt(preferenceKey, -1);
    }


    /**
     * Get a stored string preference
     */
    private static String getStringPreference(String preferenceKey) {
        return PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .getString(preferenceKey, null);
    }


    /**
     * Store a boolean preference
     */
    private static void storePreference(String preferenceKey, boolean preference) {
        PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .edit()
            .putBoolean(preferenceKey, preference)
            .apply();
    }


    /**
     * Store a boolean preference
     */
    private static void storePreference(String preferenceKey, int preference) {
        PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .edit()
            .putInt(preferenceKey, preference)
            .apply();
    }


    /**
     * Store a string preference
     */
    private static void storePreference(String preferenceKey, String preference) {
        PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .edit()
            .putString(preferenceKey, preference)
            .apply();
    }

}


