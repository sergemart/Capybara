package com.github.sergemart.mobile.capybara.data.model;

public class CurrentUser {

    private String mDeviceToken;
    private Integer mAppMode;
    private Boolean mIsFake;


    // --------------------------- Getters/ setters


    public String getDeviceToken() {
        return mDeviceToken;
    }


    public void setDeviceToken(String deviceToken) {
        this.mDeviceToken = deviceToken;
    }


    public Integer getAppMode() {
        return mAppMode;
    }


    public void setAppMode(Integer appMode) {
        this.mAppMode = appMode;
    }


    public Boolean isFake() {
        return mIsFake;
    }


    public void setFake(Boolean fake) {
        mIsFake = fake;
    }



}
