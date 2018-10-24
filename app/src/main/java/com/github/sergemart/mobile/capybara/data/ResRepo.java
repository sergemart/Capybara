package com.github.sergemart.mobile.capybara.data;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseSigninException;
import com.github.sergemart.mobile.capybara.exceptions.GoogleSigninException;
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


    // --------------------------- Repository interface: Sign-in retry dialog resources

    /**
     * @return Sign-in retry dialog icon resource id
     */
    public int getSignInRetryDialogIconR(Throwable cause) {
        return R.mipmap.ic_launcher; // TODO: Make dialog icons
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
        } else if (cause instanceof FirebaseSigninException) {
            resId = R.string.msg_firebase_client_connection_error;
        }
        return resId;
    }


    // --------------------------- Repository interface: Create family retry dialog resources

    /**
     * @return  Create family retry dialog icon resource id
     */
    public int getCreateFamilyRetryDialogIconR(Throwable cause) {
        return R.mipmap.ic_launcher; // TODO: Make dialog icons
    }


    /**
     * @return Create family retry dialog title resource id
     */
    public int getCreateFamilyRetryDialogTitleR(Throwable cause) {
        return R.string.title_google_signin_failed;
    }


    /**
     * @return Create family retry dialog message resource id
     */
    public int getCreateFamilyRetryDialogMessageR(Throwable cause) {
        return R.string.msg_google_unknown_error;
    }

}
