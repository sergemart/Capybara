package com.github.sergemart.mobile.capybara.data.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.Tools;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exception.FirebaseFunctionException;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.subjects.PublishSubject;

import static com.github.sergemart.mobile.capybara.data.events.Result.BACKEND_ERROR;
import static com.github.sergemart.mobile.capybara.data.events.Result.EXIST;
import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.data.events.Result.INTEGRITY_ERROR;
import static com.github.sergemart.mobile.capybara.data.events.Result.NOT_FOUND;
import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


// Singleton
public class FunctionsService {

    private static final String TAG = FunctionsService.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static FunctionsService sInstance;


    // Private constructor
    private FunctionsService() {

        // Init member variables
        mContext = App.getContext();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(mContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mFirebaseFunctions = FirebaseFunctions.getInstance();
    }


    // Factory method
    public static FunctionsService get() {
        if(sInstance == null) sInstance = new FunctionsService();
        return sInstance;
    }


    // --------------------------- Member variables

    private final PublishSubject<GenericEvent> mCreateFamilySubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mCreateFamilyMemberSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mDeleteFamilyMemberSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mCheckFamilyMembershipSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mSendInviteSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mJoinFamilySubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mSendLocationSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mSendLocationRequestSubject = PublishSubject.create();

    private final Context mContext;
    private FirebaseFunctions mFirebaseFunctions;


    // --------------------------- Observable getters

    public PublishSubject<GenericEvent> getCreateFamilySubject() {
        return mCreateFamilySubject;
    }


    public PublishSubject<GenericEvent> getCreateFamilyMemberSubject() {
        return mCreateFamilyMemberSubject;
    }


    public PublishSubject<GenericEvent> getDeleteFamilyMemberSubject() {
        return mDeleteFamilyMemberSubject;
    }


    public PublishSubject<GenericEvent> getCheckFamilyMembershipSubject() {
        return mCheckFamilyMembershipSubject;
    }


    public PublishSubject<GenericEvent> getSendInviteSubject() {
        return mSendInviteSubject;
    }


    public PublishSubject<GenericEvent> getJoinFamilySubject() {
        return mJoinFamilySubject;
    }


    public PublishSubject<GenericEvent> getSendLocationSubject() {
        return mSendLocationSubject;
    }


    public PublishSubject<GenericEvent> getSendLocationRequestSubject() {
        return mSendLocationRequestSubject;
    }


    // --------------------------- The interface: Manage a family
    
    /**
     * Create family data on a backend using custom Firebase callable function.
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void createFamilyAsync() {
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mCreateFamilySubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();

        mFirebaseFunctions
            .getHttpsCallable("createFamily")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    String familyUid;
                    switch (returnCode) {
                        case Constants.RETURN_CODE_CREATED:
                            familyUid = (String)Objects.requireNonNull(result.get("familyUid"));
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family data created on backend");
                            mCreateFamilySubject.onNext(GenericEvent.of(SUCCESS).setData(familyUid));
                            break;
                        case Constants.RETURN_CODE_EXIST:
                            familyUid = (String)Objects.requireNonNull(result.get("familyUid"));
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family data already exist on backend");
                            mCreateFamilySubject.onNext(GenericEvent.of(EXIST).setData(familyUid));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mCreateFamilySubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mCreateFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mCreateFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    /**
     * Insert a family member into family data on a backend using custom Firebase callable function.
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void createFamilyMemberAsync(String familyMemberEmail) {
        if (familyMemberEmail == null || familyMemberEmail.equals("")) {
            String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mCreateFamilyMemberSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mCreateFamilyMemberSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_FAMILY_MEMBER_EMAIL, familyMemberEmail);

        mFirebaseFunctions
            .getHttpsCallable("createFamilyMember")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_CREATED:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family member stored on backend");
                            mCreateFamilyMemberSubject.onNext(GenericEvent.of(SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mCreateFamilyMemberSubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mCreateFamilyMemberSubject.onNext(GenericEvent.of(NOT_FOUND));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mCreateFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mCreateFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    /**
     * Remove a family member from family data on a backend using custom Firebase callable function.
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void deleteFamilyMemberAsync(String familyMemberEmail) {
        if (familyMemberEmail == null || familyMemberEmail.equals("")) {
            String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mDeleteFamilyMemberSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mDeleteFamilyMemberSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_FAMILY_MEMBER_EMAIL, familyMemberEmail);

        mFirebaseFunctions
            .getHttpsCallable("deleteFamilyMember")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_DELETED:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family member removed on backend");
                            mDeleteFamilyMemberSubject.onNext(GenericEvent.of(SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mDeleteFamilyMemberSubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mDeleteFamilyMemberSubject.onNext(GenericEvent.of(NOT_FOUND));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mDeleteFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mDeleteFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mDeleteFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mDeleteFamilyMemberSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    /**
     * Check if the caller belongs to any family using custom Firebase callable function.
     * Send an event notifying on success or failure, carrying a family owner email in case of success
     */
    @SuppressWarnings("unchecked")
    public void checkFamilyMembershipAsync() {
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mCheckFamilyMembershipSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();

        mFirebaseFunctions
            .getHttpsCallable("checkFamilyMembership")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_EXIST:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family found");
                            mCheckFamilyMembershipSubject.onNext(GenericEvent.of(EXIST).setData( Objects.requireNonNull(result.get("creatorEmail")) ));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user belongs to more than one family record");
                            mCheckFamilyMembershipSubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mCheckFamilyMembershipSubject.onNext(GenericEvent.of(NOT_FOUND));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mCheckFamilyMembershipSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_membership_not_checked);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCheckFamilyMembershipSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_membership_not_checked);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCheckFamilyMembershipSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_membership_not_checked);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mCheckFamilyMembershipSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    /**
     * Send a message inviting to join a family.
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void sendInviteAsync(String inviteeEmail) {
        if (inviteeEmail == null || inviteeEmail.equals("")) {
            String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSendInviteSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSendInviteSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_INVITEE_EMAIL, inviteeEmail);

        mFirebaseFunctions
            .getHttpsCallable("sendInvite")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_SENT:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Invite message queued");
                            mSendInviteSubject.onNext(GenericEvent.of(SUCCESS).setData(inviteeEmail));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setData(inviteeEmail).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_invite_message_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setData(inviteeEmail).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_invite_message_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setData(inviteeEmail).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_invite_message_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setData(inviteeEmail).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    /**
     * Join a family.
     * Send a message accepting an invite to join a family.
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void joinFamilyAsync(String invitingEmail) {
        if (invitingEmail == null || invitingEmail.equals("")) {
            String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mJoinFamilySubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mJoinFamilySubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_INVITING_EMAIL, invitingEmail);

        mFirebaseFunctions
            .getHttpsCallable("joinFamily")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_OK:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family joined");
                            mJoinFamilySubject.onNext(GenericEvent.of(SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The inviting user has more than one family record");
                            mJoinFamilySubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The inviting user has no family records");
                            mJoinFamilySubject.onNext(GenericEvent.of(NOT_FOUND));
                            break;
                        case Constants.RETURN_CODE_NOT_SENT:
                            String receivedErrorMessage = (String)Objects.requireNonNull(result.get("errorMessage"));
                            String errorMessage = mContext.getString(R.string.exception_firebase_invite_acceptance_message_not_sent);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + receivedErrorMessage);
                            mJoinFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                            break;
                        default:
                            errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mJoinFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_joined);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mJoinFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_joined);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mJoinFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_joined);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mJoinFamilySubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    // --------------------------- The interface: Location

    /**
     * Request locations of family members using custom Firebase callable function
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void requestLocationsAsync() {
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSendLocationRequestSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();

        mFirebaseFunctions
            .getHttpsCallable("requestLocations")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_ALL_SENT: case Constants.RETURN_CODE_SOME_SENT:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Locations request sent");
                            mSendLocationRequestSubject.onNext(GenericEvent.of(SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mSendLocationRequestSubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mSendLocationRequestSubject.onNext(GenericEvent.of(NOT_FOUND));
                            break;
                        case Constants.RETURN_CODE_NONE_SENT:
                            String errorMessage = mContext.getString(R.string.exception_firebase_location_requests_not_sent);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendLocationRequestSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                            break;
                        default:
                            errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendLocationRequestSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_requests_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendLocationRequestSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_requests_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendLocationRequestSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_requests_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSendLocationRequestSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    /**
     * Send a location to family members using custom Firebase callable function
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void sendLocationAsync(Location location) {
        if (location == null) {
            String errorMessage = mContext.getString(R.string.exception_firebase_wrong_call);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSendLocationSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }
        if (!AuthService.get().isAuthenticated()) {
            String errorMessage = mContext.getString(R.string.exception_firebase_not_authenticated);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSendLocationSubject.onNext(GenericEvent.of(INTEGRITY_ERROR).setException(new FirebaseFunctionException(errorMessage)));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_LOCATION, Tools.get().getLocationJson(location));

        mFirebaseFunctions
            .getHttpsCallable("sendLocation")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    String returnCode = (String)Objects.requireNonNull(result.get("returnCode"));
                    switch (returnCode) {
                        case Constants.RETURN_CODE_ALL_SENT: case Constants.RETURN_CODE_SOME_SENT:
                            if (BuildConfig.DEBUG) Log.d(TAG, "Location sent");
                            mSendLocationSubject.onNext(GenericEvent.of(SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mSendLocationSubject.onNext(GenericEvent.of(INTEGRITY_ERROR));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mSendLocationSubject.onNext(GenericEvent.of(NOT_FOUND));
                            break;
                        case Constants.RETURN_CODE_NONE_SENT:
                            String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendLocationSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                            break;
                        default:
                            errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendLocationSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendLocationSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendLocationSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSendLocationSubject.onNext(GenericEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


}
