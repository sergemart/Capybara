package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
public class FamilyMemberRepoTest {

    // --------------------------- Set up

    @Before
    public void setUpTest() {
        AuthService.get().signInWithEmailAndPassword(Constants.TEST_USER_1_EMAIL, Constants.TEST_USER_PASSWORD).blockingAwait();
    }


    // --------------------------- Tests

    @Test
    public void readFamilyMemberIdsAsync_Returns_Collection_Of_Ids() {
        List<String> result = FamilyMemberRepo.get().readFamilyMemberIdsAsync().blockingGet();
        assertThat(result.size(), is(3));
    }


}