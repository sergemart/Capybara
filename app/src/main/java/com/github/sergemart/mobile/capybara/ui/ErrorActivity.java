package com.github.sergemart.mobile.capybara.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.events.GenericResult;
import com.github.sergemart.mobile.capybara.viewmodel.ErrorSharedViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.disposables.CompositeDisposable;


public class ErrorActivity
    extends AppCompatActivity
{

    private static final String TAG = ErrorActivity.class.getSimpleName();

    ErrorSharedViewModel mErrorSharedViewModel;
    private CompositeDisposable mInstanceDisposable;


    // --------------------------- Override activity event handlers


    /**
     * Start-up actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        mInstanceDisposable = new CompositeDisposable();

        // Publish the error cause
        mErrorSharedViewModel = ViewModelProviders.of(this).get(ErrorSharedViewModel.class);
        Throwable cause = App.getLastFatalException().get();
        mErrorSharedViewModel.getCauseSubject().onNext(GenericResult.SUCCESS.setException(cause));

        this.setInstanceListeners();
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
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-unrelated subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Activity lifecycle subroutines

    private void setInstanceListeners() {

        // Set a listener to the "EXIT REQUESTED" event
        mInstanceDisposable.add(mErrorSharedViewModel.getExitRequestedSubject().subscribe(
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
    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

}
