package com.github.sergemart.mobile.capybara.viewmodel;

import com.google.firebase.auth.FirebaseUser;

import androidx.lifecycle.ViewModel;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


public class SharedStartupViewModel extends ViewModel {

    private final Subject<FirebaseUser> mFirebaseUserSubject = PublishSubject.create();
    private final CompletableSubject mAppIsInitializedSubject = CompletableSubject.create();


    // --------------------------- Observable getters/ emitters

    public Subject<FirebaseUser> getFirebaseUserSubject() {
        return mFirebaseUserSubject;
    }


    public void emitFirebaseUser(FirebaseUser firebaseUser) {
        mFirebaseUserSubject.onNext(firebaseUser);
    }


    public CompletableSubject getAppIsInitializedSubject() {
        return mAppIsInitializedSubject;
    }


    public void emitAppIsInitialized() {
        mAppIsInitializedSubject.onComplete();
    }


}
