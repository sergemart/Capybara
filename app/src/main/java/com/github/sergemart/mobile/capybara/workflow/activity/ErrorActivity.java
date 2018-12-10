package com.github.sergemart.mobile.capybara.workflow.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.viewmodel.ErrorSharedViewModel;

import androidx.lifecycle.ViewModelProviders;

import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;


public class ErrorActivity
    extends AbstractActivity
{

    ErrorSharedViewModel mSharedViewModel;


    // --------------------------- Override activity event handlers


    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_error);

        // Publish the error cause
        mSharedViewModel = ViewModelProviders.of(this).get(ErrorSharedViewModel.class);
        if (App.getLastFatalException() != null) {
            Throwable cause = App.getLastFatalException().get();
            mSharedViewModel.getCauseSubject().onNext(GenericEvent.of(SUCCESS).setException(cause));
        }
        this.setInstanceListeners();
    }


    /**
     * Exit the app on back pressed
     */
    @Override
    public void onBackPressed() {
        this.exitApplication();
    }


    // --------------------------- Activity lifecycle subroutines

    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        // Set a listener to the ExitRequested event
        pInstanceDisposable.add(mSharedViewModel.getExitRequestedSubject().subscribe(
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

    /**
     * Create properly configured intent intended to invoke this activity
     */
    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

}
