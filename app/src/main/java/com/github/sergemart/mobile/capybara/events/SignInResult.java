package com.github.sergemart.mobile.capybara.events;

import com.google.firebase.auth.FirebaseUser;


public enum SignInResult {

    SUCCESS,
    FAILURE;


    // --------------------------- Member variables

    private Throwable mException;
    private FirebaseUser mFirebaseUser;


    // --------------------------- Getters/ setters

    public SignInResult setException(Throwable e) {
        mException = e;
        return this;
    }


    public Throwable getException() {
        return mException;
    }



    public FirebaseUser getFirebaseUser() {
        return mFirebaseUser;
    }


    public SignInResult setFirebaseUser(FirebaseUser firebaseUser) {
        mFirebaseUser = firebaseUser;
        return this;
    }

}
