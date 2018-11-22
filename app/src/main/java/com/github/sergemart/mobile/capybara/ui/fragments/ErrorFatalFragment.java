package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.viewmodel.ErrorSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;


public class ErrorFatalFragment
    extends AbstractFragment {


    private TextView mErrorDetailsTextView;
    private MaterialButton mExitApplicationButton;

    private ErrorSharedViewModel mErrorSharedViewModel;


    // --------------------------- Override fragment event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mErrorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(ErrorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_error_fatal, inflater, container);
        mErrorDetailsTextView = fragmentView.findViewById(R.id.textView_error_details);
        mExitApplicationButton = fragmentView.findViewById(R.id.button_exit_application);

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
            mErrorSharedViewModel.getExitRequestedSubject().onComplete()                            // send ExitRequested event
        ));

        // Set a listener to the Cause event
        pInstanceDisposable.add(mErrorSharedViewModel.getCauseSubject().subscribe(event ->
            this.showErrorMessage(event.getException())
        ));

    }


    // --------------------------- Use cases

    private void showErrorMessage(Throwable cause) {
        String messageToShow = cause.getLocalizedMessage() + " caused by:  " + cause.getCause().getLocalizedMessage();
        mErrorDetailsTextView.setText(messageToShow);
    }
}
