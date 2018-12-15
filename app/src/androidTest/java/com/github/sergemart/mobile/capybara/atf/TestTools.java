package com.github.sergemart.mobile.capybara.atf;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.PreferenceStore;
import com.github.sergemart.mobile.capybara.workflow.activity.InitialMajorActivity;
import com.github.sergemart.mobile.capybara.workflow.activity.InitialMinorActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.RandomStringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


// Singleton
public class TestTools {

    private static TestTools sInstance = new TestTools();


    // Private constructor
    private TestTools() {
    }


    // Factory method
    public static TestTools get() {
        if(sInstance == null) sInstance = new TestTools();
        return sInstance;
    }


    // --------------------------- Tools

    public String getRandomEmail() {
        return
            Constants.TEST_USER_EMAIL_PREFIX
            + RandomStringUtils.randomAlphabetic(Constants.TEST_USER_EMAIL_LENGTH)
            + Constants.TEST_USER_EMAIL_DOMAIN
        ;
    }

}
