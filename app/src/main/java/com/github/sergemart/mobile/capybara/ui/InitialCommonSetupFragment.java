package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.events.GenericResult;
import com.github.sergemart.mobile.capybara.viewmodel.InitialCommonSharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import io.reactivex.disposables.CompositeDisposable;


public class InitialCommonSetupFragment extends Fragment {

    private static final String TAG = InitialCommonSetupFragment.class.getSimpleName();

    private MaterialButton mIAmMajorButton;
    private MaterialButton mIAmMinorButton;

    private CompositeDisposable mViewDisposable;
    private CompositeDisposable mInstanceDisposable;
    private InitialCommonSharedViewModel mInitialCommonSharedViewModel;


    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        super.setRetainInstance(true);

        mInitialCommonSharedViewModel = ViewModelProviders.of(Objects.requireNonNull( super.getActivity() )).get(InitialCommonSharedViewModel.class);
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
        View fragmentView = inflater.inflate(R.layout.fragment_initial_common_setup, container, false);

        mIAmMajorButton = fragmentView.findViewById(R.id.button_i_am_major);
        mIAmMinorButton = fragmentView.findViewById(R.id.button_i_am_minor);

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
        // Set a listener to the "I Am a Major" button
        mViewDisposable.add(RxView.clicks(mIAmMajorButton).subscribe(
            event -> {
                PreferenceStore.storeAppMode(Constants.APP_MODE_MAJOR);
                PreferenceStore.storeIsAppModeSet(true);
                this.navigateToNextPage();
            })
        );

        // Set a listener to the "I Am a Minor" button
        mViewDisposable.add(RxView.clicks(mIAmMinorButton).subscribe(
            event -> {
                PreferenceStore.storeAppMode(Constants.APP_MODE_MINOR);
                PreferenceStore.storeIsAppModeSet(true);
                this.navigateToNextPage();
            })
        );
    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {
    }


    // --------------------------- Use cases

    /**
     * Navigate to the login page, if not authenticated.
     * Otherwise, notify subscribers that the app is completely initialized
     */
    private void navigateToNextPage() {
        if (!CloudRepo.get().isAuthenticated()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_initialSetup_to_initialSignin);
        } else {
            mInitialCommonSharedViewModel.getCommonSetupFinishedSubject().onNext(GenericResult.SUCCESS);
        }
    }


}
