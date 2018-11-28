package com.github.sergemart.mobile.capybara.data.model;

import android.graphics.Bitmap;


public class ContactData {

    private String id;
    private String name;
    private String email;
    private Bitmap photo;
    private int position;
    private int inviteSendResult;


    // --------------------------- Getters/ setters

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public Bitmap getPhoto() {
        return photo;
    }


    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }


    public int getPosition() {
        return position;
    }


    public void setPosition(int position) {
        this.position = position;
    }


    public int getInviteSendResult() {
        return inviteSendResult;
    }


    public void setInviteSendResult(int inviteSendResult) {
        this.inviteSendResult = inviteSendResult;
    }
}
