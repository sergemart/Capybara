package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;


public class MajorWrappingPage {

    // Private constructor
    private MajorWrappingPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static MajorWrappingPage get() {
        return new MajorWrappingPage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mNavHostFragment;


    // --------------------------- Locators

    private static final BySelector LR_NAV_HOST = By.res("com.github.sergemart.mobile.capybara:id/fragment_nav_host_major");


    // --------------------------- Asserting widget getters

    private UiObject2 getNavHostFragment() {
        if (mNavHostFragment == null) {
            mNavHostFragment = mDevice.wait(
                Until.findObject(LR_NAV_HOST),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Major' page is not displayed", mNavHostFragment);
        }
        return mNavHostFragment;
    }


    // --------------------------- Use cases


    // --------------------------- Asserts

    public MajorWrappingPage shouldPageBeDisplayed() {
        this.getNavHostFragment();
        return this;
    }

}
