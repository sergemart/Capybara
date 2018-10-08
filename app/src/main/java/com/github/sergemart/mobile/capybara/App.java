package com.github.sergemart.mobile.capybara;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.SocketException;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;


public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // ok for the application context
    private static Context sContext;
    private static boolean sFinishOnReturnToInitialGraphEnabled;


    // --------------------------- Override application event handlers

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = super.getApplicationContext();
        sFinishOnReturnToInitialGraphEnabled = false;

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException)) {                                                       // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {                                                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
        });
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
