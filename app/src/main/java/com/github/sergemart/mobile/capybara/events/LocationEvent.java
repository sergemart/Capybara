package com.github.sergemart.mobile.capybara.events;

import android.location.Location;


public class LocationEvent {

    // Private constructor
    private LocationEvent(Result result) {
        mResult = result;
    }


    // Factory method
    public static LocationEvent of(Result result) {
        return new LocationEvent(result);
    }


    // --------------------------- Member variables

    private Result mResult;
    private Location mLocation;
    private String mSenderEmail;
    private Throwable mException;


    // --------------------------- Getters/ setters


    public Result getResult() {
        return mResult;
    }


    public Location getLocation() {
        return mLocation;
    }


    public LocationEvent setLocation(Location location) {
        mLocation = location;
        return this;
    }


    public String getSenderEmail() {
        return mSenderEmail;
    }


    public LocationEvent setSenderEmail(String senderEmail) {
        mSenderEmail = senderEmail;
        return this;
    }


    public Throwable getException() {
        return mException;
    }


    public LocationEvent setException(Throwable exception) {
        mException = exception;
        return this;
    }


}
