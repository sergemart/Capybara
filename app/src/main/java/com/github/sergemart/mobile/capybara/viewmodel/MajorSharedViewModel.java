package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.data.events.GenericEvent;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;


public class MajorSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericEvent> mSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getSubject() {
        return mSubject;
    }


}
