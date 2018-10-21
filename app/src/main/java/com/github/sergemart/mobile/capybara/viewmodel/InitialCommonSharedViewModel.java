package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.events.GenericResult;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;


public class InitialCommonSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericResult> mCommonSetupFinishedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericResult> getCommonSetupFinishedSubject() {
        return mCommonSetupFinishedSubject;
    }


}
