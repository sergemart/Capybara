package com.github.sergemart.mobile.capybara.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.MessagingRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseMessagingException;
import com.github.sergemart.mobile.capybara.ui.dialogs.JoinFamilyRetryDialogFragment;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMinorSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import static com.github.sergemart.mobile.capybara.events.Result.FAILURE;
import static com.github.sergemart.mobile.capybara.events.Result.SUCCESS;


public class InitialMinorAcceptInviteFragment
    extends AbstractFragment
{

    private TextView mInvitationTextView;
    private MaterialButton mAcceptInviteButton;
    private MaterialButton mDeclineInviteButton;
    private ViewGroup mContentContainerLayout;

    private InitialMinorSharedViewModel mInitialMinorSharedViewModel;
    private Throwable mCause;
    private String mInvitingEmail;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialMinorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialMinorSharedViewModel.class);
        mInvitingEmail = super.getString(R.string.word_someone);                                    // stub

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_initial_minor_accept_invite, inflater, container);
        mInvitationTextView = fragmentView.findViewById(R.id.textView_invitation);
        mAcceptInviteButton = fragmentView.findViewById(R.id.button_accept_invite);
        mDeclineInviteButton = fragmentView.findViewById(R.id.button_decline_invite);
        mContentContainerLayout = fragmentView.findViewById(R.id.layout_content_container);

        this.setViewListeners();
        return fragmentView;
    }


    /**
     * Used uncommonly as a callback for the embedded dialog fragment
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.REQUEST_CODE_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {                                             // retry
                    this.joinFamily();
                } else if (resultCode == Activity.RESULT_CANCELED) {                                // fatal
                    mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                }
                break;
            default:
        }
    }

    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the "JoinFamily" event
        pInstanceDisposable.add(CloudRepo.get().getJoinFamilySubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    PreferenceStore.storeFamilyJoined(true);
                    if (BuildConfig.DEBUG) Log.d(TAG, "JoinFamily.SUCCESS event received; emitting MinorSetupFinished event");
                    mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS));
                    break;
                case NOT_FOUND:
                    PreferenceStore.storeFamilyJoined(false);
                    if (BuildConfig.DEBUG) Log.d(TAG, "JoinFamily.NOT_FOUND event received; emitting MinorSetupFinished event");
                    mCause = event.getException();
                    mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                    break;
                case INTEGRITY_ERROR:
                    if (BuildConfig.DEBUG) Log.d(TAG, "JoinFamily.INTEGRITY_ERROR event received; emitting MinorSetupFinished event");
                    mCause = event.getException();
                    mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setException(mCause));
                    break;
                case BACKEND_ERROR:
                    if (BuildConfig.DEBUG) Log.d(TAG, "JoinFamily.BACKEND_ERROR event received; invoking retry dialog");
                    mCause = event.getException();
                    this.showJoinFamilyRetryDialog(mCause);
                    break;
                default:
            }
        }));

    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "Accept Invite" button
        pViewDisposable.add(RxView.clicks(mAcceptInviteButton).subscribe(event ->
            this.joinFamily()
        ));


        // Set a listener to the "Decline Invite" button
        pViewDisposable.add(RxView.clicks(mDeclineInviteButton).subscribe(event ->
            this.navigateToPreviousPage()
        ));


        // Set a listener to the InviteReceived event
        pViewDisposable.add(MessagingRepo.get().getInviteReceivedSubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:                                                                       // update the UI with inviting email
                    mInvitingEmail = (String)event.getData();
                    mInvitationTextView.setText(super.getString(R.string.msg_invitation, mInvitingEmail));
                    break;
                case FAILURE:                                                                       // no way to get here, added just in case
                    mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setData(event.getData()).setException(new FirebaseMessagingException()));
                    break;
                default:
            }
        }));

    }


    // --------------------------- Use cases

    /**
     * Navigate to the waiting for an invitation page
     */
    private void navigateToPreviousPage() {
        NavHostFragment.findNavController(this).popBackStack();
    }


    /**
     * Join the family
     */
    private void joinFamily() {
        mContentContainerLayout.setVisibility(View.GONE);
        super.showWaitingState();
        CloudRepo.get().joinFamilyAsync(mInvitingEmail);
    }


    /**
     * Show join family retry dialog
     */
    private void showJoinFamilyRetryDialog(Throwable cause) {
        JoinFamilyRetryDialogFragment.newInstance(cause).show(
            Objects.requireNonNull(super.getChildFragmentManager()),
            Constants.TAG_JOIN_FAMILY_RETRY_DIALOG
        );
    }

}
