package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.events.GenericResult;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;


public class InitialMajorSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericResult> mMajorSetupFinishedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericResult> getMajorSetupFinishedSubject() {
        return mMajorSetupFinishedSubject;
    }


}
