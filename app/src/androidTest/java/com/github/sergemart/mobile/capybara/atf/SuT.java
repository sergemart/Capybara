package com.github.sergemart.mobile.capybara.atf;

import android.content.Context;
import android.content.Intent;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.github.sergemart.mobile.capybara.data.datastore.FirestoreService;
import com.github.sergemart.mobile.capybara.data.datastore.FunctionsService;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.model.CurrentUser;
import com.github.sergemart.mobile.capybara.data.repo.CurrentUserRepo;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


public class SuT {

    // Private constructor
    private SuT() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mUserEmail = TestTools.get().getRandomEmail();
        mUserPassword = Constants.TEST_USER_PASSWORD;
        mAppMode = -1;
    }


    // Factory method
    public static SuT get() {
        return new SuT();
    }


    // --------------------------- Member variables

    private final UiDevice mDevice;
    private String mUserEmail;
    private String mUserPassword;
    private int mAppMode;


    // --------------------------- Givens

    public SuT givenUserEmail(String email) {
        mUserEmail = email;
        return this;
    }


    public SuT givenUserPassword(String password) {
        mUserPassword = password;
        return this;
    }


    public SuT givenAppMode(int appMode) {
        mAppMode = appMode;
        return this;
    }


    // --------------------------- Use cases

    public SuT doResetApp() {
        PreferenceStore.storeAppMode(-1);
        PreferenceStore.storeFamilyJoined(false);
        AuthService.get().signOut();
        return this;
    }


    public SuT doStartApp() {
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


    public SuT doSignIn() {
        AuthService.get().signInWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        return this;
    }


    public SuT doSignOut() {
        AuthService.get().signOut();
        return this;
    }


    public SuT doCreateUser() {
        AuthService.get().createUserWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        AuthService.get().signInWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        CurrentUser currentUser = new CurrentUser();
        currentUser.setAppMode(mAppMode);
        currentUser.setDeviceToken(AuthService.get().getCurrentDeviceToken());
        currentUser.setFake(true);
        CurrentUserRepo.get().updateAsync(currentUser).blockingAwait();
        return this;
    }


    public SuT doDeleteUser() {
        AuthService.get().signInWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        FirestoreService.get().deleteCurrentUserAsync().blockingAwait();
        AuthService.get().deleteUser().blockingAwait();
        return this;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SuT doCreateFamily() {
        AuthService.get().signInWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        FunctionsService.get().createFamilyAsync();
        FunctionsService.get().getCreateFamilySubject().blockingFirst();
        return this;
    }


    public SuT doDeleteFamily() {
        AuthService.get().signInWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        String userId = AuthService.get().getCurrentUser().getUid();
        FirestoreService.get().deleteFamilyAsync(userId).blockingAwait();
        return this;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SuT doSendInvite() {
        AuthService.get().signInWithEmailAndPassword(mUserEmail, mUserPassword).blockingAwait();
        FunctionsService.get().sendInviteAsync(mUserEmail);
        FunctionsService.get().getSendInviteSubject().blockingFirst();
        return this;
    }
}
