package com.github.sergemart.mobile.capybara.exceptions;

public class FirebaseConnectionException extends RuntimeException {

    public FirebaseConnectionException() {
        super();
    }


    public FirebaseConnectionException(String message) {
        super(message);
    }


    public FirebaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseConnectionException(Throwable cause) {
        super(cause);
    }

}
