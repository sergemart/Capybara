package com.github.sergemart.mobile.capybara.atf;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.events.Result;
import com.github.sergemart.mobile.capybara.data.repo.MessageRepo;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static com.github.sergemart.mobile.capybara.data.events.Result.SUCCESS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class InitialMinorWaitForInvitePage {

    // Private constructor
    private InitialMinorWaitForInvitePage() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }


    // Factory method
    public static InitialMinorWaitForInvitePage get() {
        return new InitialMinorWaitForInvitePage();
    }


    // --------------------------- Member variables

    private UiDevice mDevice;
    private UiObject2 mWaitTextView;
    private String mMajorPartyEmail;


    // --------------------------- Locators

    private static final BySelector LR_WAIT_TEXT_VIEW = By.res("com.github.sergemart.mobile.capybara:id/textView_wait");


    // --------------------------- Asserting widget getters

    private UiObject2 getWaitTextView() {
        if (mWaitTextView == null) {
            mWaitTextView = mDevice.wait(
                Until.findObject(LR_WAIT_TEXT_VIEW),
                Constants.UI_AUTOMATOR_DEFAULT_TIMEOUT
            );
            assertNotNull("'Wait' textView is not displayed", mWaitTextView);
        }
        return mWaitTextView;
    }


    // --------------------------- Givens

    public InitialMinorWaitForInvitePage givenMajorPartyEmail(String majorPartyEmail) {
        mMajorPartyEmail = majorPartyEmail;
        return this;
    }


    // --------------------------- Use cases

    public void doFakeReceiveInvite() {
        MessageRepo.get().getInviteReceivedSubject().onNext(GenericEvent
            .of(SUCCESS)
            .setData(mMajorPartyEmail)
        );
    }


    // --------------------------- Asserts

    public InitialMinorWaitForInvitePage shouldPageBeDisplayed() {
        try {
            this.getWaitTextView();
        } catch (AssertionError e) {
            fail("'Wait For Invite' page is not displayed");
        }
        return this;
    }

}
