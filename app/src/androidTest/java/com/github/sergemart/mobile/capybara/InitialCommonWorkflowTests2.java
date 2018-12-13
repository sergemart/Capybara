package com.github.sergemart.mobile.capybara;

import android.content.Context;
import android.content.Intent;

import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiCollection;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitialCommonWorkflowTests2 {

    private static UiDevice sDevice;

//    @Rule
//    public ActivityTestRule<InitialCommonActivity> mActivityRule = new ActivityTestRule<>(InitialCommonActivity.class);
    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);


    // --------------------------- Set up / tear down

    @BeforeClass
    public static void SetUpClass() {
        PreferenceStore.storeAppMode(-1);                                                     // should cause app start from the initial WF
        AuthService.get().signOut();

        sDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    @Before
    public void SetUpTest() {
        // Start from the home screen
        sDevice.pressHome();
        // Wait for launcher
        final String launcherPackage = sDevice.getLauncherPackageName();
        assertThat(launcherPackage, is(notNullValue()));
        sDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);
        // Launch the app
        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        assertThat(intent, is(notNullValue()));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);                                           // clear out any previous instances
        context.startActivity(intent);
        sDevice.wait(                                                                               // wait for the app to appear
            Until.hasObject(
                By.pkg(BuildConfig.APPLICATION_ID).depth(0)
            ),
            Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
        );
    }


    @AfterClass
    public static void TearDownClass() {
        PreferenceStore.storeAppMode(-1);                                                     // should cause app start from the initial WF
        AuthService.get().signOut();
    }


    // --------------------------- Tests

    @Test
    public void initial_Common_Workflow_Performs_Major_Main_Scenario()
        throws UiObjectNotFoundException
    {
        // "Initial Setup" page is displayed
        UiObject iAmMajorButton = sDevice.findObject(new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/button_i_am_major"));
        assertThat(iAmMajorButton.exists(), is(true));
        UiObject iAmMinorButton = sDevice.findObject(new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/button_i_am_minor"));
        assertThat(iAmMinorButton.exists(), is(true));

        // Click "I am Major" button
        iAmMajorButton.clickAndWaitForNewWindow(Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);

        // "Initial Sign In" page is displayed
        UiObject signInButton = sDevice.findObject(new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/button_sign_in"));
        assertThat(signInButton.exists(), is(true));

        // Click "Sign In" button
        signInButton.click();
        sDevice.waitForWindowUpdate(BuildConfig.APPLICATION_ID, Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);

        // "Select an Account" system dialog is displayed
        UiObject selectAccountDialog = sDevice.findObject(new UiSelector().resourceId("com.google.android.gms:id/account_picker"));
        assertThat(selectAccountDialog.exists(), is(true));

        // Click the first account displayed on the dialog
        UiCollection accountList = new UiCollection(new UiSelector().resourceId("android:id/list"));
        UiObject firstAccountInList = accountList.getChild(new UiSelector().index(0));
        firstAccountInList.click();
        sDevice.waitForWindowUpdate(BuildConfig.APPLICATION_ID, Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT);

        // Major nav host fragment is displayed
        UiObject navHostFragment = sDevice.findObject(new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/fragment_nav_host_major"));
        assertThat(navHostFragment.exists(), is(true));

        // Locator fragment is displayed
        UiObject locatorFragment = sDevice.findObject(new UiSelector().resourceId("com.github.sergemart.mobile.capybara:id/fragment_map"));
        assertThat(locatorFragment.exists(), is(true));
    }

}
