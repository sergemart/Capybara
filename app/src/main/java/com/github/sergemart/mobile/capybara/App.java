package com.github.sergemart.mobile.capybara;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.viewmodel.AppViewModel;

import java.lang.ref.WeakReference;

import androidx.multidex.MultiDexApplication;


public class App extends MultiDexApplication {

    private static final String TAG = App.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // ok for the application context
    private static Context sContext;
    private static WeakReference<Throwable> sLastFatalException;


    // --------------------------- Override application event handlers

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        sContext = super.getApplicationContext();

        // App start-up actions
        AppViewModel.get().setRxGlobalErrorHandler();
        AuthService.get().getTokenAsync();                                                            // make sense for non-initial start-ups
        AppViewModel.get().createNotificationChannels();
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
