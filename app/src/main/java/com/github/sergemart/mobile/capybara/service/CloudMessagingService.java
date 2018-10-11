package com.github.sergemart.mobile.capybara.service;

import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class CloudMessagingService
    extends FirebaseMessagingService
{

    private static final String TAG = CloudMessagingService.class.getSimpleName();


    // --------------------------- Override service event handlers

    /**
     *
     */
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        if (BuildConfig.DEBUG) Log.d(TAG, s);
    }


    /**
     *
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
