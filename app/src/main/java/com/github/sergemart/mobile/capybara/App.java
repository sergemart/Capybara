package com.github.sergemart.mobile.capybara;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;


public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // ok for the application context
    private static Context sContext;
    private static WeakReference<Throwable> sLastFatalException;


    // --------------------------- Override application event handlers

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = super.getApplicationContext();

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) e = e.getCause();
            if (e instanceof IOException) return;                                                   // skip, irrelevant network problem or API that throws on cancellation
            if (e instanceof InterruptedException) return;                                          // skip, some blocking code was interrupted by a dispose call
            if (BuildConfig.DEBUG) Log.e(TAG, "Undeliverable exception: " + e.getMessage());
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
     * @return A weak reference to the last fatal exception, which is causing the app exit
     */
    public static WeakReference<Throwable> getLastFatalException() {
        return sLastFatalException;
    }


    /**
     * @param lastFatalException A last fatal exception causing the app exit
     */
    public static void setLastFatalException(WeakReference<Throwable> lastFatalException) {
        sLastFatalException = lastFatalException;
    }


}
