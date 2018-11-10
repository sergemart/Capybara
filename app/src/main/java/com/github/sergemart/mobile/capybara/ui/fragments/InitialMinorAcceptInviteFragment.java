package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.MessagingServiceRepo;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseMessagingException;
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

    private InitialMinorSharedViewModel mInitialMinorSharedViewModel;
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
        View fragmentView = inflater.inflate(R.layout.fragment_initial_minor_accept_invite, container, false);
        mInvitationTextView = fragmentView.findViewById(R.id.textView_invitation);
        mAcceptInviteButton = fragmentView.findViewById(R.id.button_accept_invite);
        mDeclineInviteButton = fragmentView.findViewById(R.id.button_decline_invite);
        pBackgroundImageView = fragmentView.findViewById(R.id.imageView_background);

        this.setViewListeners();
        return fragmentView;
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {
    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "Accept Invite" button
        pViewDisposable.add(RxView.clicks(mAcceptInviteButton).subscribe(event ->
            mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(SUCCESS))
        ));


        // Set a listener to the "Decline Invite" button
        pViewDisposable.add(RxView.clicks(mDeclineInviteButton).subscribe(event ->
            this.navigateToPreviousPage()
        ));


        // Set a listener to the InviteReceived event
        pViewDisposable.add(MessagingServiceRepo.get().getInviteReceivedSubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
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

}
