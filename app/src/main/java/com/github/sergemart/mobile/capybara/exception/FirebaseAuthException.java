package com.github.sergemart.mobile.capybara.exception;

public class FirebaseAuthException extends RuntimeException {

    public FirebaseAuthException() {
        super();
    }


    public FirebaseAuthException(String message) {
        super(message);
    }


    public FirebaseAuthException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseAuthException(Throwable cause) {
        super(cause);
    }

}
