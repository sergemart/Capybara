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

    ErrorSharedViewModel mErrorSharedViewModel;
    private CompositeDisposable mEventDisposable;


    // --------------------------- Override activity event handlers


    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        mEventDisposable = new CompositeDisposable();

        String errorDetails = super.getIntent().getStringExtra(KEY_ERROR_DETAILS);
        mErrorSharedViewModel = ViewModelProviders.of(this).get(ErrorSharedViewModel.class);
        mErrorSharedViewModel.getErrorDetailsSubject().onNext(errorDetails);

        this.setEventListeners();
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
        mEventDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "Event subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Activity lifecycle subroutines

    private void setEventListeners() {

        // Set a listener to the "EXIT REQUESTED" event
        mEventDisposable.add(mErrorSharedViewModel.getExitRequestedSubject().subscribe(
            this::exitApplication
        ));

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(KEY_ERROR_DETAILS, errorDetails);
        return intent;
    }

}
