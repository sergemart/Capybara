package com.github.sergemart.mobile.capybara.data;

import com.github.sergemart.mobile.capybara.R;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;


// Singleton
public class ResRepo {

    private static ResRepo sInstance = new ResRepo();


    // Private constructor
    private ResRepo() {
    }


    // Factory method
    public static ResRepo get() {
        if(sInstance == null) sInstance = new ResRepo();
        return sInstance;
    }


    // --------------------------- Repository interface

    /**
     * @return Retry Sign-In Dialog icon resource id
     */
    public int getSigninRetryDialogIconR(Throwable cause) {
        return R.mipmap.ic_launcher; // TODO: Make dialog icons
    }


    /**
     * @return Retry Sign-In Dialog title resource id
     */
    public int getSigninRetryDialogTitleR(Throwable cause) {
        int resId = R.string.title_google_signin_failed;

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
        return resId;
    }


    /**
     * @return Retry Sign-In Dialog message resource id
     */
    public int getSigninRetryDialogMessageR(Throwable cause) {
        int resId = R.string.msg_google_unknown_error;
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
        return resId;
    }

}
