package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.data.events.GenericEvent;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;


public class InitialMinorSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericEvent> mMinorSetupFinishedSubject = BehaviorSubject.create();
    private final CompletableSubject mExitRequestedSubject = CompletableSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getMinorSetupFinishedSubject() {
        return mMinorSetupFinishedSubject;
    }


    public CompletableSubject getExitRequestedSubject() {
        return mExitRequestedSubject;
    }

}
