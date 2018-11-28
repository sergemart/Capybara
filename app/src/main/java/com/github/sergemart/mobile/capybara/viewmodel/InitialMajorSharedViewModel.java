package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.data.events.GenericEvent;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;


public class InitialMajorSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericEvent> mMajorSetupFinishedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getMajorSetupFinishedSubject() {
        return mMajorSetupFinishedSubject;
    }


}
