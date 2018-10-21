package com.github.sergemart.mobile.capybara.service;

import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.reactivex.disposables.CompositeDisposable;


public class CloudMessagingService
    extends FirebaseMessagingService
{

    private static final String TAG = CloudMessagingService.class.getSimpleName();

    private CompositeDisposable mDisposable;


    // --------------------------- Override service event handlers

    /**
     * Startup actions
     */
    @Override
    public void onCreate() {
        super.onCreate();

        this.initMemberVariables();
        this.setAttributes();
        this.setListeners();
    }


    /**
     * Invoked after app install when a token is first generated, and again if the token changes
     */
    @Override
    public void onNewToken(String newDeviceToken) {
        super.onNewToken(newDeviceToken);
        this.updateDeviceToken(newDeviceToken);
    }


    /**
     * Called when a message is received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    // --------------------------- Service controls

    /**
     * Init member variables
     */
    private void initMemberVariables() {
        mDisposable = new CompositeDisposable();
    }


    /**
     * Set attributes
     */
    private void setAttributes() {
    }


    /**
     * Set listeners to view-related events, containers and events
     */
    private void setListeners() {
    }


    // --------------------------- Use cases

    /**
     * Update the Firebase Messaging device token when received from the cloud
     */
    private void updateDeviceToken(String newDeviceToken) {
        if (BuildConfig.DEBUG) Log.d(TAG, "New device token received, calling repository update method.");
        CloudRepo.get().onTokenReceivedByMessagingService(newDeviceToken);
    }


}
