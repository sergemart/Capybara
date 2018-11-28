package com.github.sergemart.mobile.capybara.model;

import android.graphics.Bitmap;
import android.location.Location;


public class FamilyMember {

    private String name;
    private Bitmap photo;
    private String email;
    private Location location;


    // --------------------------- Getters/ setters

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Bitmap getPhoto() {
        return photo;
    }


    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }


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
