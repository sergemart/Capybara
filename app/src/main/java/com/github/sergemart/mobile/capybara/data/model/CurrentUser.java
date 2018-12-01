package com.github.sergemart.mobile.capybara.data.model;

public class CurrentUser {

    private String deviceToken;
    private Integer appMode;


    // --------------------------- Getters/ setters


    public String getDeviceToken() {
        return deviceToken;
    }


    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }


    public Integer getAppMode() {
        return appMode;
    }


    public void setAppMode(Integer appMode) {
        this.appMode = appMode;
    }
}
