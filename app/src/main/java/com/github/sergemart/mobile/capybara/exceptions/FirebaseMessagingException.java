package com.github.sergemart.mobile.capybara.exceptions;

public class FirebaseMessagingException extends RuntimeException {

    public FirebaseMessagingException() {
        super();
    }


    public FirebaseMessagingException(String message) {
        super(message);
    }


    public FirebaseMessagingException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseMessagingException(Throwable cause) {
        super(cause);
    }

}
