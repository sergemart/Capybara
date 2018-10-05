package com.github.sergemart.mobile.capybara;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;


public class App extends Application {

    @SuppressLint("StaticFieldLeak")                                                                // ok for the application context
    private static Context sContext;
    private static boolean sFinishOnReturnToInitialGraphEnabled;


    // --------------------------- Override application event handlers

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = super.getApplicationContext();
        sFinishOnReturnToInitialGraphEnabled = false;
    }


    // --------------------------- Static getters/ setters

    /**
     * Get the application context. Make sure that all the calls is occurred after the application onCreate()
     */
    public static Context getContext() {
        return sContext;
    }


    /**
     * @return if false, indicates that return to the initial graph is disabled
     */
    public static boolean finishOnReturnToInitialGraphEnabled() {
        return sFinishOnReturnToInitialGraphEnabled;
    }


    /**
     * @param finishOnReturnToInitialGraphEnabled if false, indicates that return to the initial graph is disabled
     */
    public static void setFinishOnReturnToInitialGraphEnabled(boolean finishOnReturnToInitialGraphEnabled) {
        App.sFinishOnReturnToInitialGraphEnabled = finishOnReturnToInitialGraphEnabled;
    }



}
