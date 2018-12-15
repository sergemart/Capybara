package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;

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


    // --------------------------- Locators

    private static final BySelector LR_SIGN_IN_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_sign_in");
    private static final BySelector LR_SELECT_ACCOUNT_DIALOG = By.res("com.google.android.gms:id/account_picker");
    private static final UiSelector LR_ACCOUNT_LIST = new UiSelector().resourceId("android:id/list");


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


    // --------------------------- Asserts

    public InitialCommonSignInPage shouldPageBeDisplayed() {
        try {
            this.getSignInButton();
        } catch (AssertionError e) {
            fail("'Initial Common Sign-In' page is not displayed");
        }
        return this;
    }


    public InitialCommonSignInPage shouldSelectAnAccountDialogBeDisplayed() {
        this.getSelectAccountDialog();
        return this;
    }

}
