package com.github.sergemart.mobile.capybara.data;

import android.preference.PreferenceManager;

import com.github.sergemart.mobile.capybara.App;

import java.util.UUID;


public class PreferenceStore {

    private static final String PREF_APP_MODE = "appMode";
    private static final String PREF_FAMILY_CREATED = "familyCreated";
    private static final String PREF_FAMILY_JOINED = "familyJoined";


    // --------------------------- Repository interface

    /**
     * Get an app mode stored in shared preferences
     */
    public static int getAppMode() {
        return getIntPreference(PREF_APP_MODE);
    }


    /**
     * Store an app mode in shared preferences
     */
    public static void storeAppMode(int value) {
        storePreference(PREF_APP_MODE, value);
    }


    /**
     * Get a 'Family Created' flag stored in shared preferences
     */
    public static boolean getFamilyCreated() {
        return getBooleanPreference(PREF_FAMILY_CREATED);
    }


    /**
     * Store a 'Family Created' flag in shared preferences
     */
    public static void storeFamilyCreated(boolean value) {
        storePreference(PREF_FAMILY_CREATED, value);
    }


    /**
     * Get a 'Family Joined' flag stored in shared preferences
     */
    public static boolean getFamilyJoined() {
        return getBooleanPreference(PREF_FAMILY_JOINED);
    }


    /**
     * Store a 'Family Joined' flag in shared preferences
     */
    public static void storeFamilyJoined(boolean value) {
        storePreference(PREF_FAMILY_JOINED, value);
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


