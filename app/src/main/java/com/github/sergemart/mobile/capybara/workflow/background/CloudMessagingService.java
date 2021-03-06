package com.github.sergemart.mobile.capybara.workflow.background;

import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.Tools;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.FunctionsService;
import com.github.sergemart.mobile.capybara.data.datastore.GeoService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.events.LocationEvent;
import com.github.sergemart.mobile.capybara.data.model.CurrentUser;
import com.github.sergemart.mobile.capybara.data.repo.CurrentUserRepo;
import com.github.sergemart.mobile.capybara.data.repo.MessageRepo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

import io.reactivex.disposables.CompositeDisposable;

import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


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
        this.updateDeviceToken(newDeviceToken);
    }


    /**
     * Called when a message is received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Message received; processing");
        Map<String, String> messageData = remoteMessage.getData();
        if (messageData == null || messageData.size() == 0) {
            if (BuildConfig.DEBUG) Log.d(TAG, "The message has no data; skipping");
            return;
        }
        switch (Objects.requireNonNull(messageData.get(Constants.KEY_MESSAGE_TYPE))) {
            case Constants.MESSAGE_TYPE_INVITE:                                                     // a message is an invite
                this.notifyOnInvite(
                    messageData.get(Constants.KEY_INVITING_EMAIL)
                );
                break;
            case Constants.MESSAGE_TYPE_ACCEPT_INVITE:                                              // a message is an invite acceptance
                this.notifyOnInviteAccepted(
                    messageData.get(Constants.KEY_INVITEE_EMAIL)
                );
                break;
            case Constants.MESSAGE_TYPE_LOCATION:                                                   // a message is a location notification
                this.notifyOnLocation(
                    messageData.get(Constants.KEY_LOCATION),
                    messageData.get(Constants.KEY_SENDER_EMAIL)
                );
                break;
            case Constants.MESSAGE_TYPE_LOCATION_REQUEST:                                           // a message is a location request
                this.notifyOnLocationRequest(
                    messageData.get(Constants.KEY_SENDER_EMAIL)
                );
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unknown message type; skipping");
        }
    }


    /**
     * Instance clean-up actions
     */
    @Override
    public void onDestroy() {
        mDisposable.clear();
        super.onDestroy();
    }


    // --------------------------- Service lifecycle subroutines

    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        mDisposable.add(GeoService.get().getLocateMeSubject().subscribe(location -> {
            GeoService.get().stopLocationUpdates();
            FunctionsService.get().sendLocationAsync(location);
        }));

    }


    // --------------------------- Use cases

    /**
     * Update the app and the backend with the new Firebase Messaging device token
     */
    private void updateDeviceToken(String newDeviceToken) {
        if (BuildConfig.DEBUG) Log.d(TAG, "New device token received, updating");

        String currentDeviceToken = CurrentUserRepo.get().readSync().getDeviceToken();
        if (newDeviceToken.equals(currentDeviceToken)) {                                            // break the possible loop
            if (BuildConfig.DEBUG) Log.d(TAG, "New device token actually not new; skipping update");
            return;
        }
        if (!AuthService.get().isAuthenticated()) {                                                 // avoid early update error
            if (BuildConfig.DEBUG) Log.d(TAG, "User is not authenticated; skipping update");
            return;
        }
        CurrentUser currentUser = new CurrentUser();
        currentUser.setDeviceToken(currentDeviceToken);
        mDisposable.add(CurrentUserRepo.get().updateAsync(currentUser).subscribe(() -> {}));        // update the token
    }


    /**
     * Notify about a received invite
     */
    private void notifyOnInvite(String invitingEmail) {
        if (invitingEmail == null || invitingEmail.isEmpty()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Message contains no inviting email; skipping");
            return;
        }
        if (PreferenceStore.getAppMode() != Constants.APP_MODE_MINOR) {
            if (BuildConfig.DEBUG) Log.e(TAG, "App mode should be 'MINOR' to process the message; skipping");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "An invite message received, emitting a corresponding event");
        MessageRepo.get().getInviteReceivedSubject().onNext(GenericEvent
            .of(SUCCESS)
            .setData(invitingEmail)
        );
    }


    /**
     * Notify about an accepted invite
     */
    private void notifyOnInviteAccepted(String inviteeEmail) {
        if (inviteeEmail == null || inviteeEmail.isEmpty()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Message contains no invitee email; skipping");
            return;
        }
        if (PreferenceStore.getAppMode() != Constants.APP_MODE_MAJOR) {
            if (BuildConfig.DEBUG) Log.e(TAG, "App mode should be 'MAJOR' to process the message; skipping");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "An invite acceptance message received, emitting a corresponding event");
        MessageRepo.get().getAcceptInviteReceivedSubject().onNext(GenericEvent
            .of(SUCCESS)
            .setData(inviteeEmail)
        );
    }


    /**
     * Notify about a received location request
     */
    private void notifyOnLocationRequest(String senderEmail) {
        if (senderEmail == null || senderEmail.isEmpty()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Message contains no sender email; skipping");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "A location request message received, emitting a corresponding event");
        MessageRepo.get().getLocationRequestReceivedSubject().onNext(GenericEvent
            .of(SUCCESS)
            .setData(senderEmail)
        );
        super.sendBroadcast(LocationRequestBroadcastReceiver.getIntent());
    }


    /**
     * Notify about a received location
     */
    private void notifyOnLocation(String locationJson, String senderEmail) {
        if (locationJson == null || locationJson.isEmpty()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Message contains no location; skipping");
            return;
        }
        if (senderEmail == null || senderEmail.isEmpty()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Message contains no sender email; skipping");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "A location message received, emitting a corresponding event");
        Location location = Tools.get().getLocationFromJson(locationJson);
        MessageRepo.get().getLocationReceivedSubject().onNext(LocationEvent
            .of(SUCCESS)
            .setLocation(location)
            .setSenderEmail(senderEmail)
        );
    }

}
