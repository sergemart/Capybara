package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiCollection;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;


public class InitialCommonSignInPage {

    private static InitialCommonSignInPage sInstance = new InitialCommonSignInPage();


    // Private constructor
    private InitialCommonSignInPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static InitialCommonSignInPage get() {
        if(sInstance == null) sInstance = new InitialCommonSignInPage();
        return sInstance;
    }


    // --------------------------- Member variables

    private UiDevice mDevice;


    // --------------------------- Locators

    private static final UiSelector LR_SIGN_IN_BUTTON = new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/button_sign_in");
    private static final UiSelector LR_SELECT_ACCOUNT_DIALOG = new UiSelector().resourceId("com.google.android.gms:id/account_picker");
    private static final UiSelector LR_ACCOUNT_LIST = new UiSelector().resourceId("android:id/list");


    // --------------------------- Use cases

    public InitialCommonSignInPage proceedWithSignIn() {
        try {
            mDevice.findObject(LR_SIGN_IN_BUTTON).click();
            mDevice.waitForWindowUpdate(BuildConfig.APPLICATION_ID, Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        return this;
    }


    public InitialCommonSignInPage selectTheFirstAccountToSignIn() {
        try {
            UiCollection accountList = new UiCollection(LR_ACCOUNT_LIST);
            UiObject firstAccountInList = accountList.getChild(new UiSelector().index(0));
            firstAccountInList.click();
            mDevice.waitForWindowUpdate(BuildConfig.APPLICATION_ID, Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        return this;
    }


    // --------------------------- Asserts

    public InitialCommonSignInPage assertThatPageIsDisplayed() {
        UiObject signInButton = mDevice.findObject(LR_SIGN_IN_BUTTON);
        if (!signInButton.exists()) fail("'Initial Common Sign-In' page is not displayed");
        return this;
    }


    public InitialCommonSignInPage assertThatSelectAnAccountDialogIsDisplayed() {
        UiObject selectAccountDialog = mDevice.findObject(LR_SELECT_ACCOUNT_DIALOG);
        if (!selectAccountDialog.exists()) fail("'Select An Account' system dialog is not displayed");
        return this;
    }

}
