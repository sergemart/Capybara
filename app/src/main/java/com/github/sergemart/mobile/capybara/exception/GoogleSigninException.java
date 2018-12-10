package com.github.sergemart.mobile.capybara.exception;

public class GoogleSigninException extends RuntimeException {

    public GoogleSigninException() {
        super();
    }


    public GoogleSigninException(String message) {
        super(message);
    }


    public GoogleSigninException(String message, Throwable cause) {
        super(message, cause);
    }


    public GoogleSigninException(Throwable cause) {
        super(cause);
    }

}
