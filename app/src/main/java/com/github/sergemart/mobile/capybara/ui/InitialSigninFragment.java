package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.PreferenceStore;
import com.github.sergemart.mobile.capybara.viewmodel.SharedStartupViewModel;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import io.reactivex.disposables.CompositeDisposable;


public class InitialSigninFragment extends Fragment {

    private static final String TAG = InitialSigninFragment.class.getSimpleName();

    private MaterialButton mSignInButton;

    private CompositeDisposable mDisposable;
    private SharedStartupViewModel mSharedStartupViewModel;


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
        View fragmentView = inflater.inflate(R.layout.fragment_initial_signin, container, false);

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
        mSignInButton = fragmentView.findViewById(R.id.button_sign_in);

        mDisposable = new CompositeDisposable();
        mSharedStartupViewModel = ViewModelProviders.of(Objects.requireNonNull(super.getActivity())).get(SharedStartupViewModel.class);
    }


    /**
     * Set attributes
     */
    private void setAttributes() {
    }


    /**
     * Set listeners to widgets and containers
     */
    private void setListeners() {
        // Set a listener to the "Sign In" button
        mDisposable.add(
            RxView.clicks(mSignInButton).subscribe(event -> this.signIn())
        );

        // Set a listener to the "SIGNED IN" event
        mDisposable.add(CloudRepo.get().getSigninSubject()
            .subscribe(event -> this.navigateToNextPage()) // TODO: Implement onError
        );
    }


    // --------------------------- Use cases

    /**
     * Sign in with Google account
     */
    private void signIn() {
        CloudRepo.get().sendSignInIntent(Objects.requireNonNull( super.getActivity() ));
    }


    /**
     * Return to the initial setup page, if the app mode is not set.
     * Otherwise, notify subscribers that the app is completely initialized
     */
    private void navigateToNextPage() {
        if (!PreferenceStore.getStoredIsAppModeSet()) {
            NavHostFragment.findNavController(this).popBackStack();
        } else {
            mSharedStartupViewModel.emitAppIsInitialized();
        }
    }

}
