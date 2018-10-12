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
     * Set listeners to widgets, containers and events
     */
    private void setListeners() {
        // Set a listener to the "DEVICE TOKEN RECEIVED" event
        mDisposable.add(CloudRepo.get().getGetDeviceTokenSubject()
            .subscribe(event -> this.publishDeviceToken()) // TODO: Implement onError
        );
    }


    // --------------------------- Use cases

    /**
     * Update the Firebase Messaging device token when received from the cloud
     */
    private void updateDeviceToken(String newDeviceToken) {
        CloudRepo.get().updateDeviceToken(newDeviceToken);
        if (BuildConfig.DEBUG) Log.d(TAG, "New device token released, updating.");
    }


    /**
     * Publish the Firebase Messaging device token
     */
    private void publishDeviceToken() {
        CloudRepo.get().publishDeviceTokenAsync();
        if (BuildConfig.DEBUG) Log.d(TAG, "New device token released, publishing.");
    }

}
