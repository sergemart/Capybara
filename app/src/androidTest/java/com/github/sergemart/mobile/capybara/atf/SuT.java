package com.github.sergemart.mobile.capybara.atf;

import android.content.Context;
import android.content.Intent;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


public class SuT {

    private static SuT sInstance = new SuT();


    // Private constructor
    private SuT() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static SuT get() {
        if(sInstance == null) sInstance = new SuT();
        return sInstance;
    }

    // --------------------------- Member variables

    private UiDevice mDevice;


    // --------------------------- Use cases

    public SuT startApp() {
        // Start from the home screen
        mDevice.pressHome();
        // Wait for launcher
        final String launcherPackage = mDevice.getLauncherPackageName();
        assertThat(launcherPackage, is(notNullValue()));
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);
        // Launch the app
        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        assertThat(intent, is(notNullValue()));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);                                           // clear out any previous instances
        context.startActivity(intent);
        mDevice.wait(                                                                               // wait for the app to appear
            Until.hasObject(
                By.pkg(BuildConfig.APPLICATION_ID).depth(0)
            ),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
        return this;
    }


}
