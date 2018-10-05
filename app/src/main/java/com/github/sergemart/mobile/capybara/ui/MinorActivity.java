package com.github.sergemart.mobile.capybara.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.sergemart.mobile.capybara.R;

import androidx.appcompat.app.AppCompatActivity;


public class MinorActivity
    extends AppCompatActivity
{

    private static final String TAG = MinorActivity.class.getSimpleName();


    // --------------------------- Override activity event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor);
    }


    // --------------------------- Static encapsulation-leveraging methods

    // Create properly configured intent intended to invoke this activity
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MinorActivity.class);
    }

}
