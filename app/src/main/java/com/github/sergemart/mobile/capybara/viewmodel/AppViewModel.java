package com.github.sergemart.mobile.capybara.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.events.Result;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseDbException;

import io.reactivex.Observable;


// Singleton
public class AppViewModel {

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static AppViewModel sInstance = new AppViewModel();


    // Private constructor
    private AppViewModel() {
        // Init member variables
        mContext = App.getContext();
    }


    // Factory method
    public static AppViewModel get() {
        if(sInstance == null) sInstance = new AppViewModel();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Context mContext;


    // --------------------------- Use cases

    @SuppressWarnings({"unchecked", "UnnecessaryUnboxing"})
    public Observable<GenericEvent<Boolean>> ifSchemaUpgradeNeededAsync() {
        return FirestoreService.get().readSystemDatabaseAsync()
            .map(event -> {
                switch (event.getResult()) {
                    case SUCCESS:
                        Integer backendVersion = (Integer) event.getData().get(Constants.FIRESTORE_FIELD_VERSION);
                        if (backendVersion == null) {
                            String errorMessage = mContext.getString(R.string.exception_firebase_field_not_read);
                            return GenericEvent.of(Result.NOT_FOUND).setException(new FirebaseDbException(errorMessage));
                        }
                        if (backendVersion.intValue() < BuildConfig.VERSION_CODE) {                 // schema upgrade needed
                            return GenericEvent.of(Result.SUCCESS).setData(Boolean.TRUE);
                        }
                        if (backendVersion.intValue() >= BuildConfig.VERSION_CODE) {                // schema upgrade not needed
                            return GenericEvent.of(Result.SUCCESS).setData(Boolean.FALSE);
                        }
                    case FAILURE:
                        return event.setData(null);                                                 // forward the event with cleared data
                    default:
                        return event.setData(null);
                }
            })
        ;

    }

}
