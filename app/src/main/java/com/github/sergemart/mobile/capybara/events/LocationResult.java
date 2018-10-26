package com.github.sergemart.mobile.capybara.events;

import android.location.Location;


public enum LocationResult {

    SUCCESS,
    FAILURE;


    // --------------------------- Member variables

    private Location mLocation;
    private String mSenderEmail;
    private Throwable mException;


    // --------------------------- Getters/ setters

    public LocationResult setLocation(Location location) {
        mLocation = location;
        return this;
    }


    public Location getLocation() {
        return mLocation;
    }


    public LocationResult setSenderEmail(String senderEmail) {
        mSenderEmail = senderEmail;
        return this;
    }


    public String getSenderEmail() {
        return mSenderEmail;
    }


    public LocationResult setException(Throwable e) {
        mException = e;
        return this;
    }


    public Throwable getException() {
        return mException;
    }
}
