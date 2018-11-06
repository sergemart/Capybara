package com.github.sergemart.mobile.capybara.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.Tools;
import com.github.sergemart.mobile.capybara.events.CreateFamilyEvent;
import com.github.sergemart.mobile.capybara.events.FamilyActionEvent;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.events.SignInEvent;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseFunctionException;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseSigninException;
import com.github.sergemart.mobile.capybara.exceptions.GoogleSigninException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.subjects.PublishSubject;

import static com.github.sergemart.mobile.capybara.events.CreateFamilyEvent.Result.BACKEND_ERROR;
import static com.github.sergemart.mobile.capybara.events.CreateFamilyEvent.Result.CREATED;
import static com.github.sergemart.mobile.capybara.events.CreateFamilyEvent.Result.EXIST;
import static com.github.sergemart.mobile.capybara.events.CreateFamilyEvent.Result.EXIST_MORE_THAN_ONE;
import static com.github.sergemart.mobile.capybara.events.FamilyActionEvent.Result.MORE_THAN_ONE_FAMILY;
import static com.github.sergemart.mobile.capybara.events.FamilyActionEvent.Result.NO_FAMILY;
import static com.github.sergemart.mobile.capybara.events.GenericEvent.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.events.GenericEvent.Result.SUCCESS;


// Singleton
public class CloudRepo {

    private static final String TAG = CloudRepo.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")                                                                // OK for the application context
    private static CloudRepo sInstance;


    // Private constructor
    private CloudRepo() {

        // Init member variables
        mContext = App.getContext();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(mContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) mUsername = mFirebaseUser.getDisplayName();
        mFirebaseFunctions = FirebaseFunctions.getInstance();
        mFirebaseInstanceId = FirebaseInstanceId.getInstance();
        mDeviceToken = "";
    }


    // Factory method
    public static CloudRepo get() {
        if(sInstance == null) sInstance = new CloudRepo();
        return sInstance;
    }


    // --------------------------- Member variables

    private final PublishSubject<SignInEvent> mSignInSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mGetDeviceTokenSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mPublishDeviceTokenSubject = PublishSubject.create();
    private final PublishSubject<CreateFamilyEvent> mCreateFamilySubject = PublishSubject.create();
    private final PublishSubject<FamilyActionEvent> mCreateFamilyMemberSubject = PublishSubject.create();
    private final PublishSubject<FamilyActionEvent> mDeleteFamilyMemberSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mSendInviteSubject = PublishSubject.create();
    private final PublishSubject<FamilyActionEvent> mJoinFamilySubject = PublishSubject.create();
    private final PublishSubject<FamilyActionEvent> mSendLocationSubject = PublishSubject.create();
    private final PublishSubject<GenericEvent> mSignOutSubject = PublishSubject.create();

    private final Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername = Constants.DEFAULT_USERNAME;
    private FirebaseFunctions mFirebaseFunctions;
    private FirebaseInstanceId mFirebaseInstanceId;
    private String mDeviceToken;


    // --------------------------- Observable getters

    public PublishSubject<SignInEvent> getSignInSubject() {
        return mSignInSubject;
    }


    public PublishSubject<GenericEvent> getGetDeviceTokenSubject() {
        return mGetDeviceTokenSubject;
    }


    public PublishSubject<GenericEvent> getPublishDeviceTokenSubject() {
        return mPublishDeviceTokenSubject;
    }


    public PublishSubject<CreateFamilyEvent> getCreateFamilySubject() {
        return mCreateFamilySubject;
    }


    public PublishSubject<FamilyActionEvent> getCreateFamilyMemberSubject() {
        return mCreateFamilyMemberSubject;
    }


    public PublishSubject<FamilyActionEvent> getDeleteFamilyMemberSubject() {
        return mDeleteFamilyMemberSubject;
    }


    public PublishSubject<GenericEvent> getSendInviteSubject() {
        return mSendInviteSubject;
    }


    public PublishSubject<FamilyActionEvent> getJoinFamilySubject() {
        return mJoinFamilySubject;
    }


    public PublishSubject<GenericEvent> getSignOutSubject() {
        return mSignOutSubject;
    }


    public PublishSubject<FamilyActionEvent> getSendLocationSubject() {
        return mSendLocationSubject;
    }


    // --------------------------- Repository interface: User authentication

    /**
     * @return true if authenticated, false if not
     */
    public boolean isAuthenticated() {
        return mFirebaseUser != null;
    }


    /**
     * Get current username
     */
    public String getCurrentUsername() {
        if (mUsername.equals(Constants.DEFAULT_USERNAME)) return "";
        else return mUsername;
    }


