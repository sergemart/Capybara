package com.github.sergemart.mobile.capybara.data;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.events.LocationEvent;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;


// Singleton
public class MessagingRepo {

    private static MessagingRepo sInstance = new MessagingRepo();


    // Private constructor
    private MessagingRepo() {
    }


    // Factory method
    public static MessagingRepo get() {
        if(sInstance == null) sInstance = new MessagingRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final BehaviorSubject<GenericEvent> mInviteReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<GenericEvent> mAcceptInviteReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<LocationEvent> mLocationReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<GenericEvent> mLocationRequestReceivedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getInviteReceivedSubject() {
        return mInviteReceivedSubject;
    }


    public BehaviorSubject<GenericEvent> getAcceptInviteReceivedSubject() {
        return mAcceptInviteReceivedSubject;
    }


    public BehaviorSubject<LocationEvent> getLocationReceivedSubject() {
        return mLocationReceivedSubject;
    }


    public BehaviorSubject<GenericEvent> getLocationRequestReceivedSubject() {
        return mLocationRequestReceivedSubject;
    }

}
