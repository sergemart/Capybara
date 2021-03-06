package com.github.sergemart.mobile.capybara.data.datastore;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.atf.SuT;
import com.github.sergemart.mobile.capybara.atf.TestTools;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import io.reactivex.observers.TestObserver;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class FirestoreServiceTests {

    private static String sUserEmail;

    private FirebaseUser mCurrentUser;


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
        mCurrentUser = AuthService.get().getCurrentUser();
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

    // !!! The tests deals with local database replica first. The read tests may fail if the replica is not in sync
    // In that case Firebase does not give a clear error message
    // Clear test app data to recreate the local replica

    @Test
    public void updateCurrentUserAsync_Has_Proper_Rx_Flow() {
        TestObserver testObserver = new TestObserver();
        // Given
        Map<String, Object> userData = new ConcurrentHashMap<>();
        userData.put("testKey1", "testValue1");
        userData.put("testKey2", "testValue2");
        // Then
        testObserver.assertNotSubscribed();
        // When
        FirestoreService.get().updateCurrentUserLocalReplicaAsync(userData).subscribe(testObserver);
        // Then
        testObserver.assertSubscribed();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }


    @Test
    public void updateCurrentUserAsync_Updates_User_Document() {
        // Given
        Map<String, Object> userData = new ConcurrentHashMap<>();
        userData.put("testKey1", "testValue1");
        userData.put("testKey2", "testValue2");
        // When
        FirestoreService.get().updateCurrentUserLocalReplicaAsync(userData).blockingAwait();
        DocumentSnapshot result = FirestoreService.get().readCurrentUserAsync().blockingGet();
        // Then
        assertThat(result.get("testKey1"), is("testValue1"));
        assertThat(result.get("testKey2"), is("testValue2"));
    }


    @Test
    public void readCurrentUserAsync_Returns_Proper_User_Document() {
        // When
        DocumentSnapshot result = FirestoreService.get().readCurrentUserAsync().blockingGet();
        // Then
        assertThat(result.getId(), is(mCurrentUser.getUid()));
    }


    @Test
    public void readFamilyAsync_Returns_Proper_Family() {
        // When
        QuerySnapshot result = FirestoreService.get().readFamilyAsync(mCurrentUser.getUid()).blockingGet();
        // Then
        assertThat(result.size(), is(1));                                                     // should be only one family
        assertThat(result.getDocuments().get(0).get(Constants.FIRESTORE_FIELD_CREATOR), is(mCurrentUser.getUid()));
    }


    @Test
    public void readSystemDatabaseAsync_Returns_Proper_Database_Document() {
        // When
        DocumentSnapshot result = FirestoreService.get().readSystemDatabaseAsync().blockingGet();
        // Then
        assertThat((Long)result.get(Constants.FIRESTORE_FIELD_VERSION), greaterThan(0L));
    }


}