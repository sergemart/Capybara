package com.github.sergemart.mobile.capybara.data.events;

public class GenericEvent<T> {

    // Private constructor
    private GenericEvent(Result result) {
        mResult = result;
    }


    // Factory method
    public static <T> GenericEvent<T> of(Result result) {
        return new GenericEvent<>(result);
    }


    // --------------------------- Member variables

    private Result mResult;
    private T mData;
    private Throwable mException;


    // --------------------------- Getters/ setters


    public Result getResult() {
        return mResult;
    }


    public T getData() {
        return mData;
    }


    public GenericEvent setData(T data) {
        mData = data;
        return this;
    }


    public Throwable getException() {
        return mException;
    }


    public GenericEvent setException(Throwable exception) {
        mException = exception;
        return this;
    }

}
