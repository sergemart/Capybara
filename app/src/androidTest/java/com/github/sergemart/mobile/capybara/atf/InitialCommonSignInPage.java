package com.github.sergemart.mobile.capybara.atf;

import android.content.Context;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiCollection;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class InitialCommonSignInPage {

    private static Context sContext = ApplicationProvider.getApplicationContext();


    // Private constructor
    private InitialCommonSignInPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static InitialCommonSignInPage get() {
        return new InitialCommonSignInPage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mSignInButton;
    private UiObject2 mSelectAccountDialog;
    private UiObject mFirstAccountInList;
    private UiObject2 mRetrySignInDialog;
    private UiObject2 mRetrySignInButton;
    private UiObject2 mCancelSignInButton;


    // --------------------------- Locators

    private static final BySelector LR_SIGN_IN_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_sign_in");
    private static final BySelector LR_SELECT_ACCOUNT_DIALOG = By.res("com.google.android.gms:id/account_picker");
    private static final UiSelector LR_ACCOUNT_LIST = new UiSelector().resourceId("android:id/list");
    private static final BySelector LR_RETRY_SIGN_IN_DIALOG = By.text(sContext.getString(R.string.msg_google_signin_canceled_by_user));
    private static final BySelector LR_RETRY_SIGN_IN_BUTTON = By.text(sContext.getString(R.string.action_retry));
    private static final BySelector LR_CANCEL_SIGN_IN_BUTTON = By.text(sContext.getString(R.string.action_thanks_no));


    // --------------------------- Asserting widget getters

    private UiObject2 getSignInButton() {
        if (mSignInButton == null) {
            mSignInButton = mDevice.wait(
                Until.findObject(LR_SIGN_IN_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Sign In' button is not displayed", mSignInButton);
        }
        return mSignInButton;
    }


    private UiObject2 getSelectAccountDialog() {
        if (mSelectAccountDialog == null) {
            mSelectAccountDialog = mDevice.wait(
                Until.findObject(LR_SELECT_ACCOUNT_DIALOG),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Select An Account' system dialog is not displayed", mSelectAccountDialog);
        }
        return mSelectAccountDialog;
    }


    private UiObject getFirstAccountInList() {
        if (mFirstAccountInList == null) {
            this.getSelectAccountDialog();
            try {
                UiCollection accountList = new UiCollection(LR_ACCOUNT_LIST);
                mFirstAccountInList = accountList.getChild(new UiSelector().index(0));
            } catch (UiObjectNotFoundException e) {
                fail(e.getMessage());
            }
            assertNotNull("An account list is not displayed", mFirstAccountInList);
        }
        return mFirstAccountInList;
    }


    private UiObject2 getRetrySignInDialog() {
        if (mRetrySignInDialog == null) {
            mRetrySignInDialog = mDevice.wait(
                Until.findObject(LR_RETRY_SIGN_IN_DIALOG),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Retry Sign-In' dialog is not displayed", mRetrySignInDialog);
        }
        return mRetrySignInDialog;
    }


    private UiObject2 getRetrySignInButton() {
        if (mRetrySignInButton == null) {
            mRetrySignInButton = mDevice.wait(
                Until.findObject(LR_RETRY_SIGN_IN_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Retry Sign-In' button is not displayed", mRetrySignInButton);
        }
        return mRetrySignInButton;
    }


    private UiObject2 getCancelSignInButton() {
        if (mCancelSignInButton == null) {
            mCancelSignInButton = mDevice.wait(
                Until.findObject(LR_CANCEL_SIGN_IN_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Cancel Sign-In' button is not displayed", mCancelSignInButton);
        }
        return mCancelSignInButton;
    }


    // --------------------------- Use cases

    public InitialCommonSignInPage doProceedWithSignIn() {
        this.getSignInButton().click();
        return this;
    }


    public InitialCommonSignInPage doSelectTheFirstAccountToSignIn() {
        try {
            this.getFirstAccountInList().click();
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        return this;
    }


    public InitialCommonSignInPage doRetrySignIn() {
        this.getRetrySignInButton().click();
        return this;
    }


    public InitialCommonSignInPage doCancelSignIn() {
        this.getCancelSignInButton().click();
        return this;
    }


    public InitialCommonSignInPage doCancelSelectAccountDialogByPressingBackButton() {
        mDevice.pressBack();
        return this;
    }


    public InitialCommonSignInPage doCancelSelectAccountDialogByPressingOutsideOne() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDevice.click(mDevice.getDisplayWidth() - 2,mDevice.getDisplayHeight() - 2);
        return this;
    }


    // --------------------------- Asserts

    public InitialCommonSignInPage shouldPageBeDisplayed() {
        try {
            this.getSignInButton();
        } catch (AssertionError e) {
            fail("'Initial Common Sign-In' page is not displayed");
        }
        return this;
    }


    public InitialCommonSignInPage shouldSelectAccountDialogBeDisplayed() {
        this.getSelectAccountDialog();
        return this;
    }


    public InitialCommonSignInPage shouldRetrySignInDialogBeDisplayed() {
        this.getRetrySignInDialog();
        return this;
    }

}
