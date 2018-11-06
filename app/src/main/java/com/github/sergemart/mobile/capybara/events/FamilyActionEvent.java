package com.github.sergemart.mobile.capybara.events;

public class FamilyActionEvent {

    // Private constructor
    private FamilyActionEvent(Result result) {
        mResult = result;
    }


    // Factory method
    public static FamilyActionEvent of(Result result) {
        return new FamilyActionEvent(result);
    }


    // --------------------------- Member variables

    private Result mResult;
    private Throwable mException;


    // --------------------------- Getters/ setters


    public Result getResult() {
        return mResult;
    }


    public Throwable getException() {
        return mException;
    }


    public FamilyActionEvent setException(Throwable exception) {
        mException = exception;
        return this;
    }


    // --------------------------- Inner classes: A result

    public enum Result {
        SUCCESS,
        NO_FAMILY,
        MORE_THAN_ONE_FAMILY,
        BACKEND_ERROR
    }
}
