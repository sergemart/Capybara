package com.github.sergemart.mobile.capybara.ui.fragments;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;


public abstract class AbstractFragment extends Fragment {

    protected String TAG;

    private ImageView mBackgroundImageView;
    private ImageView mWaitingImageView;
    private ProgressBar mProgressBar;
    private AnimationDrawable mWaitingAnimationDrawable;
    private ViewGroup mWaitingLayout;

    AppCompatActivity pActivity;
    CompositeDisposable pViewDisposable;
    CompositeDisposable pInstanceDisposable;


    // --------------------------- Override fragment lifecycle event handlers


    /**
     * Activity attach actions
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        pActivity = (AppCompatActivity) super.getActivity();
    }


    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        super.setRetainInstance(true);

        pViewDisposable = new CompositeDisposable();
        pInstanceDisposable = new CompositeDisposable();
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        pViewDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroyView() called, view-related subscriptions disposed");

        // Avoid memory leaks
        if (mBackgroundImageView != null) mBackgroundImageView.setImageBitmap(null);
        if (mWaitingImageView != null) {
            mWaitingImageView.setImageBitmap(null);
            mWaitingImageView.setBackground(null);
        }

        super.onDestroyView();
    }


    /**
     * Stop actions
     */
    @Override
    public void onStop() {
        if (mWaitingAnimationDrawable != null) mWaitingAnimationDrawable.stop();
        super.onStop();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        pInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy() called, instance subscriptions disposed");
        super.onDestroy();
    }


    /**
     * Activity detach actions
     */
    @Override
    public void onDetach() {
        pActivity = null;
        super.onDetach();
    }


    // --------------------------- Inheritance helpers

    /**
     * Inflate shared widgets. Called from the child's onViewCreated
     */
    View inflateFragment(int fragmentLayoutId, LayoutInflater inflater, ViewGroup container) {
        View fragmentView = inflater.inflate(fragmentLayoutId, container, false);
        mBackgroundImageView = fragmentView.findViewById(R.id.imageView_background);
        mWaitingLayout = fragmentView.findViewById(R.id.layout_waiting);
        mWaitingImageView = fragmentView.findViewById(R.id.imageView_waiting);
        mProgressBar = fragmentView.findViewById(R.id.progressBar_waiting);

        if (mWaitingImageView != null){
            mWaitingImageView.setBackgroundResource(R.drawable.waiting);
            mWaitingAnimationDrawable = (AnimationDrawable) mWaitingImageView.getBackground();
        }

        return fragmentView;
    }


    /**
     * Show widgets indicating a waiting state
     */
    void showWaitingState() {
        if (mWaitingLayout != null) mWaitingLayout.setVisibility(View.VISIBLE);
        if (mWaitingAnimationDrawable != null) mWaitingAnimationDrawable.start();
    }


    /**
     * Hide widgets indicating a waiting state
     */
    void hideWaitingState() {
        if (mWaitingAnimationDrawable != null) mWaitingAnimationDrawable.stop();
        if (mWaitingLayout != null) mWaitingLayout.setVisibility(View.GONE);
    }
}
