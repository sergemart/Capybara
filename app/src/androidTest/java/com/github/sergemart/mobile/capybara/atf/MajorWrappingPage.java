package com.github.sergemart.mobile.capybara.atf;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import static org.junit.Assert.fail;


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

    private static final UiSelector LR_NAV_HOST = new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/fragment_nav_host_major");


    // --------------------------- Use cases


    // --------------------------- Asserts

    public MajorWrappingPage assertThatPageIsDisplayed() {
        UiObject navHost = mDevice.findObject(LR_NAV_HOST);
        if (!navHost.exists()) fail("'Major' page is not displayed");
        return this;
    }

}
