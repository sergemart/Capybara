package com.github.sergemart.mobile.capybara;

import android.Manifest;


@SuppressWarnings("WeakerAccess")
public final class Constants {

    // ----------------------- Hard-coded values

    public static final int APP_MODE_MAJOR = 1;
    public static final int APP_MODE_MINOR = 2;
    public static final String DEFAULT_USERNAME = "anonymous";
    public static final String CLOUD_MESSAGING_LOCATION_PROVIDER = "cloudMessagingProvider";
    public static final int INVITE_NONE = 0;
    public static final int INVITE_SENT = 1;
    public static final int INVITE_NOT_SENT = 2;
    public static final int LOCATION_REQUEST_INTERVAL = 10 * 1000;                                  // milliseconds
    public static final float MAP_MAX_ZOOM = 18;
    public static final String DB_NAME = "capybara.db";
    public static final String NOTIFICATION_CHANNEL_GENERAL = "com.github.sergemart.mobile.capybara.NOTIFICATION_CHANNEL_GENERAL"; // Notification channel ID (since Oreo)
    public static final String SELECTION_ID = "selection_id";                                       // used by the RecyclerView Selection Library


    // ----------------------- App preference names

    public static final String PREF_APP_MODE = "appMode";
    public static final String PREF_FAMILY_CREATED = "familyCreated";
    public static final String PREF_FAMILY_JOINED = "familyJoined";
    public static final String PREF_BACKEND_VERSION = "backendVersion";


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
    public static final int REQUEST_CODE_NOTIFICATION_CONTENT = 7060;


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


    // ----------------------- Data keys and JSON field names

    public static final String KEY_LOCATION = "location";
    public static final String KEY_LOCATION_LAT = "locationLat";
    public static final String KEY_LOCATION_LON = "locationLon";
    public static final String KEY_LOCATION_TIME = "locationTime";
    public static final String KEY_DEVICE_TOKEN = "deviceToken";
    public static final String KEY_APP_MODE = "appMode";
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


    // ----------------------- Intent actions

    public static final String INTENT_ACTION_SEND_LOCATION = "com.github.sergemart.mobile.capybara.SEND_LOCATION";


    // ----------------------- Firestore collection, document and field names

    public static final String FIRESTORE_COLLECTION_SYSTEM = "system";
    public static final String FIRESTORE_DOCUMENT_DATABASE = "database";
    public static final String FIRESTORE_FIELD_VERSION = "version";
    public static final String FIRESTORE_COLLECTION_USERS = "users";
    public static final String FIRESTORE_COLLECTION_FAMILIES = "families";
    public static final String FIRESTORE_FIELD_CREATOR = "creator";
    public static final String FIRESTORE_FIELD_MEMBERS = "members";


    // ----------------------- Permissions

    public static final String[] LOCATION_PERMISSIONS = new String[]{
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    public static final String[] CONTACTS_PERMISSIONS = new String[]{
        Manifest.permission.READ_CONTACTS,
    };


    // ----------------------- Testing

    public static final String TEST_USER_1_EMAIL = "capybara.test.dummy.1@gmail.com";
    public static final String TEST_USER_2_EMAIL = "capybara.test.dummy.2@gmail.com";
    public static final String TEST_USER_3_EMAIL = "capybara.test.dummy.3@gmail.com";
    public static final String TEST_USER_PASSWORD = "c@pyb@ra";

}
