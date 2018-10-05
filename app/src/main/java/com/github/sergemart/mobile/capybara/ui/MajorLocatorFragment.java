package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.R;
import com.google.android.gms.maps.SupportMapFragment;


public class MajorLocatorFragment extends SupportMapFragment {

    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.initMemberVariables();
        this.setAttributes();
        this.setListeners();

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    // --------------------------- Widget controls

    /**
     * Init member variables
     */
    private void initMemberVariables() {
    }


    /**
     * Set attributes
     */
    private void setAttributes() {
    }


    /**
     * Set listeners to widgets and containers
     */
    private void setListeners() {
    }


}
