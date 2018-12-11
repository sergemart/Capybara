package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class FamilyMemberRepoTest {

    // --------------------------- Set up / tear down

    @BeforeClass
    public static void setUpClass() {
        AuthService.get().signInWithEmailAndPassword(Constants.TEST_USER_1_EMAIL, Constants.TEST_USER_PASSWORD).blockingAwait();
    }


    @AfterClass
    public static void tearDownClass() {
        AuthService.get().signOut();
    }


    // --------------------------- Tests

    @Test
    public void readFamilyMemberIdsAsync_Returns_Ids() {
        List<String> result = FamilyMemberRepo.get().readFamilyMemberIdsAsync().blockingGet();
        assertThat(result.size(), is(3));
    }


}