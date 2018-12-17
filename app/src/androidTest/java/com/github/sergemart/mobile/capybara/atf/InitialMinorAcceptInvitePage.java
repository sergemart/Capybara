package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class InitialMinorAcceptInvitePage {

    // Private constructor
    private InitialMinorAcceptInvitePage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static InitialMinorAcceptInvitePage get() {
        return new InitialMinorAcceptInvitePage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mButtonAcceptInvite;
    private UiObject2 mButtonDeclineInvite;


    // --------------------------- Locators

    private static final BySelector LR_ACCEPT_INVITE_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_accept_invite");
    private static final BySelector LR_DECLINE_INVITE_BUTTON = By.res("com.github.sergemart.mobile.capybara:id/button_decline_invite");


    // --------------------------- Asserting widget getters

    private UiObject2 getButtonAcceptInvite() {
        if (mButtonAcceptInvite == null) {
            mButtonAcceptInvite = mDevice.wait(
                Until.findObject(LR_ACCEPT_INVITE_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Accept Invite' button is not displayed", mButtonAcceptInvite);
        }
        return mButtonAcceptInvite;
    }


    private UiObject2 getButtonDeclineInvite() {
        if (mButtonDeclineInvite == null) {
            mButtonDeclineInvite = mDevice.wait(
                Until.findObject(LR_DECLINE_INVITE_BUTTON),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Decline Invite' button is not displayed", mButtonDeclineInvite);
        }
        return mButtonDeclineInvite;
    }


    // --------------------------- Use cases

    public void doAcceptInvite() {
        this.getButtonAcceptInvite().click();
    }


    // --------------------------- Asserts

    public InitialMinorAcceptInvitePage shouldPageBeDisplayed() {
        try {
            this.getButtonAcceptInvite();
            this.getButtonDeclineInvite();
        } catch (AssertionError e) {
            fail("'Accept Invite' page is not displayed");
        }
        return this;
    }

}
