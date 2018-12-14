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

    private static MajorWrappingPage sInstance = new MajorWrappingPage();


    // Private constructor
    private MajorWrappingPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static MajorWrappingPage get() {
        if(sInstance == null) sInstance = new MajorWrappingPage();
        return sInstance;
    }


    // --------------------------- Member variables

    private UiDevice mDevice;


    // --------------------------- Locators

    private static final BySelector LR_NAV_HOST = By.res("com.github.sergemart.mobile.capybara:id/fragment_nav_host_major");


    // --------------------------- Use cases


    // --------------------------- Asserts

    public MajorWrappingPage assertThatPageIsDisplayed() {
        UiObject2 navHost = mDevice.wait(
            Until.findObject(LR_NAV_HOST),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        assertNotNull("'Major' page is not displayed", navHost);
        return this;
    }

}
