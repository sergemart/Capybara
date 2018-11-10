package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.viewmodel.InitialMinorSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;


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
        View fragmentView = inflater.inflate(R.layout.fragment_initial_minor_wait_for_invite, container, false);
        mExitApplicationButton = fragmentView.findViewById(R.id.button_exit_application);

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

        // Set a listener to the "Exit Application" button
        pViewDisposable.add(RxView.clicks(mExitApplicationButton).subscribe(event ->
            mInitialMinorSharedViewModel.getExitRequestedSubject().onComplete()                     // send ExitRequested event
        ));

    }


    // --------------------------- Use cases



}
