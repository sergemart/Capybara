package com.github.sergemart.mobile.capybara;

import android.Manifest;


public final class Constants {

    // ----------------------- Hard-coded values

    public static final int APP_MODE_MAJOR = 1;
    public static final int APP_MODE_MINOR = 2;
    public static final String DEFAULT_USERNAME = "anonymous";
    static final String CLOUD_MESSAGING_LOCATION_PROVIDER = "cloudMessagingProvider";
    public static final int INVITE_SENT = 1;
    public static final int INVITE_NOT_SENT = 2;
    public static final int LOCATION_REQUEST_INTERVAL = 10 * 1000;                                  // milliseconds
    public static final float MAP_MAX_ZOOM = 18;


    // ----------------------- Dialog tags

    public static final String TAG_GRANT_PERMISSION_RETRY_DIALOG = "grantPermissionRetryDialog";
    public static final String TAG_SIGN_IN_RETRY_DIALOG = "signInRetryDialog";
    public static final String TAG_CREATE_FAMILY_RETRY_DIALOG = "createFamilyRetryDialog";
    public static final String TAG_JOIN_FAMILY_RETRY_DIALOG = "joinFamilyRetryDialog";


    // ----------------------- Request codes

    public static final int REQUEST_CODE_SIGN_IN = 7010;
    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 7020;
    public static final int REQUEST_CODE_READ_CONTACTS_PERMISSIONS = 7030;
    public static final int REQUEST_CODE_DIALOG_FRAGMENT = 7040;
    static final int REQUEST_CODE_NOTIFICATION_CONTENT = 7060;


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


    // ----------------------- Data keys

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
    public static final String KEY_IS_IN_ACTION_MODE = "isInActionMode";


    // ----------------------- Message types

    public static final String MESSAGE_TYPE_INVITE = "invite";
    public static final String MESSAGE_TYPE_ACCEPT_INVITE = "acceptInvite";
    public static final String MESSAGE_TYPE_LOCATION = "location";
    public static final String MESSAGE_TYPE_LOCATION_REQUEST = "locationRequest";


    // ----------------------- Job IDs

    public static final int JOB_SEND_LOCATION = 1001;


    // ----------------------- Notification channel IDs (since Oreo)

    static final String NOTIFICATION_CHANNEL_GENERAL = "com.github.sergemart.mobile.capybara.NOTIFICATION_CHANNEL_GENERAL";


    // ----------------------- Intent actions

    public static final String INTENT_ACTION_SEND_LOCATION = "com.github.sergemart.mobile.capybara.SEND_LOCATION";


    // ----------------------- Permissions

    public static final String[] LOCATION_PERMISSIONS = new String[]{
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    public static final String[] CONTACTS_PERMISSIONS = new String[]{
        Manifest.permission.READ_CONTACTS,
    };


}
