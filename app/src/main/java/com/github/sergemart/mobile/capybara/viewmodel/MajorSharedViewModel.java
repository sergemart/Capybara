package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.events.GenericResult;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;


public class MajorSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericResult> mSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericResult> getSubject() {
        return mSubject;
    }


}
