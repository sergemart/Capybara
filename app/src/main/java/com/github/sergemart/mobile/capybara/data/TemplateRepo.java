package com.github.sergemart.mobile.capybara.data;

import java.util.ArrayList;
import java.util.List;


// Singleton
public class TemplateRepo {

    private static TemplateRepo sInstance = new TemplateRepo();


    // Private constructor
    private TemplateRepo() {
        mItems = new ArrayList<>();
        mItems.add("1");
        mItems.add("2");
        mItems.add("3");
    }


    // Factory method
    public static TemplateRepo get() {
        if(sInstance == null) sInstance = new TemplateRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private List<Object> mItems;


    // --------------------------- Getters / setters

    public List<Object> getItems() {
        return mItems;
    }


}
