package com.github.sergemart.mobile.capybara;

import com.github.sergemart.mobile.capybara.atf.MajorWrappingPage;
import com.github.sergemart.mobile.capybara.atf.InitialCommonSetupPage;
import com.github.sergemart.mobile.capybara.atf.InitialCommonSignInPage;
import com.github.sergemart.mobile.capybara.atf.CommonLocatorPage;
import com.github.sergemart.mobile.capybara.atf.SuT;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitialCommonWorkflowTests {

    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);


    // --------------------------- Set up / tear down

    @BeforeClass
    public static void SetUpClass() {
        PreferenceStore.storeAppMode(-1);                                                     // should cause app start from the initial WF
        AuthService.get().signOut();
    }


    @AfterClass
    public static void TearDownClass() {
        PreferenceStore.storeAppMode(-1);
        AuthService.get().signOut();
    }


    // --------------------------- Tests

    @Test
    public void initial_Common_Workflow_Performs_Major_Main_Scenario() {
        SuT.get()
            .startApp()
        ;
        InitialCommonSetupPage.get()
            .assertThatPageIsDisplayed()
            .tellThatIAmMajor()
        ;
        InitialCommonSignInPage.get()
            .assertThatPageIsDisplayed()
            .proceedWithSignIn()
            .assertThatSelectAnAccountDialogIsDisplayed()
            .selectTheFirstAccountToSignIn()
        ;
        MajorWrappingPage.get()
            .assertThatPageIsDisplayed()
        ;
        CommonLocatorPage.get()
            .assertThatPageIsDisplayed()
        ;
    }

}
