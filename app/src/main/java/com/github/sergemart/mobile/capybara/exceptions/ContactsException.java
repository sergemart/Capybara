package com.github.sergemart.mobile.capybara.exceptions;

public class ContactsException extends RuntimeException {

    public ContactsException() {
        super();
    }


    public ContactsException(String message) {
        super(message);
    }


    public ContactsException(String message, Throwable cause) {
        super(message, cause);
    }


    public ContactsException(Throwable cause) {
        super(cause);
    }

}
