package com.github.sergemart.mobile.capybara.viewmodel;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.CompletableSubject;


public class InitialMajorSharedViewModel extends ViewModel {

    private final CompletableSubject mMajorSetupFinishedSubject = CompletableSubject.create();


    // --------------------------- Observable getters

    public CompletableSubject getMajorSetupFinishedSubject() {
        return mMajorSetupFinishedSubject;
    }


}
