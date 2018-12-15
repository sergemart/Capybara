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

    // Private constructor
    private InitialCommonSetupPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static InitialCommonSetupPage get() {
        return new InitialCommonSetupPage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mIAmMajorButton;
    private UiObject2 mIAmMinorButton;


    // --------------------------- Locators

    private static final BySelector LR_I_AM_MAJOR_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_i_am_major");
    private static final BySelector LR_I_AM_MINOR_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_i_am_minor");


    // --------------------------- Asserting widget getters

    private UiObject2 getIAmMajorButton() {
        if (mIAmMajorButton == null) {
            mIAmMajorButton = mDevice.wait(
                Until.findObject(LR_I_AM_MAJOR_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'I Am Major' button is not displayed", mIAmMajorButton);
        }
        return mIAmMajorButton;
    }


    private UiObject2 getIAmMinorButton() {
        if (mIAmMinorButton == null) {
            mIAmMinorButton = mDevice.wait(
                Until.findObject(LR_I_AM_MINOR_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'I Am Minor' button is not displayed", mIAmMinorButton);
        }
        return mIAmMinorButton;
    }


    // --------------------------- Use cases

    public InitialCommonSetupPage doTellThatIAmMajor() {
        this.getIAmMajorButton().click();
        return this;
    }


    public InitialCommonSetupPage doTellThatIAmMinor() {
        this.getIAmMinorButton().click();
        return this;
    }


    // --------------------------- Asserts

    public InitialCommonSetupPage shouldPageBeDisplayed() {
        try {
            this.getIAmMajorButton();
            this.getIAmMinorButton();
        } catch (AssertionError e) {
            fail("'Initial Common Setup' page is not displayed");
        }
        return this;
    }

}
