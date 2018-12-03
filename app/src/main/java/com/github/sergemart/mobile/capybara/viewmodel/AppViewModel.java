package com.github.sergemart.mobile.capybara.viewmodel;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.events.Result;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseDbException;

import java.io.IOException;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;


// Singleton
public class AppViewModel {

    private static final String TAG = AppViewModel.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static AppViewModel sInstance = new AppViewModel();


    // Private constructor
    private AppViewModel() {
        // Init member variables
        mContext = App.getContext();
    }


    // Factory method
    public static AppViewModel get() {
        if(sInstance == null) sInstance = new AppViewModel();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Context mContext;


    // --------------------------- Use cases

    /**
     * Create a notification channels for Oreo
     */
    public void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.notification_channel_general_name);
            String description = mContext.getString(R.string.notification_channel_general_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_GENERAL, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }


    /**
     * Set a global Rx error handler to handle undelivered onErrors
     */
    public void setRxGlobalErrorHandler() {
        if (BuildConfig.DEBUG) {
            RxJavaPlugins.setErrorHandler(e -> {
                if (e instanceof UndeliverableException || e instanceof OnErrorNotImplementedException) e = e.getCause();
                if (e instanceof IOException)
                    return;                                                                         // skip, irrelevant network problem or API that throws on cancellation
                if (e instanceof InterruptedException)
                    return;                                                                         // skip, some blocking code was interrupted by a dispose call
                throw new RuntimeException(e);                                                      // do not intercept others when debugging
            });
        } else {
            RxJavaPlugins.setErrorHandler(e -> {
                if (e instanceof UndeliverableException || e instanceof OnErrorNotImplementedException) e = e.getCause();
                if (e instanceof IOException)
                    return;                                                                         // skip, irrelevant network problem or API that throws on cancellation
                if (e instanceof InterruptedException)
                    return;                                                                         // skip, some blocking code was interrupted by a dispose call
                Log.e(TAG, "Undeliverable exception: " + e.getMessage());                      // intercept others when in prod
            });
        }

    }

}
