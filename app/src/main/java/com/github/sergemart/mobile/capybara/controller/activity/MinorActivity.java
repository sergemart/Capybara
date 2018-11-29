package com.github.sergemart.mobile.capybara.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.sergemart.mobile.capybara.R;


public class MinorActivity
    extends AbstractActivity
{

    // --------------------------- Override activity event handlers

    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_minor);
    }


    // --------------------------- Static encapsulation-leveraging methods


    /**
     * Create properly configured intent intended to invoke this activity
     */
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MinorActivity.class);
    }

}
