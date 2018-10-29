package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;


public abstract class AbstractFragment extends Fragment {

    protected String TAG;

    private ImageView mBackgroundImageView;
    protected CompositeDisposable mViewDisposable;
    protected CompositeDisposable mInstanceDisposable;


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

        mViewDisposable = new CompositeDisposable();
        mInstanceDisposable = new CompositeDisposable();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_major_invite, container, false);
        mBackgroundImageView = fragmentView.findViewById(R.id.imageView_background);
        return null;
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        mViewDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroyView() called, view-related subscriptions disposed");
        if (mBackgroundImageView != null )mBackgroundImageView.setImageBitmap(null);                // to avoid memory leak
        super.onDestroyView();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy() called, instance subscriptions are disposed");
        super.onDestroy();
    }

}
