package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;


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

    private static final BySelector LR_MAP_FRAGMENT = By.res("com.github.sergemart.mobile.capybara:id/fragment_map");


    // --------------------------- Use cases


    // --------------------------- Asserts

    public CommonLocatorPage assertThatPageIsDisplayed() {
        UiObject2 mapFragment = mDevice.wait(
            Until.findObject(LR_MAP_FRAGMENT),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        assertNotNull("'Map' page is not displayed", mapFragment);
        return this;
    }

}
