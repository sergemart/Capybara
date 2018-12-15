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

    // Private constructor
    private CommonLocatorPage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static CommonLocatorPage get() {
        return new CommonLocatorPage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mMapFragment;


    // --------------------------- Locators

    private static final BySelector LR_MAP_FRAGMENT = By.res("com.github.sergemart.mobile.capybara:id/fragment_map");


    // --------------------------- Asserting widget getters

    private UiObject2 getMapFragment() {
        if (mMapFragment == null) {
            mMapFragment = mDevice.wait(
                Until.findObject(LR_MAP_FRAGMENT),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Locator' page is not displayed", mMapFragment);
        }
        return mMapFragment;
    }


    // --------------------------- Use cases


    // --------------------------- Asserts

    public CommonLocatorPage shouldPageBeDisplayed() {
        this.getMapFragment();
        return this;
    }

}
