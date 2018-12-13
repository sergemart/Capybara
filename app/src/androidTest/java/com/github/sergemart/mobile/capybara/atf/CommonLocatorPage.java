package com.github.sergemart.mobile.capybara.atf;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import static org.junit.Assert.fail;


public class CommonLocatorPage {

    private static CommonLocatorPage sInstance = new CommonLocatorPage();


    // Private constructor
    private CommonLocatorPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static CommonLocatorPage get() {
        if(sInstance == null) sInstance = new CommonLocatorPage();
        return sInstance;
    }


    // --------------------------- Member variables

    private UiDevice mDevice;


    // --------------------------- Locators

    private static final UiSelector LR_MAP_FRAGMENT = new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/fragment_map");


    // --------------------------- Use cases


    // --------------------------- Asserts

    public CommonLocatorPage assertThatPageIsDisplayed() {
        UiObject mapFragment = mDevice.findObject(LR_MAP_FRAGMENT);
        if (!mapFragment.exists()) fail("'Map' page is not displayed");
        return this;
    }

}
