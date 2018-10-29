package com.github.sergemart.mobile.capybara.ui;

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
     * Instance start-up
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");

        mInstanceDisposable = new CompositeDisposable();
    }


    // Instance clean-up
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Instance subscriptions are disposed");
        super.onDestroy();
    }

}
