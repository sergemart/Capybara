package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.MessagingRepo;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.exceptions.FirebaseMessagingException;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMinorSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import static com.github.sergemart.mobile.capybara.data.events.Result.FAILURE;


public class InitialMinorWaitForInviteFragment
    extends AbstractFragment
{

    private MaterialButton mExitApplicationButton;

    private InitialMinorSharedViewModel mInitialMinorSharedViewModel;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialMinorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(InitialMinorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_initial_minor_wait_for_invite, inflater, container);
        mExitApplicationButton = fragmentView.findViewById(R.id.button_exit_application);

        this.setViewListeners();
        return fragmentView;
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {

        // Set a listener to the InviteReceived event
        pInstanceDisposable.add(MessagingRepo.get().getInviteReceivedSubject().subscribe(event -> {
            switch (event.getResult()) {
                case SUCCESS:
                    this.navigateToNextPage();
                    break;
                case FAILURE:                                                                       // no way to get here, added just in case
                    mInitialMinorSharedViewModel.getMinorSetupFinishedSubject().onNext(GenericEvent.of(FAILURE).setData(event.getData()).setException(new FirebaseMessagingException()));
                    break;
                default:
            }
        }));
    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "Exit Application" button
        pViewDisposable.add(RxView.clicks(mExitApplicationButton).subscribe(event ->
            mInitialMinorSharedViewModel.getExitRequestedSubject().onComplete()                     // send ExitRequested event
        ));

    }


    // --------------------------- Use cases

    /**
     * Navigate to the accept invitation page
     */
    private void navigateToNextPage() {
        NavHostFragment.findNavController(this).navigate(R.id.action_initialMinorWaitForInvite_to_initialMinorAcceptInvite);
    }


}
