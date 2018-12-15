package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.exception.FirebaseDbException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;


// Singleton
public class FirestoreService {

    private static final String TAG = FirestoreService.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static FirestoreService sInstance;


    // Private constructor
    private FirestoreService() {
        // Init member variables
        mContext = App.getContext();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings firestoreSettings = new FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)                                                  // Preferred way to store timestamps (instead of Date)
            .build()
        ;
        mFirestore.setFirestoreSettings(firestoreSettings);
    }


    // Factory method
    public static FirestoreService get() {
        if(sInstance == null) sInstance = new FirestoreService();
        return sInstance;
    }


    // --------------------------- Member variables

    private final Context mContext;
    private FirebaseFirestore mFirestore;


    // --------------------------- The interface: Current user

    /**
     * Create or update a current user document on a backend in a way which works for the online and offline modes as well
     */
    public Completable updateCurrentUserLocalReplicaAsync(Map<String, Object> userData) {
        return Completable.create(emitter -> {
            if (userData == null) {
                String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            String userUid = AuthService.get().getCurrentUser().getUid();
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_USERS)
                .document(userUid)
                .set(userData, SetOptions.merge())                                                  // use the local replica
            ;
            if (BuildConfig.DEBUG) Log.d(TAG, "User document created or updated :" + userUid);
            emitter.onComplete();
        });
    }


    /**
     * Read a current user document on a backend
     */
    public Single<DocumentSnapshot> readCurrentUserAsync() {
        return Single.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_USERS)
                .document(AuthService.get().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "User document read");
                    emitter.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_not_read);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


    /**
     * Delete a current user document on a backend.
     * Out-of-workflow method to use in tests
     */
    public Completable deleteCurrentUserAsync() {
        return Completable.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_USERS)
                .document(AuthService.get().getCurrentUser().getUid())
                .delete()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "User document deleted");
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


    // --------------------------- The interface: Family

    /**
     * Create or update a family document on a backend.
     * Out-of-workflow method to use in tests
     */
    public Completable updateFamilyAsync(Map<String, Object> familyData) {
        return Completable.create(emitter -> {
            if (familyData == null) {
                String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            String creatorId = AuthService.get().getCurrentUser().getUid();
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_FAMILIES)
                .whereEqualTo(Constants.FIRESTORE_FIELD_CREATOR, creatorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.size() == 0) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_family_not_found);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new FirebaseDbException(errorMessage));
                    } else if (querySnapshot.size() > 1) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_multiple_families);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new FirebaseDbException(errorMessage));
                    } else {
                        querySnapshot
                            .getDocuments()
                            .get(0)
                            .getReference()
                            .set(familyData, SetOptions.merge())
                            .addOnSuccessListener(result -> {
                                if (BuildConfig.DEBUG) Log.d(TAG, "Family document updated");
                                emitter.onComplete();
                            })
                            .addOnFailureListener(e -> {
                                String errorMessage = mContext.getString(R.string.exception_firebase_family_not_updated);
                                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                                emitter.onError(new FirebaseDbException(errorMessage, e));
                            })
                        ;
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_updated);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


    /**
     * Read a family document on a backend
     */
    public Single<QuerySnapshot> readFamilyAsync(String creatorId) {
        return Single.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_FAMILIES)
                .whereEqualTo(Constants.FIRESTORE_FIELD_CREATOR, creatorId)
                .get()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Family document(s) read");
                    emitter.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_read);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


    /**
     * Delete a family document on a backend.
     * Out-of-workflow method to use in tests
     */
    public Completable deleteFamilyAsync(String creatorId) {
        return Completable.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_FAMILIES)
                .whereEqualTo(Constants.FIRESTORE_FIELD_CREATOR, creatorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.size() == 0) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_family_not_found);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new FirebaseDbException(errorMessage));
                    } else if (querySnapshot.size() > 1) {
                        String errorMessage = mContext.getString(R.string.exception_firebase_multiple_families);
                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                        emitter.onError(new FirebaseDbException(errorMessage));
                    } else {
                        querySnapshot
                            .getDocuments()
                            .get(0)
                            .getReference()
                            .delete()
                            .addOnSuccessListener(result -> {
                                if (BuildConfig.DEBUG) Log.d(TAG, "Family document deleted");
                                emitter.onComplete();
                            })
                            .addOnFailureListener(e -> {
                                String errorMessage = mContext.getString(R.string.exception_firebase_family_not_deleted);
                                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                                emitter.onError(new FirebaseDbException(errorMessage, e));
                            })
                        ;
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


    // --------------------------- The interface: System database

    /**
     * Read a system/database document on a backend
     */
    public Single<DocumentSnapshot> readSystemDatabaseAsync() {
        return Single.create(emitter -> {
            if (!AuthService.get().isAuthenticated()) {
                String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                emitter.onError(new FirebaseDbException(errorMessage));
                return;
            }
            mFirestore
                .collection(Constants.FIRESTORE_COLLECTION_SYSTEM)
                .document(Constants.FIRESTORE_DOCUMENT_DATABASE)
                .get()
                .addOnSuccessListener(result -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "System/database document read");
                    emitter.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mContext.getString(R.string.exception_firebase_system_data_not_read);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    emitter.onError(new FirebaseDbException(errorMessage, e));
                })
            ;
        });
    }


}
