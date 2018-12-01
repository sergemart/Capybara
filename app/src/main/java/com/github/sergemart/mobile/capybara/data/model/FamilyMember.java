package com.github.sergemart.mobile.capybara.data.model;

import android.graphics.Bitmap;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "family_member")
public class FamilyMember {

    @PrimaryKey
    @NonNull
    private String extId = "";
    private String name;
    private String type;
    @Ignore
    private Bitmap photo;
    private String email;
    @Ignore
    private Location location;


    // --------------------------- Getters/ setters

    @NonNull
    public String getExtId() {
        return extId;
    }


    public void setExtId(@NonNull String extId) {
        this.extId = extId;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
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
