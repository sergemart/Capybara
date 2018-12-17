package com.github.sergemart.mobile.capybara;

import com.github.sergemart.mobile.capybara.atf.CommonLocatorPage;
import com.github.sergemart.mobile.capybara.atf.ErrorFatalPage;
import com.github.sergemart.mobile.capybara.atf.InitialCommonSetupPage;
import com.github.sergemart.mobile.capybara.atf.InitialCommonSignInPage;
import com.github.sergemart.mobile.capybara.atf.InitialMinorAcceptInvitePage;
import com.github.sergemart.mobile.capybara.atf.InitialMinorWaitForInvitePage;
import com.github.sergemart.mobile.capybara.atf.MajorWrappingPage;
import com.github.sergemart.mobile.capybara.atf.MinorWrappingPage;
import com.github.sergemart.mobile.capybara.atf.SuT;
import com.github.sergemart.mobile.capybara.atf.TestTools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
//    @Rule
//    public ActivityTestRule<InitialCommonActivity> mActivityRule = new ActivityTestRule<>(InitialCommonActivity.class);

    private static String sMajorPartyEmail;
    private static String sMinorPartyEmail;


    // --------------------------- Set up / tear down

    @BeforeClass
    public static void setUpClass() {
        sMajorPartyEmail = TestTools.get().getRandomEmail();
        sMinorPartyEmail = TestTools.get().getRandomEmail();

        SuT.get()
            .givenUserEmail(sMajorPartyEmail)
            .doCreateUser()
            .doCreateFamily()
            .givenUserEmail(sMinorPartyEmail)
            .doCreateUser()
        ;
    }


    @Before
    public void setUpTest() {
    }


    @After
    public void tearDownTest() {
    }


    @AfterClass
    public static void TearDownClass() {
        SuT.get()
            .givenUserEmail(sMajorPartyEmail)
            .doDeleteFamily()
            .doDeleteUser()
            .givenUserEmail(sMinorPartyEmail)
            .doDeleteUser()
            .doResetApp()
        ;
    }


    // --------------------------- Tests

    @Test
    public void performs_Major_Main_Scenario_With_Phone_Account() {
        SuT.get()
            .doResetApp()                                                                           // should cause app start from the initial WF
            .doStartApp()
        ;
        InitialCommonSetupPage.get()
            .shouldPageBeDisplayed()
            .doTellThatIAmMajor()
        ;
        InitialCommonSignInPage.get()
            .shouldPageBeDisplayed()
            .doProceedWithSignIn()
            .shouldSelectAccountDialogBeDisplayed()
            .doSelectTheFirstAccountToSignIn()
        ;
        MajorWrappingPage.get()
            .shouldPageBeDisplayed()
        ;
        CommonLocatorPage.get()
            .shouldPageBeDisplayed()
        ;
    }


    @Test
    public void performs_Major_Decline_Sign_In_Scenario_With_Phone_Account() {
        SuT.get()
            .doResetApp()                                                                           // should cause app start from the initial WF
            .doStartApp()
        ;
        InitialCommonSetupPage.get()
            .shouldPageBeDisplayed()
            .doTellThatIAmMajor()
        ;
        InitialCommonSignInPage.get()
            .shouldPageBeDisplayed()
            .doProceedWithSignIn()
            .shouldSelectAccountDialogBeDisplayed()
            .doCancelSelectAccountDialogByPressingBackButton()
            .shouldRetrySignInDialogBeDisplayed()
            .doRetrySignIn()
            .shouldSelectAccountDialogBeDisplayed()
            .doCancelSelectAccountDialogByPressingOutsideOne()
            .shouldRetrySignInDialogBeDisplayed()
            .doCancelSignIn()
        ;
        ErrorFatalPage.get()
            .shouldPageBeDisplayed()
        ;
    }


    @Test
    public void performs_Minor_Main_Scenario_With_Phone_Account() {
        SuT.get()
            .doResetApp()                                                                           // should cause app start from the initial WF
            .doStartApp()
        ;
        InitialCommonSetupPage.get()
            .shouldPageBeDisplayed()
            .doTellThatIAmMinor()
        ;
        InitialCommonSignInPage.get()
            .shouldPageBeDisplayed()
            .doProceedWithSignIn()
            .shouldSelectAccountDialogBeDisplayed()
            .doSelectTheFirstAccountToSignIn()
        ;
        InitialMinorWaitForInvitePage.get()
            .shouldPageBeDisplayed()
            .givenMajorPartyEmail(sMajorPartyEmail)
            .doFakeReceiveInvite()
        ;
        InitialMinorAcceptInvitePage.get()
            .shouldPageBeDisplayed()
            .doAcceptInvite()
        ;
        MinorWrappingPage.get()
            .shouldPageBeDisplayed()
        ;
        CommonLocatorPage.get()
            .shouldPageBeDisplayed()
        ;
    }

}
