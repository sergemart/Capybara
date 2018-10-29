package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.github.sergemart.mobile.capybara.BuildConfig;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;


public abstract class AbstractFragment extends Fragment {

    protected String TAG;

    ImageView pBackgroundImageView;
    CompositeDisposable pViewDisposable;
    CompositeDisposable pInstanceDisposable;


    // --------------------------- Override fragment lifecycle event handlers

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
        if (pBackgroundImageView != null) pBackgroundImageView.setImageBitmap(null);                // to avoid memory leak
        super.onDestroyView();
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


}
