package com.github.sergemart.mobile.capybara.exceptions;

public class LocationPermissionException extends RuntimeException {

    public LocationPermissionException() {
        super();
    }


    public LocationPermissionException(String message) {
        super(message);
    }


    public LocationPermissionException(String message, Throwable cause) {
        super(message, cause);
    }


    public LocationPermissionException(Throwable cause) {
        super(cause);
    }
}
