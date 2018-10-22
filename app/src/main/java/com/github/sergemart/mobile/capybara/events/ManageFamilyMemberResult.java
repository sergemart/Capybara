package com.github.sergemart.mobile.capybara.events;

public enum ManageFamilyMemberResult {

    SUCCESS,
    NO_FAMILY,
    MORE_THAN_ONE_FAMILY,
    BACKEND_ERROR;


    // --------------------------- Member variables

    private Throwable mException;


    // --------------------------- Getters/ setters

    public ManageFamilyMemberResult setException(Throwable e) {
        mException = e;
        return this;
    }


    public Throwable getException() {
        return mException;
    }

}
