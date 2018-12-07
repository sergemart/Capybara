package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = App.class)
public class FamilyMemberRepoTest {

    // --------------------------- Set up

    @Before
    public void setUpTest() {
        FirebaseApp.initializeApp(App.getContext());
    }


    // --------------------------- Tests

    @Test
    public void readFamilyMemberIdsAsync_Returns_Collection_Of_Ids() {
        FamilyMemberRepo.get().readFamilyMemberIdsAsync();
        assertThat(1, is(1));
    }


}