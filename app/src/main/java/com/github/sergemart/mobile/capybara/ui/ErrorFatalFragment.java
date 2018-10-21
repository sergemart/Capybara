package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.viewmodel.ErrorSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class ErrorFatalFragment extends Fragment {

    private static final String TAG = ErrorFatalFragment.class.getSimpleName();

    private TextView mErrorDetailsTextView;
    private MaterialButton mExitApplicationButton;

    private ErrorSharedViewModel mErrorSharedViewModel;
    private CompositeDisposable mViewDisposable;
    private CompositeDisposable mInstanceDisposable;



    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRetainInstance(true);

        mErrorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull( super.getActivity() )).get(ErrorSharedViewModel.class);
        mViewDisposable = new CompositeDisposable();
        mInstanceDisposable = new CompositeDisposable();

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_error_fatal, container, false);

        mErrorDetailsTextView = fragmentView.findViewById(R.id.textView_error_details);
        mExitApplicationButton = fragmentView.findViewById(R.id.button_exit_application);

        this.setViewListeners();
        return fragmentView;
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        mViewDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-related subscriptions are disposed");
        super.onDestroyView();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-unrelated subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {

        // Set a listener to the "Exit Application" button
        mViewDisposable.add(RxView.clicks(mExitApplicationButton).subscribe(event ->
            mErrorSharedViewModel.getExitRequestedSubject().onComplete()                            // send ExitRequested event
        ));

        // Set a listener to the Cause event
        mInstanceDisposable.add(mErrorSharedViewModel.getCauseSubject().subscribe(event ->
            this.showErrorMessage(event.getException())
        ));

    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {


    }


    // --------------------------- Use cases

    private void showErrorMessage(Throwable cause) {
        String messageToShow = cause.getLocalizedMessage() + " caused by:  " + cause.getCause().getLocalizedMessage();
        mErrorDetailsTextView.setText(messageToShow);
    }
}
