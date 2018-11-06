package com.github.sergemart.mobile.capybara.events;

public class CreateFamilyEvent {

    // Private constructor
    private CreateFamilyEvent(Result result) {
        mResult = result;
    }


    // Factory method
    public static CreateFamilyEvent of(Result result) {
        return new CreateFamilyEvent(result);
    }


    // --------------------------- Member variables

    private Result mResult;
    private String mFamilyUid;
    private Throwable mException;


    // --------------------------- Getters/ setters


    public Result getResult() {
        return mResult;
    }


    public String getFamilyUid() {
        return mFamilyUid;
    }


    public CreateFamilyEvent setFamilyUid(String familyUid) {
        mFamilyUid = familyUid;
        return this;
    }


    public Throwable getException() {
        return mException;
    }


    public CreateFamilyEvent setException(Throwable exception) {
        mException = exception;
        return this;
    }

}
