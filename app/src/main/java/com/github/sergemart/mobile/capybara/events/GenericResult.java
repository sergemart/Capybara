package com.github.sergemart.mobile.capybara.events;

public enum GenericResult {

    SUCCESS,
    FAILURE;


    // --------------------------- Member variables

    private Throwable mException;


    // --------------------------- Getters/ setters

    public GenericResult setException(Throwable e) {
        mException = e;
        return this;
    }


    public Throwable getException() {
        return mException;
    }
}
