package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.events.GenericResult;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;


public class InitialMajorSharedViewModel extends ViewModel {

    private final PublishSubject<GenericResult> mMajorSetupFinishedSubject = PublishSubject.create();


    // --------------------------- Observable getters

    public PublishSubject<GenericResult> getMajorSetupFinishedSubject() {
        return mMajorSetupFinishedSubject;
    }


}
