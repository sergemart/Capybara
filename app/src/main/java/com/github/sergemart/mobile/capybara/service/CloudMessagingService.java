package com.github.sergemart.mobile.capybara.service;

import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.MessagingServiceRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.events.GenericResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

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

        mDisposable = new CompositeDisposable();
    }


    /**
     * Invoked after app install when a token is first generated, and again if the token changes
     */
    @Override
    public void onNewToken(String newDeviceToken) {
        super.onNewToken(newDeviceToken);
        this.notifyOnNewDeviceToken(newDeviceToken);
    }


    /**
     * Called when a message is received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Message received; processing");
        if (remoteMessage.getData() == null || remoteMessage.getData().size() == 0) {
            if (BuildConfig.DEBUG) Log.d(TAG, "The message has no data; skipping");
            return;
        }
        Map<String, String> messageData = remoteMessage.getData();
        if (messageData == null || messageData.size() == 0) {
            if (BuildConfig.DEBUG) Log.d(TAG, "The message has empty data; skipping");
            return;
        }
        switch (Objects.requireNonNull(messageData.get(Constants.KEY_MESSAGE_TYPE))) {
            case Constants.MESSAGE_TYPE_INVITE:                                                     // a message is an invite
                this.notifyOnInvite(messageData.get(Constants.KEY_INVITING_EMAIL));
                break;
            case Constants.MESSAGE_TYPE_ACCEPT_INVITE:                                              // a message is an invite acceptance
                this.notifyOnAcceptInvite(messageData.get(Constants.KEY_INVITEE_EMAIL));
                break;
            case Constants.MESSAGE_TYPE_LOCATION:                                                   // a message is a location notification
//                this.notifyOnLocation(messageData.get(Constants.KEY_LOCATION));
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unknown message type; skipping");
        }

    }


    // --------------------------- Use cases

    /**
     * Notify the app on the new Firebase Messaging device token when received one from the cloud
     */
    private void notifyOnNewDeviceToken(String newDeviceToken) {
        if (BuildConfig.DEBUG) Log.d(TAG, "New device token received, calling repository update method.");
        CloudRepo.get().updateDeviceToken(newDeviceToken);
    }


    /**
     * Notify about a received invite
     */
    private void notifyOnInvite(String invitingEmail) {
        if (invitingEmail == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Inviting email is null; skipping");
            return;
        }
        if (!PreferenceStore.getStoredIsAppModeSet()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "App mode is not set; skipping");
            return;
        }
        if (PreferenceStore.getStoredAppMode() != Constants.APP_MODE_MINOR) {
            if (BuildConfig.DEBUG) Log.e(TAG, "App mode should be 'MINOR' to process the message; skipping");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "An invite message received, emitting a corresponding event");
        MessagingServiceRepo.get().getInviteReceivedSubject().onNext(GenericResult.SUCCESS.setData(invitingEmail));
    }


    /**
     * Notify about a received invite
     */
    private void notifyOnAcceptInvite(String inviteeEmail) {
        if (inviteeEmail == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Invitee email is null; skipping");
            return;
        }
        if (!PreferenceStore.getStoredIsAppModeSet()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "App mode is not set; skipping");
            return;
        }
        if (PreferenceStore.getStoredAppMode() != Constants.APP_MODE_MAJOR) {
            if (BuildConfig.DEBUG) Log.e(TAG, "App mode should be 'MAJOR' to process the message; skipping");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "An invite acceptance message received, emitting a corresponding event");
        MessagingServiceRepo.get().getAcceptInviteReceivedSubject().onNext(GenericResult.SUCCESS.setData(inviteeEmail));
    }


}
