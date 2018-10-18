package com.github.sergemart.mobile.capybara.exceptions;

public class FirebaseSigninException extends RuntimeException {

    public FirebaseSigninException() {
        super();
    }


    public FirebaseSigninException(String message) {
        super(message);
    }


    public FirebaseSigninException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseSigninException(Throwable cause) {
        super(cause);
    }

}
