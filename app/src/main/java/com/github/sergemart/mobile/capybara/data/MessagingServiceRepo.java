package com.github.sergemart.mobile.capybara.data;

import com.github.sergemart.mobile.capybara.events.GenericResult;

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

    private final BehaviorSubject<GenericResult> mInviteReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<GenericResult> mAcceptInviteReceivedSubject = BehaviorSubject.create();
    private final BehaviorSubject<GenericResult> mLocationReceivedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericResult> getInviteReceivedSubject() {
        return mInviteReceivedSubject;
    }


    public BehaviorSubject<GenericResult> getAcceptInviteReceivedSubject() {
        return mAcceptInviteReceivedSubject;
    }


    public BehaviorSubject<GenericResult> getLocationReceivedSubject() {
        return mLocationReceivedSubject;
    }

}
