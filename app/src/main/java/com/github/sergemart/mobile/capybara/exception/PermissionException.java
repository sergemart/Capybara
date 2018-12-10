package com.github.sergemart.mobile.capybara.exception;

public class PermissionException extends RuntimeException {

    public PermissionException() {
        super();
    }


    public PermissionException(String message) {
        super(message);
    }


    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }


    public PermissionException(Throwable cause) {
        super(cause);
    }
}
