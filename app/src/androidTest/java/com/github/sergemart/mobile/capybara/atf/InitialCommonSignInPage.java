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

    private static final BySelector LR_SIGN_IN_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_sign_in");
    private static final BySelector LR_SELECT_ACCOUNT_DIALOG = By.res("com.google.android.gms:id/account_picker");
    private static final UiSelector LR_ACCOUNT_LIST = new UiSelector().resourceId("android:id/list");


    // --------------------------- Use cases

    public InitialCommonSignInPage proceedWithSignIn() {
        UiObject2 signInButton = mDevice.findObject(LR_SIGN_IN_BUTTON);
        assertNotNull("'Sign In' button not found", signInButton);
        signInButton.click();
        return this;
    }


    public InitialCommonSignInPage selectTheFirstAccountToSignIn() {
        try {
            UiCollection accountList = new UiCollection(LR_ACCOUNT_LIST);
            UiObject firstAccountInList = accountList.getChild(new UiSelector().index(0));
            firstAccountInList.click();
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        return this;
    }


    // --------------------------- Asserts

    public InitialCommonSignInPage assertThatPageIsDisplayed() {
        UiObject2 signInButton = mDevice.wait(
            Until.findObject(LR_SIGN_IN_BUTTON),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        assertNotNull("'Initial Common Sign-In' page is not displayed", signInButton);
        return this;
    }


    public InitialCommonSignInPage assertThatSelectAnAccountDialogIsDisplayed() {
        UiObject2 selectAccountDialog = mDevice.wait(
            Until.findObject(LR_SELECT_ACCOUNT_DIALOG),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        assertNotNull("'Select An Account' system dialog is not displayed", selectAccountDialog);
        return this;
    }

}
