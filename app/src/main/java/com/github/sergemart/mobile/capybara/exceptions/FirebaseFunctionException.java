package com.github.sergemart.mobile.capybara.exceptions;

public class FirebaseFunctionException extends RuntimeException {

    public FirebaseFunctionException() {
        super();
    }


    public FirebaseFunctionException(String message) {
        super(message);
    }


    public FirebaseFunctionException(String message, Throwable cause) {
        super(message, cause);
    }


    public FirebaseFunctionException(Throwable cause) {
        super(cause);
    }

}
