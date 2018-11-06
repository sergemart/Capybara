package com.github.sergemart.mobile.capybara.events;

import com.google.firebase.auth.FirebaseUser;


public class SignInEvent {

    // Private constructor
    private SignInEvent(Result result) {
        mResult = result;
    }


    // Factory method
    public static SignInEvent of(Result result) {
        return new SignInEvent(result);
    }


    // --------------------------- Member variables

    private Result mResult;
    private FirebaseUser mFirebaseUser;
    private Throwable mException;


    // --------------------------- Getters/ setters


    public Result getResult() {
        return mResult;
    }


    public FirebaseUser getFirebaseUser() {
        return mFirebaseUser;
    }


    public SignInEvent setFirebaseUser(FirebaseUser firebaseUser) {
        mFirebaseUser = firebaseUser;
        return this;
    }


    public Throwable getException() {
        return mException;
    }


    public SignInEvent setException(Throwable exception) {
        mException = exception;
        return this;
    }

}
