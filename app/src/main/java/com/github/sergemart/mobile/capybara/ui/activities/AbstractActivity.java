package com.github.sergemart.mobile.capybara.ui.activities;

import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;


public abstract class AbstractActivity
    extends AppCompatActivity
{

    protected String TAG;
    protected CompositeDisposable mInstanceDisposable;


    // --------------------------- Override activity event handlers

    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");

        mInstanceDisposable = new CompositeDisposable();
    }


    /**
     * Getting visible
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart() called");
    }


    /**
     * Getting hidden
     */
    @Override
    protected void onStop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStop() called");
        super.onStop();
    }


    /**
     *  Instance clean-up actions
     */
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy() called, instance subscriptions disposed");
        super.onDestroy();
    }

}
