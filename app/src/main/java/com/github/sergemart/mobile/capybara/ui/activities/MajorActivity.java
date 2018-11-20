package com.github.sergemart.mobile.capybara.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.sergemart.mobile.capybara.R;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;


public class MajorActivity
    extends AbstractActivity
{

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private NavController mNavController;
    private NavOptions mDefaultNavOptions;


    // --------------------------- Override activity event handlers

    /**
     * Instance creation actions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_major);

        mDrawerLayout = findViewById(R.id.layout_fragment_container_major);
        mNavigationView = findViewById(R.id.navigationView_major);

        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment_major);
        mDefaultNavOptions = new NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
        ;

        this.setInstanceListeners();
    }


    // --------------------------- Activity lifecycle subroutines

    /**
     * Set instance listeners
     */
    private void setInstanceListeners() {

        // Set a listener to the Drawer events
        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(true);                                                              // set item as selected to persist highlight
            mDrawerLayout.closeDrawers();                                                           // close drawer when item is tapped

            NavOptions navOptions;
            switch ( menuItem.getItemId() ) {
                case R.id.nav_action_invite:
                    navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.majorInviteFragment, true)                         // clear the entire task
                        .build()
                    ;
                    mNavController.navigate(R.id.majorInviteFragment, null, navOptions);
                    return true;                                                                    // no further processing needed
                case R.id.nav_action_locate:
                    navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.majorLocatorFragment, true)                        // clear the entire task
                        .build()
                    ;
                    mNavController.navigate(R.id.majorLocatorFragment, null, navOptions);
                    return true;                                                                    // no further processing needed
                default:
                    return true;                                                                    // no further processing needed
            }
        });
    }


    // --------------------------- Static encapsulation-leveraging methods

    /**
     * Create properly configured intent intended to invoke this activity
     */
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MajorActivity.class);
    }

}
