package com.github.sergemart.mobile.capybara.data;

import android.preference.PreferenceManager;

import com.github.sergemart.mobile.capybara.App;

import java.util.UUID;


public class PreferenceStore {

    private static final String PREF_IS_APP_MODE_SET = "isAppModeSet";
    private static final String PREF_APP_MODE = "appMode";
    private static final String PREF_FAMILY_UUID = "familyUuid";


    // --------------------------- Public methods

    /**
     * Get an app mode set flag stored in shared preferences
     */
    public static boolean getStoredIsAppModeSet() {
        return getBooleanPreference(PREF_IS_APP_MODE_SET);
    }


    /**
     * Store an app mode set flag in shared preferences
     */
    public static void storeIsAppModeSet(boolean isAppModeSet) {
        storePreference(PREF_IS_APP_MODE_SET, isAppModeSet);
    }


    /**
     * Get an app mode stored in shared preferences
     */
    public static Boolean getStoredAppMode() {
        return getBooleanPreference(PREF_APP_MODE);
    }


    /**
     * Store an app mode in shared preferences
     */
    public static void storeAppMode(boolean appMode) {
        storePreference(PREF_APP_MODE, appMode);
    }


    /**
     * Get a family UUID stored in shared preferences
     */
    public static UUID getFamilyUuid() {
        return UUID.fromString(getStringPreference(PREF_FAMILY_UUID));
    }


    /**
     * Store a family UUID in shared preferences
     */
    public static void storeFamilyUuid(UUID familyUuid) {
        storePreference(PREF_FAMILY_UUID, familyUuid.toString());
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


