package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.events.LocationEvent;

import io.reactivex.subjects.BehaviorSubject;


// Singleton
public class MessageRepo {

    private static MessageRepo sInstance = new MessageRepo();


    // Private constructor
    private MessageRepo() {
    }


    // Factory method
    public static MessageRepo get() {
        if(sInstance == null) sInstance = new MessageRepo();
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
