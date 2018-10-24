package com.github.sergemart.mobile.capybara;

import android.Manifest;


public final class Constants {

    // -----------------------

    public static final boolean APP_MODE_MAJOR = true;
    public static final boolean APP_MODE_MINOR = false;


    // ----------------------- Hard-coded strings

    public static final String DEFAULT_USERNAME = "anonymous";


    // ----------------------- Request codes

    public static final int REQUEST_CODE_SIGN_IN = 7001;
    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 7002;
    public static final int REQUEST_CODE_DIALOG_FRAGMENT = 7003;


    // ----------------------- Data keys and predefined values

    public static final String KEY_LOCATION = "location";
    static final String KEY_LOCATION_LAT = "locationLat";
    static final String KEY_LOCATION_LON = "locationLon";
    static final String KEY_LOCATION_TIME = "locationTime";
    public static final String KEY_DEVICE_TOKEN = "deviceToken";
    public static final String KEY_FAMILY_MEMBER_EMAIL = "familyMemberEmail";
    public static final String KEY_INVITEE_EMAIL = "inviteeEmail";
    public static final String KEY_INVITING_EMAIL = "invitingEmail";
    public static final String KEY_MESSAGE_TYPE = "messageType";
    public static final String MESSAGE_TYPE_INVITE = "invite";
    public static final String MESSAGE_TYPE_ACCEPT_INVITE = "acceptInvite";


    // ----------------------- Permissions

    public static final String[] LOCATION_PERMISSIONS = new String[]{
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    };


}
