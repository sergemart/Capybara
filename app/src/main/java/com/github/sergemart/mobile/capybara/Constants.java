package com.github.sergemart.mobile.capybara;

import android.Manifest;


public final class Constants {

    // -----------------------

    public static final boolean APP_MODE_MAJOR = true;
    public static final boolean APP_MODE_MINOR = false;


    // ----------------------- Hard-coded strings

    public static final String DEFAULT_USERNAME = "anonymous";
    static final String CLOUD_MESSAGING_LOCATION_PROVIDER = "cloudMessagingProvider";


    // ----------------------- Request codes

    public static final int REQUEST_CODE_SIGN_IN = 7001;
    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 7002;
    public static final int REQUEST_CODE_DIALOG_FRAGMENT = 7003;


    // ----------------------- Return codes

    public static final String RETURN_CODE_OK = "ok";
    public static final String RETURN_CODE_CREATED = "created";
    public static final String RETURN_CODE_DELETED = "deleted";
    public static final String RETURN_CODE_EXIST = "exist";
    public static final String RETURN_CODE_NO_FAMILY = "no_family";
    public static final String RETURN_CODE_MORE_THAN_ONE_FAMILY = "many_families";
    public static final String RETURN_CODE_SENT = "sent";
    public static final String RETURN_CODE_NOT_SENT = "not_sent";
    public static final String RETURN_CODE_ALL_SENT = "all_sent";
    public static final String RETURN_CODE_SOME_SENT = "some_sent";
    public static final String RETURN_CODE_NONE_SENT = "none_sent";


    // ----------------------- Data keys and predefined values

    public static final String KEY_LOCATION = "location";
    static final String KEY_LOCATION_LAT = "locationLat";
    static final String KEY_LOCATION_LON = "locationLon";
    static final String KEY_LOCATION_TIME = "locationTime";
    public static final String KEY_DEVICE_TOKEN = "deviceToken";
    public static final String KEY_FAMILY_MEMBER_EMAIL = "familyMemberEmail";
    public static final String KEY_INVITEE_EMAIL = "inviteeEmail";
    public static final String KEY_INVITING_EMAIL = "invitingEmail";
    public static final String KEY_SENDER_EMAIL = "senderEmail";
    public static final String KEY_MESSAGE_TYPE = "messageType";
    public static final String MESSAGE_TYPE_INVITE = "invite";
    public static final String MESSAGE_TYPE_ACCEPT_INVITE = "acceptInvite";
    public static final String MESSAGE_TYPE_LOCATION = "location";


    // ----------------------- Permissions

    public static final String[] LOCATION_PERMISSIONS = new String[]{
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    };


}
