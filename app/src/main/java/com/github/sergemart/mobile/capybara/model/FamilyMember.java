package com.github.sergemart.mobile.capybara.model;

import android.location.Location;


public class FamilyMember {

    private String email;
    private Location location;


    // --------------------------- Getters/ setters

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public Location getLocation() {
        return location;
    }


    public void setLocation(Location location) {
        this.location = location;
    }
}
