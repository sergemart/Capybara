package com.github.sergemart.mobile.capybara.data.maintain;

// Singleton
public class FirestoreMaintainer {

    private static FirestoreMaintainer sInstance = new FirestoreMaintainer();


    // Private constructor
    private FirestoreMaintainer() {
    }


    // Factory method
    public static FirestoreMaintainer get() {
        if(sInstance == null) sInstance = new FirestoreMaintainer();
        return sInstance;
    }

}
