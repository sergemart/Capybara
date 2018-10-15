package com.github.sergemart.mobile.capybara.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.viewmodel.ErrorSharedViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class ErrorActivity
    extends AppCompatActivity
{

    private static final String TAG = ErrorActivity.class.getSimpleName();
    private static final String KEY_ERROR_DETAILS = "errorDetails";

    private CompositeDisposable mDisposable;


    // --------------------------- Override activity event handlers


    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        mDisposable = new CompositeDisposable();

        String errorDetails = super.getIntent().getStringExtra(KEY_ERROR_DETAILS);
        ErrorSharedViewModel errorSharedViewModel = ViewModelProviders.of(this).get(ErrorSharedViewModel.class);
        errorSharedViewModel.emitErrorDetails(errorDetails);

        // Set a listener to the "EXIT REQUESTED" event
        mDisposable.add(errorSharedViewModel.getExitRequestedSubject().subscribe(
            this::exitApplication
        ));

    }


    /**
     * Exit the app on back pressed
     */
    @Override
    public void onBackPressed() {
        this.exitApplication();
    }


    // Instance clean-up
    @Override
    public void onDestroy() {
        mDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Subscriptions are disposed.");
        super.onDestroy();
    }


    // --------------------------- Use cases

    /**
     * Exit the application and finalize its process
     */
    private void exitApplication() {
        super.finishAffinity();
        System.exit(0);
    }


    // --------------------------- Static encapsulation-leveraging methods

    // Create properly configured intent intended to invoke this activity
    public static Intent newIntent(Context packageContext, String errorDetails) {
        Intent intent = new Intent(packageContext, ErrorActivity.class);
        intent.putExtra(KEY_ERROR_DETAILS, errorDetails);
        return intent;
    }

}
