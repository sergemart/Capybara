package com.github.sergemart.mobile.capybara.events;

public enum GenericResult {

    SUCCESS,
    FAILURE;


    // --------------------------- Member variables

    private Object mData;
    private Throwable mException;


    // --------------------------- Getters/ setters

    public GenericResult setData(Object data) {
        mData = data;
        return this;
    }


    public Object getData() {
        return mData;
    }


    public GenericResult setException(Throwable e) {
        mException = e;
        return this;
    }


    public Throwable getException() {
        return mException;
    }
}
