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

    private static final BySelector LR_I_AM_MAJOR_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_i_am_major");
    private static final BySelector LR_I_AM_MINOR_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_i_am_minor");


    // --------------------------- Use cases

    public InitialCommonSetupPage tellThatIAmMajor() {
        UiObject2 iAmMajorButton = mDevice.findObject(LR_I_AM_MAJOR_BUTTON);
        assertNotNull("'I Am Major' button not found", iAmMajorButton);
        iAmMajorButton.click();
        return this;
    }


    public InitialCommonSetupPage tellThatIAmMinor() {
        UiObject2 iAmMinorButton = mDevice.findObject(LR_I_AM_MINOR_BUTTON);
        assertNotNull("'I Am Minor' button not found", iAmMinorButton);
        iAmMinorButton.click();
        return this;
    }


    // --------------------------- Asserts

    public InitialCommonSetupPage assertThatPageIsDisplayed() {
        UiObject2 iAmMajorButton = mDevice.wait(
            Until.findObject(LR_I_AM_MAJOR_BUTTON),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        UiObject2 iAmMinorButton = mDevice.wait(
            Until.findObject(LR_I_AM_MINOR_BUTTON),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        if (iAmMajorButton == null || iAmMinorButton == null) fail("'Initial Common Setup' page is not displayed");
        return this;
    }

}