    /**
     * Send sign-in intent
     */
    public void sendSignInIntent(Activity activity) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, Constants.REQUEST_CODE_SIGN_IN);
    }


    /**
     * Process the response intent from Google client and proceed with Firebase authentication.
     * Send an event carrying the authenticated Firebase user
     */
    public void proceedWithFirebaseAuthAsync(Intent responseIntent) {
        // Process the response intent from Google client
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(responseIntent);
        GoogleSignInAccount googleSignInAccount;
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);                      // throwa an exception on sign-in error
        } catch (ApiException e) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_failed);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
            mSignInSubject.onNext(SignInEvent.of(SignInEvent.Result.FAILURE).setException( new GoogleSigninException(errorMessage, e)) );
            return;
        }
        if (googleSignInAccount == null) {
            String errorMessage = mContext.getString(R.string.exception_google_sign_in_account_is_null);
            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
            mSignInSubject.onNext(SignInEvent.of(SignInEvent.Result.FAILURE).setException( new GoogleSigninException(errorMessage)) );
            return;
        }

        // Google sign-in was successful, proceed with Firebase authentication
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth
            .signInWithCredential(authCredential)
            .addOnCompleteListener(task -> {
                if ( !task.isSuccessful() ) {                                                       // error check
                    String errorMessage = mContext.getString(R.string.exception_firebase_client_connection_failed);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSignInSubject.onNext(SignInEvent.of(SignInEvent.Result.FAILURE).setException( new FirebaseSigninException(errorMessage)) );
                    return;
                }
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_user_is_null);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSignInSubject.onNext(SignInEvent.of(SignInEvent.Result.FAILURE).setException( new FirebaseSigninException(errorMessage)) );
                    return;
                }
                mUsername = mFirebaseUser.getDisplayName();
                if (BuildConfig.DEBUG) Log.d(TAG, "Signed in successfully: " + mUsername);
                mSignInSubject.onNext(SignInEvent.of(SignInEvent.Result.SUCCESS).setFirebaseUser(mFirebaseUser));
            })
        ;
    }


    /**
     * Sign out the current user.
     * Send an event notifying on success or failure
     */
    public void signOut() {
        try {
            mFirebaseAuth.signOut();
            mGoogleSignInClient.signOut();
            mUsername = Constants.DEFAULT_USERNAME;
            mSignOutSubject.onNext(GenericEvent.of(SUCCESS));
        } catch (Exception e) {
            mSignOutSubject.onNext(GenericEvent.of(FAILURE).setException(new FirebaseSigninException(e)));
        }
    }


    // --------------------------- Repository interface: Manage the device token

    /**
     * Explicitly get Firebase Messaging device token from the cloud.
     * Send an event notifying on success or failure
     */
    public void getTokenAsync() {
        mFirebaseInstanceId
            .getInstanceId()
            .addOnSuccessListener(instanseIdResult -> {
                mDeviceToken = instanseIdResult.getToken();
                if (BuildConfig.DEBUG) Log.d(TAG, "Got device token: " + mDeviceToken);
                mGetDeviceTokenSubject.onNext(GenericEvent.of(SUCCESS));
            })
            .addOnFailureListener(e -> {
                String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_received);
                if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
                mGetDeviceTokenSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e)) );
            })
        ;
    }


    /**
     * Update current known device token when received one from the cloud.
     * Init publishing the token, if it differs
     */
    public void updateDeviceToken(String deviceToken) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Update token method called with the token provided: " + deviceToken);
        if (deviceToken.equals(mDeviceToken)) {                                                     // break the possible loop
            if (BuildConfig.DEBUG) Log.d(TAG, "Provided token already known; skipping update");
            return;
        }
        mDeviceToken = deviceToken;
        this.publishDeviceTokenAsync();
    }


    /**
     * Publish device token on a backend using custom Firebase callable function
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void publishDeviceTokenAsync() {
        if (mDeviceToken == null || mDeviceToken.equals("")) {
            if (BuildConfig.DEBUG) Log.e(TAG, "No device token set while attempting to publish it on backend; skipping");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to publish device token on backend; skipping");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_DEVICE_TOKEN, mDeviceToken);

        mFirebaseFunctions
            .getHttpsCallable("updateDeviceToken")
            .call(data)
            .continueWith(task -> {
                Map<String, Object> result = null;
                try {
                    result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData(); // throws an exception on error
                    // if success:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Device token published :" + mDeviceToken);
                    mPublishDeviceTokenSubject.onNext(GenericEvent.of(SUCCESS));
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
                    mPublishDeviceTokenSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e)) );
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " +  e.getMessage());
                    mPublishDeviceTokenSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e)) );
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_device_token_not_published);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + "; no exception provided");
                    mPublishDeviceTokenSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage)) );
                }
            })
        ;
    }


    // --------------------------- Repository interface: Manage a family
    
    /**
     * Create family data on a backend using custom Firebase callable function.
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void createFamilyAsync() {
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to create family data on backend; skipping");
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
                            mCreateFamilySubject.onNext(CreateFamilyEvent.of(CREATED).setFamilyUid(familyUid));
                            break;
                        case Constants.RETURN_CODE_EXIST:
                            familyUid = (String)Objects.requireNonNull(result.get("familyUid"));
                            if (BuildConfig.DEBUG) Log.d(TAG, "Family data already exist on backend");
                            mCreateFamilySubject.onNext(CreateFamilyEvent.of(EXIST).setFamilyUid(familyUid));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mCreateFamilySubject.onNext(CreateFamilyEvent.of(EXIST_MORE_THAN_ONE));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mCreateFamilySubject.onNext(CreateFamilyEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onNext(CreateFamilyEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilySubject.onNext(CreateFamilyEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mCreateFamilySubject.onNext(CreateFamilyEvent.of(BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
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
            if (BuildConfig.DEBUG) Log.e(TAG, "Empty or null family member email provided while attempting to store it on backend; skipping");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to store family member on backend; skipping");
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
                            mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(MORE_THAN_ONE_FAMILY));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(NO_FAMILY));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_created);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mCreateFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
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
            if (BuildConfig.DEBUG) Log.e(TAG, "Empty or null family member email provided while attempting to remove it on backend; skipping");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to remove family member on backend; skipping");
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
                            mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(MORE_THAN_ONE_FAMILY));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(NO_FAMILY));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_member_not_deleted);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mDeleteFamilyMemberSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
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
            if (BuildConfig.DEBUG) Log.e(TAG, "Empty or null invitee email provided while attempting to send an invite; skipping");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to send an invite; skipping");
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
                            if (BuildConfig.DEBUG) Log.d(TAG, "Invite message sent");
                            mSendInviteSubject.onNext(GenericEvent.of(SUCCESS));
                            break;
                        default:
                            String errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_invite_message_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_invite_message_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_invite_message_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSendInviteSubject.onNext(GenericEvent.of(FAILURE).setException( new FirebaseFunctionException(errorMessage) ));
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
            if (BuildConfig.DEBUG) Log.e(TAG, "Empty or null inviting email provided while attempting to join a family; skipping");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to join a family; skipping");
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
                            mJoinFamilySubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The inviting user has more than one family record");
                            mJoinFamilySubject.onNext(FamilyActionEvent.of(MORE_THAN_ONE_FAMILY));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The inviting user has no family records");
                            mJoinFamilySubject.onNext(FamilyActionEvent.of(NO_FAMILY));
                            break;
                        case Constants.RETURN_CODE_NOT_SENT:
                            String receivedErrorMessage = (String)Objects.requireNonNull(result.get("errorMessage"));
                            String errorMessage = mContext.getString(R.string.exception_firebase_invite_acceptance_message_not_sent);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + receivedErrorMessage);
                            mJoinFamilySubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                            break;
                        default:
                            errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mJoinFamilySubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_joined);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mJoinFamilySubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_joined);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mJoinFamilySubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_family_not_joined);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mJoinFamilySubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


    // --------------------------- Repository interface: Location
    
    /**
     * Send a location to family members using custom Firebase callable function
     * Send an event notifying on success or failure
     */
    @SuppressWarnings("unchecked")
    public void sendLocationAsync(Location location) {
        if (location == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Location is null while attempting to send it to a family; skipping");
            return;
        }
        if (mFirebaseUser == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "User not authenticated while attempting to send location to a family; skipping");
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
                            mSendLocationSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.SUCCESS));
                            break;
                        case Constants.RETURN_CODE_MORE_THAN_ONE_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has more than one family record");
                            mSendLocationSubject.onNext(FamilyActionEvent.of(MORE_THAN_ONE_FAMILY));
                            break;
                        case Constants.RETURN_CODE_NO_FAMILY:
                            if (BuildConfig.DEBUG) Log.e(TAG, "The user has no family records");
                            mSendLocationSubject.onNext(FamilyActionEvent.of(NO_FAMILY));
                            break;
                        case Constants.RETURN_CODE_NONE_SENT:
                            String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendLocationSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                            break;
                        default:
                            errorMessage = mContext.getString(R.string.exception_firebase_function_unknown_response);
                            if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                            mSendLocationSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                    }
                } catch (Exception e) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendLocationSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                }
                return result;
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    Exception e = task.getException();
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage + ": " + e.getMessage());
                    mSendLocationSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage, e) ));
                } else if (!task.isSuccessful()) {
                    String errorMessage = mContext.getString(R.string.exception_firebase_location_not_sent);
                    if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                    mSendLocationSubject.onNext(FamilyActionEvent.of(FamilyActionEvent.Result.BACKEND_ERROR).setException( new FirebaseFunctionException(errorMessage) ));
                }
            })
        ;
    }


}
