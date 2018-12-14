package com.github.sergemart.mobile.capybara.data.repo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.exception.InvalidDataException;
import com.github.sergemart.mobile.capybara.exception.NotFoundException;

import java.util.List;

import io.reactivex.Single;


// Singleton
public class FamilyMemberRepo {

    private static final String TAG = FamilyMemberRepo.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static FamilyMemberRepo sInstance;


    // Private constructor
    private FamilyMemberRepo() {
        // Init member variables
        mContext = App.getContext();
    }


    // Factory method
    public static FamilyMemberRepo get() {
        if(sInstance == null) sInstance = new FamilyMemberRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Context mContext;


    // --------------------------- The interface

    /**
     * Emit a list of the IDs of the current user's family members
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    public Single<List<String>> readFamilyMemberIdsAsync() {
        return Single.create(emitter -> {
            String familyCreatorId = AuthService.get().getCurrentUser().getUid();
            FirestoreService.get().readFamilyAsync(familyCreatorId).subscribe(
                querySnapshot -> {
                    if (querySnapshot.size() == 0) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_family_not_found);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new NotFoundException(errorMessage));
                    } else if (querySnapshot.size() > 1) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_multiple_families);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new InvalidDataException(errorMessage));
                    } else {
                        List<String> familyMemeberIds = (List<String>) querySnapshot.getDocuments().get(0).get(Constants.FIRESTORE_FIELD_MEMBERS);
                        emitter.onSuccess(familyMemeberIds);
                    }
                },
                emitter::onError
            );
        });
    }


}
