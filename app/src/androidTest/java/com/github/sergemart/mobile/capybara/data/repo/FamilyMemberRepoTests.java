package com.github.sergemart.mobile.capybara.data.repo;

import com.github.sergemart.mobile.capybara.atf.SuT;
import com.github.sergemart.mobile.capybara.atf.TestTools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
public class FamilyMemberRepoTests {

    private static String sUserEmail;


    // --------------------------- Set up / tear down

    @BeforeClass
    public static void setUpClass() {
        sUserEmail = TestTools.get().getRandomEmail();
        SuT.get()
            .givenUserEmail(sUserEmail)
            .doCreateUser()
            .doCreateFamily()
            .doSignIn()
        ;
    }


    @Before
    public void setUpTest() {
    }


    @After
    public void tearDownTest() {
    }


    @AfterClass
    public static void tearDownClass() {
        SuT.get()
            .givenUserEmail(sUserEmail)
            .doDeleteFamily()
            .doDeleteUser()
            .doSignOut()
        ;
    }


    // --------------------------- Tests

    @Test
    public void readFamilyMemberIdsAsync_Returns_Ids() {
        // When
        List<String> result = FamilyMemberRepo.get().readFamilyMemberIdsAsync().blockingGet();
        // Then
        assertThat(result.size(), is(1));
    }


}