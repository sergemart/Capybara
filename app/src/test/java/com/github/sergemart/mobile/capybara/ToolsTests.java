package com.github.sergemart.mobile.capybara;

import android.location.Location;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ToolsTests {

    private static String sSampleLocationJson;

    @Mock
    private Location mLocation;


    // --------------------------- Set up

    @BeforeClass
    public static void setUpSuite() throws IOException {
        // Prepare resources
        InputStream inputStream = Objects.requireNonNull(ToolsTests.class.getClassLoader()).getResourceAsStream("sample_location.json");
        sSampleLocationJson = IOUtils.toString(inputStream,"UTF-8");
        inputStream.close();                                                                        // can not use try-with-resources: InputStream does not implement Autocloseable
    }


    @Before
    public void setUpTest() {

        // mLocation
        when(mLocation.getLatitude()).thenReturn(12.23d);
        when(mLocation.getLongitude()).thenReturn(23.45d);
        when(mLocation.getTime()).thenReturn(321L);
    }


    // --------------------------- Tests

    @Test
    public void getLocationJson_Returns_Proper_Json() {
        String locationJson = Tools.get().getLocationJson(mLocation);
        assertThat(locationJson, is(sSampleLocationJson));
    }


}