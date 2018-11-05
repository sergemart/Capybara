package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.events.GenericEvent;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;


public class InitialCommonSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericEvent> mCommonSetupFinishedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getCommonSetupFinishedSubject() {
        return mCommonSetupFinishedSubject;
    }


}
