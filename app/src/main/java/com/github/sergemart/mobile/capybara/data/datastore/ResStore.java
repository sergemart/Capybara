package com.github.sergemart.mobile.capybara.data.datastore;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exception.FirebaseAuthException;
import com.github.sergemart.mobile.capybara.exception.GoogleSigninException;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;


// Singleton
public class ResStore {

    private static ResStore sInstance;


    // Private constructor
    private ResStore() {
    }


    // Factory method
    public static ResStore get() {
        if(sInstance == null) sInstance = new ResStore();
        return sInstance;
    }


    // --------------------------- The interface: Grant permission retry dialog resources

    /**
     * @return  Grant permission retry dialog icon resource id
     */
    public int getGrantPermissionRetryDialogIconR() {
        return R.mipmap.capybara_bighead; // TODO: Make dialog icons
    }


    /**
     * @return Grant permission retry dialog title resource id
     */
    public int getGrantPermissionRetryDialogTitleR() {
        return R.string.title_permission_not_granted;
    }


    /**
     * @return Grant permission retry dialog message resource id
     */
    public int getGrantPermissionRetryDialogMessageR() {
        return R.string.msg_permission_not_granted;
    }


    // --------------------------- The interface: Sign-in retry dialog resources

    /**
     * @return Sign-in retry dialog icon resource id
     */
    public int getSignInRetryDialogIconR(Throwable cause) {
        return R.mipmap.capybara_bighead; // TODO: Make dialog icons
    }


    /**
     * @return Sign-in retry dialog title resource id
     */
    public int getSignInRetryDialogTitleR(Throwable cause) {
        int resId = R.string.title_google_signin_failed;

        if (cause instanceof GoogleSigninException) {
            if (cause.getCause() instanceof ApiException) {
                ApiException apiException = (ApiException) cause.getCause();
                switch (apiException.getStatusCode()) {
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        resId = R.string.title_google_signin_canceled;
                        break;
                    default:
                        resId = R.string.title_google_signin_failed;
                }
            }
        }
        return resId;
    }


    /**
     * @return Sign-in retry dialog message resource id
     */
    public int getSignInRetryDialogMessageR(Throwable cause) {
        int resId = R.string.msg_google_unknown_error;
        if (cause instanceof GoogleSigninException) {
            if (cause.getCause() instanceof ApiException) {
                ApiException apiException = (ApiException) cause.getCause();
                switch (apiException.getStatusCode()) {
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        resId = R.string.msg_google_signin_canceled_by_user;
                        break;
                    default:
                        resId = R.string.msg_google_client_connection_error;
                }
            }
        } else if (cause instanceof FirebaseAuthException) {
            resId = R.string.msg_firebase_client_connection_error;
        }
        return resId;
    }


    // --------------------------- The interface: Create family retry dialog resources

    /**
     * @return  Create family retry dialog icon resource id
     */
    public int getCreateFamilyRetryDialogIconR(Throwable cause) {
        return R.mipmap.capybara_bighead; // TODO: Make dialog icons
    }


    /**
     * @return Create family retry dialog title resource id
     */
    public int getCreateFamilyRetryDialogTitleR(Throwable cause) {
        return R.string.title_create_family_failed;
    }


    /**
     * @return Create family retry dialog message resource id
     */
    public int getCreateFamilyRetryDialogMessageR(Throwable cause) {
        return R.string.msg_firebase_unknown_error;
    }


    // --------------------------- The interface: Join family retry dialog resources

    /**
     * @return  Join family retry dialog icon resource id
     */
    public int getJoinFamilyRetryDialogIconR(Throwable cause) {
        return R.mipmap.capybara_bighead; // TODO: Make dialog icons
    }


    /**
     * @return Join family retry dialog title resource id
     */
    public int getJoinFamilyRetryDialogTitleR(Throwable cause) {
        return R.string.title_join_family_failed;
    }


    /**
     * @return Join family retry dialog message resource id
     */
    public int getJoinFamilyRetryDialogMessageR(Throwable cause) {
        return R.string.msg_firebase_unknown_error;
    }


    // --------------------------- The interface: Upgrade backend retry dialog resources

    /**
     * @return  Upgrade backend retry dialog icon resource id
     */
    public int getUpgradeBackendRetryDialogIconR(Throwable cause) {
        return R.mipmap.capybara_bighead; // TODO: Make dialog icons
    }


    /**
     * @return Upgrade backend retry dialog title resource id
     */
    public int getUpgradeBackendRetryDialogTitleR(Throwable cause) {
        return R.string.title_join_family_failed;
    }


    /**
     * @return Upgrade backend retry dialog message resource id
     */
    public int getUpgradeBackendRetryDialogMessageR(Throwable cause) {
        return R.string.msg_firebase_unknown_error;
    }

}
