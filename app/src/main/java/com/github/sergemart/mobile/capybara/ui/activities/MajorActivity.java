package com.github.sergemart.mobile.capybara.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;

import androidx.appcompat.app.AppCompatActivity;


public class MajorActivity
    extends AppCompatActivity
{

    private static final String TAG = MajorActivity.class.getSimpleName();


    // --------------------------- Override activity event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_major);
    }


    /**
     * Start up actions, incl. entry point routing
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart() called");

    }


    // --------------------------- Static encapsulation-leveraging methods

    // Create properly configured intent intended to invoke this activity
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MajorActivity.class);
    }

}
