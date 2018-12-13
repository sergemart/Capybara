package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;


public class InitialCommonSetupPage {

    private static InitialCommonSetupPage sInstance = new InitialCommonSetupPage();


    // Private constructor
    private InitialCommonSetupPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static InitialCommonSetupPage get() {
        if(sInstance == null) sInstance = new InitialCommonSetupPage();
        return sInstance;
    }


    // --------------------------- Member variables

    private UiDevice mDevice;


    // --------------------------- Locators

    private static final UiSelector LR_I_AM_MAJOR_BUTTON = new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/button_i_am_major");
    private static final UiSelector LR_I_AM_MINOR_BUTTON = new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/button_i_am_minor");


    // --------------------------- Use cases

    public InitialCommonSetupPage tellThatIAmMajor() {
        try {
            mDevice.findObject(LR_I_AM_MAJOR_BUTTON).clickAndWaitForNewWindow(Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        return this;
    }


    // --------------------------- Asserts

    public InitialCommonSetupPage assertThatPageIsDisplayed() {
        UiObject iAmMajorButton = mDevice.findObject(LR_I_AM_MAJOR_BUTTON);
        UiObject iAmMinorButton = mDevice.findObject(LR_I_AM_MINOR_BUTTON);
        if (!iAmMajorButton.exists() || !iAmMinorButton.exists()) fail("'Initial Common Setup' page is not displayed");
        return this;
    }

}
