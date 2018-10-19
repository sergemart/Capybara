package com.github.sergemart.mobile.capybara.events;

public enum CreateFamilyResult {

    CREATED,
    EXIST,
    EXIST_MORE_THAN_ONE,
    BACKEND_ERROR;


    // --------------------------- Member variables

    private Throwable mException;
    private String mFamilyUid;


    // --------------------------- Getters/ setters

    public CreateFamilyResult setException(Throwable e) {
        mException = e;
        return this;
    }


    public Throwable getException() {
        return mException;
    }


    public String getFamilyUid() {
        return mFamilyUid;
    }


    public CreateFamilyResult setFamilyUid(String familyUid) {
        mFamilyUid = familyUid;
        return this;
    }

}
