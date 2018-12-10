package com.github.sergemart.mobile.capybara.exception;

public class FirebaseDbException extends RuntimeException {

    public FirebaseDbException() {
        super();
    }


    public FirebaseDbException(String message) {
        super(message);
    }


    public FirebaseDbException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseDbException(Throwable cause) {
        super(cause);
    }

}
