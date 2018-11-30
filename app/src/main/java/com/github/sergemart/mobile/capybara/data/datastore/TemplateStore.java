package com.github.sergemart.mobile.capybara.data.datastore;

import java.util.ArrayList;
import java.util.List;


// Singleton
public class TemplateStore {

    private static TemplateStore sInstance = new TemplateStore();


    // Private constructor
    private TemplateStore() {
        mItems = new ArrayList<>();
        mItems.add("1");
        mItems.add("2");
        mItems.add("3");
    }


    // Factory method
    public static TemplateStore get() {
        if(sInstance == null) sInstance = new TemplateStore();
        return sInstance;
    }


    // --------------------------- Member variables

    private List<Object> mItems;


    // --------------------------- Getters / setters

    public List<Object> getItems() {
        return mItems;
    }


}
