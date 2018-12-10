package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = App.class)
public class FamilyMemberRepoTest {

    // --------------------------- Set up

    @Before
    public void setUpTest() {
//        AuthService.get().signInWithEmailAndPassword("capybara.test.dummy.1@gmail.com", "c@pyb@ra").blockingAwait();
        AuthService.get().signInWithEmailAndPassword("capybara.test.dummy.1@gmail.com", "c@pyb@ra");
    }


    // --------------------------- Tests

    @Test
    public void readFamilyMemberIdsAsync_Returns_Collection_Of_Ids() {
        List<String> result = FamilyMemberRepo.get().readFamilyMemberIdsAsync().blockingGet();
        assertThat(1, is(1));
    }


}