package com.github.sergemart.mobile.capybara.viewmodel;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.Subject;


public class ErrorSharedViewModel extends ViewModel {

    private final BehaviorSubject<String> mErrorDetailsSubject = BehaviorSubject.create();
    private final CompletableSubject mExitRequestedSubject = CompletableSubject.create();


    // --------------------------- Observable getters/ emitters

    public Subject<String> getErrorDetailsSubject() {
        return mErrorDetailsSubject;
    }


    public CompletableSubject getExitRequestedSubject() {
        return mExitRequestedSubject;
    }

}
