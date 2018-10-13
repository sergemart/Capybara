package com.github.sergemart.mobile.capybara.exceptions;

public class FirebaseDatabaseException extends RuntimeException {

    public FirebaseDatabaseException() {
        super();
    }


    public FirebaseDatabaseException(String message) {
        super(message);
    }


    public FirebaseDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseDatabaseException(Throwable cause) {
        super(cause);
    }

}
