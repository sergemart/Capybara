package com.github.sergemart.mobile.capybara.workflow.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.AuthService;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

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
        ImageView thumbnailImageView = mNavigationView.getHeaderView(0).findViewById(R.id.imageView_thumbnail);
        TextView userNameTextView = mNavigationView.getHeaderView(0).findViewById(R.id.textView_user_name);
        TextView userEmailTextView = mNavigationView.getHeaderView(0).findViewById(R.id.textView_user_email);

        userNameTextView.setText(AuthService.get().getCurrentUsername());
        userEmailTextView.setText(AuthService.get().getCurrentUser().getEmail());
        Picasso.get()
            .load(AuthService.get().getCurrentUser().getPhotoUrl())
            .placeholder(R.mipmap.capybara_bighead)
            .into(thumbnailImageView)
        ;

        mNavController = Navigation.findNavController(this, R.id.fragment_nav_host_major);

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

            NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(                                                                        // clear the entire task TODO: Works not as expected: clears nav graph fragment also. Action-based nav could be broken!
                    Objects.requireNonNull(mNavController.getCurrentDestination()).getId(),         // docs recommend use nav graph id here. Does not work
                    true
                )
                .build()
            ;
            switch ( menuItem.getItemId() ) {
                case R.id.nav_action_invite:
                    mNavController.navigate(R.id.fragment_major_invite, null, navOptions);
                    return true;                                                                    // no further processing needed
                case R.id.nav_action_locate:
                    mNavController.navigate(R.id.fragment_common_locator, null, navOptions);
                    return true;                                                                    // no further processing needed
                case R.id.nav_action_show_budget:
                    mNavController.navigate(R.id.fragment_major_budget_list, null, navOptions);
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
