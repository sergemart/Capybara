package com.github.sergemart.mobile.capybara.viewmodel;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.model.CurrentUser;
import com.github.sergemart.mobile.capybara.data.repo.CurrentUserRepo;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseDbException;

import androidx.lifecycle.ViewModel;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


public class InitialCommonSharedViewModel extends ViewModel {

    private final BehaviorSubject<GenericEvent> mCommonSetupFinishedSubject = BehaviorSubject.create();


    // --------------------------- Observable getters

    public BehaviorSubject<GenericEvent> getCommonSetupFinishedSubject() {
        return mCommonSetupFinishedSubject;
    }


    // --------------------------- Use cases

    /**
     * Async upgrade backend database schema
     */
    public Completable upgradeBackendObservableAsync() {
        return Completable.create(emitter -> {
            try {                                                                                   // synchronous sequence of upgrade calls
                if (PreferenceStore.getCurrentBackendVersion() < 3) this.upgradeBackendSchemaV3();
                PreferenceStore.storeCurrentBackendVersion(BuildConfig.VERSION_CODE);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }


    // --------------------------- Subroutines: Sequential upgrades

    /**
     * Upgrade the backend database schema to v3, performed in a blocking way
     */
    private void upgradeBackendSchemaV3() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setAppMode(PreferenceStore.getAppMode());
        CurrentUserRepo.get().updateAsync(currentUser).blockingAwait();
    }


}
