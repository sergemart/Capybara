package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.Repository;


public class MinorTaskListFragment extends Fragment {

    private static final String TAG = MinorTaskListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;


    // --------------------------- Override fragment event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        this.setHasOptionsMenu(true);                                                               // tell the fragment manager that this.onCreateOptionsMenu() should be called
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_minor_task_list, container, false);

        this.initMemberVariables(fragmentView);
        this.setAttributes();
        this.setListeners();

        return fragmentView;
    }


    /**
     * Inflate the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);                                                  // useless here, but is a good rule
        inflater.inflate(R.menu.fragment_main, menu);
    }


    /**
     * Process menu item selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId() ) {
            case R.id.menuItem_action:
                return true;                                                                        // no further processing needed

            default:
                return super.onOptionsItemSelected(item);                                           // unknown item id - call parent
        }
    }


    // --------------------------- Widget controls

    /**
     * Init member variables
     */
    private void initMemberVariables(View fragmentView) {
        mRecyclerView = fragmentView.findViewById(R.id.recyclerView_list);
    }


    /**
     * Set attributes
     */
    private void setAttributes() {
        super.setRetainInstance(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager( this.getActivity() ));              // set up the RecyclerView
        this.setupAdapter();
    }


    /**
     * Set listeners to widgets and containers
     */
    private void setListeners() {
    }


    // --------------------------- Subroutines

    /**
     * Set a new Adapter instance loaded with a displayed collection into the member Recycler View
     */
    private void setupAdapter() {
        mRecyclerView.setAdapter(new MainItemAdapter( this.getActivity(), Repository.get().getItems() ));
    }


}
