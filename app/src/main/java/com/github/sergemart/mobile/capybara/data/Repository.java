package com.github.sergemart.mobile.capybara.data;

import java.util.ArrayList;
import java.util.List;


// Singleton
public class Repository {

    private static Repository sInstance = new Repository();


    // Private constructor
    private Repository() {
        mItems = new ArrayList<>();
        mItems.add("1");
        mItems.add("2");
        mItems.add("3");
    }


    // Factory method
    public static Repository get() {
        if(sInstance == null) sInstance = new Repository();
        return sInstance;
    }


    // --------------------------- Member variables

    private List<Object> mItems;


    // --------------------------- Getters / setters

    public List<Object> getItems() {
        return mItems;
    }


}
