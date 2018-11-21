package com.github.sergemart.mobile.capybara;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.github.sergemart.mobile.capybara.data.CloudRepo;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

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
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        sContext = super.getApplicationContext();

        if (!BuildConfig.DEBUG) {                                                                   // do not intercept when debugging
            RxJavaPlugins.setErrorHandler(e -> {
                if (e instanceof UndeliverableException) e = e.getCause();
                if (e instanceof IOException)
                    return;                                                                         // skip, irrelevant network problem or API that throws on cancellation
                if (e instanceof InterruptedException)
                    return;                                                                         // skip, some blocking code was interrupted by a dispose call
                Log.e(TAG, "Undeliverable exception: " + e.getMessage());
            });
        }

        // App start-up actions
        CloudRepo.get().getTokenAsync();                                                            // make sense for non-initial start-ups
        this.createNotificationChannels();
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


    // --------------------------- Use cases

    /**
     * Create a notification channels for Oreo
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_general_name);
            String description = getString(R.string.notification_channel_general_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_GENERAL, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

}
