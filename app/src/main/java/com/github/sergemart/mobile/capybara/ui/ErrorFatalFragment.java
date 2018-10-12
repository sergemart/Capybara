package com.github.sergemart.mobile.capybara.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.viewmodel.SharedErrorViewModel;
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

    private SharedErrorViewModel mSharedErrorViewModel;
    private CompositeDisposable mDisposable;


    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_error_fatal, container, false);

        this.initMemberVariables(fragmentView);
        this.setAttributes();
        this.setListeners();

        return fragmentView;
    }


    // Instance clean-up
    @Override
    public void onDestroy() {
        mDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Subscriptions are disposed.");
        super.onDestroy();
    }


    // --------------------------- Widget controls

    /**
     * Init member variables
     */
    private void initMemberVariables(View fragmentView) {
        mErrorDetailsTextView = fragmentView.findViewById(R.id.textView_error_details);
        mExitApplicationButton = fragmentView.findViewById(R.id.button_exit_application);

        mSharedErrorViewModel = ViewModelProviders.of(Objects.requireNonNull( super.getActivity() )).get(SharedErrorViewModel.class);
        mDisposable = new CompositeDisposable();
    }


    /**
     * Set attributes
     */
    private void setAttributes() {
    }


    /**
     * Set listeners to widgets, containers and events
     */
    private void setListeners() {
        // Set a listener to the "Exit Application" button
        mDisposable.add(RxView.clicks(mExitApplicationButton).subscribe(
            event -> mSharedErrorViewModel.emitExitRequested()
        ));

        // Set a listener to the "ERROR DETAILS PUBLISHED" event
        mDisposable.add(mSharedErrorViewModel.getErrorDetailsSubject().subscribe(
            errorDetails -> mErrorDetailsTextView.setText(errorDetails)
        ));
    }

}
