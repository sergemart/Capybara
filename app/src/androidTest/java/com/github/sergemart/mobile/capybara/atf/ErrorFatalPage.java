package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class ErrorFatalPage {

    // Private constructor
    private ErrorFatalPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static ErrorFatalPage get() {
        return new ErrorFatalPage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mErrorHeader;
    private UiObject2 mExitButton;


    // --------------------------- Locators

    private static final BySelector LR_ERROR_HEADER = By.res("com.github.sergemart.mobile.capybara:id/textView_error_header");
    private static final BySelector LR_EXIT_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_exit_application");


    // --------------------------- Asserting widget getters

    private UiObject2 getErrorHeader() {
        if (mErrorHeader == null) {
            mErrorHeader = mDevice.wait(
                Until.findObject(LR_ERROR_HEADER),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Error Header' textView is not displayed", mErrorHeader);
        }
        return mErrorHeader;
    }


    private UiObject2 getExitButton() {
        if (mExitButton == null) {
            mExitButton = mDevice.wait(
                Until.findObject(LR_EXIT_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Exit Application' button is not displayed", mExitButton);
        }
        return mExitButton;
    }


    // --------------------------- Use cases

    public void doExitApplication() {
        this.getExitButton().click();
    }


    // --------------------------- Asserts

    public ErrorFatalPage shouldPageBeDisplayed() {
        try {
            this.getErrorHeader();
            this.getExitButton();
        } catch (AssertionError e) {
            fail("'Fatal Error' page is not displayed");
        }
        return this;
    }

}
