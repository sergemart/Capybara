package com.github.sergemart.mobile.capybara.viewmodel;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.CompletableSubject;


public class InitialCommonSharedViewModel extends ViewModel {

    private final CompletableSubject mCommonSetupFinishedSubject = CompletableSubject.create();


    // --------------------------- Observable getters

    public CompletableSubject getCommonSetupFinishedSubject() {
        return mCommonSetupFinishedSubject;
    }


}
