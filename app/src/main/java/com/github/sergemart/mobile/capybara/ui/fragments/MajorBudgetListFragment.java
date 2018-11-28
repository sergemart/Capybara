package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.model.ContactData;
import com.github.sergemart.mobile.capybara.model.FamilyMember;
import com.github.sergemart.mobile.capybara.viewmodel.MajorSharedViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MajorBudgetListFragment
    extends AbstractFragment
{

    private RecyclerView mMinorsRecyclerView;

    private final List<ContactData> mMinors = Collections.synchronizedList(new ArrayList<>());
    private MinorsAdapter mMinorsAdapter;
    private MajorSharedViewModel mMajorSharedViewModel;
    private ActionMode mActionMode;
    private boolean mIsInActionMode;                                                                // to remember the Action Mode state between the lives


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMinorsAdapter = new MinorsAdapter();
        mMajorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(pActivity)).get(MajorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.inflateFragment(R.layout.fragment_major_budget_list, inflater, container);
        mMinorsRecyclerView = fragmentView.findViewById(R.id.recyclerView_minors);

        // Set up the RecyclerView
        mMinorsRecyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(pActivity)));
        mMinorsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMinorsRecyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(pActivity), DividerItemDecoration.VERTICAL));
        mMinorsRecyclerView.setAdapter(mMinorsAdapter);

        // Recall the previous life
        if (savedInstanceState != null && savedInstanceState.getBoolean(Constants.KEY_IS_IN_ACTION_MODE, false)){ // recall the Action Mode
            mIsInActionMode = true;
            mActionMode = (Objects.requireNonNull(pActivity)).startSupportActionMode(new ActionModeCallback());
        }

        this.setViewListeners();

        super.showWaitingState();
        this.getMinors();

        return fragmentView;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Constants.KEY_IS_IN_ACTION_MODE, mIsInActionMode);                      // remember Action Mode
    }


    @Override
    public void onDestroy() {
        if (mActionMode != null) mActionMode.finish();
        super.onDestroy();
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {
    }


    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {
    }


    // --------------------------- Use cases

    /**
     * Get minors to display in the list
     */
    private void getMinors() {
    }


    // ============================== Inner classes: Recycler View Adapter

    class MinorsAdapter extends RecyclerView.Adapter<MinorHolder> {

        /**
         * @return View holder instance
         */
        @NonNull
        @Override
        public MinorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = pLayoutInflater.inflate(R.layout.list_item_minor_budget, parent, false);
            return new MinorHolder(itemView);
        }


        /**
         * Should be lightweight to smooth scrolling. All possible preparations to be made outside this method
         */
        @Override
        public void onBindViewHolder(@NonNull MinorHolder holder, int position) {
            ContactData item = mMinors.get(position);
        }


        /**
         * @return A collection size
         */
        @Override
        public int getItemCount() {
            return mMinors.size();
        }


        /**
         * Clean-up actions
         */
        @Override
        public void onViewRecycled(@NonNull MinorHolder holder) {
            super.onViewRecycled(holder);
        }

    }


    // ============================== Inner classes: View holder

    class MinorHolder
        extends RecyclerView.ViewHolder
    {

        View mmItemView;
        ImageView mmThumbnailImageView;
        TextView mmMinorNameTextView;


        MinorHolder(View itemView) {
            super(itemView);
            mmItemView = itemView;
            mmThumbnailImageView = itemView.findViewById(R.id.imageView_thumbnail);
            mmMinorNameTextView = itemView.findViewById(R.id.textView_minor_name);
        }


        void bind(FamilyMember item, boolean isActive) {
            mmItemView.setActivated(isActive);
            mmMinorNameTextView.setText(item.getName());
            mmThumbnailImageView.setImageBitmap(item.getPhoto());
        }

    }


    // ============================== Inner classes: Context menu controller (Action Mode callback)

    public class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_major_budget_list, menu);
            mIsInActionMode = true;
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return true;
        }


        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menuItem_action:
                    // do the action
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mIsInActionMode = false;
        }
    }
}
