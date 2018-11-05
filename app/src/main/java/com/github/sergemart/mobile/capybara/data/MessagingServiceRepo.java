package com.github.sergemart.mobile.capybara.data;

import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.events.LocationResult;

import io.reactivex.subjects.BehaviorSubject;


// Singleton
public class MessagingServiceRepo {

    private static MessagingServiceRepo sInstance = new MessagingServiceRepo();


    // Private constructor
    private MessagingServiceRepo() {
    }


    // Factory method
    public static MessagingServiceRepo get() {
        if(sInstance == null) sInstance = new MessagingServiceRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final BehaviorSubject<GenericEvent> mInviteReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<GenericEvent> mAcceptInviteReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<LocationResult> mLocationReceivedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getInviteReceivedSubject() {
        return mInviteReceivedSubject;
    }


    public BehaviorSubject<GenericEvent> getAcceptInviteReceivedSubject() {
        return mAcceptInviteReceivedSubject;
    }


    public BehaviorSubject<LocationResult> getLocationReceivedSubject() {
        return mLocationReceivedSubject;
    }

}
