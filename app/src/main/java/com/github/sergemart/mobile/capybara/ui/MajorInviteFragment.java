package com.github.sergemart.mobile.capybara.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.ContactsRepo;
import com.github.sergemart.mobile.capybara.ui.adapters.ContactsAdapter;
import com.github.sergemart.mobile.capybara.viewmodel.MajorSharedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.CompositeDisposable;


public class MajorInviteFragment extends Fragment {

    private static final String TAG = MajorInviteFragment.class.getSimpleName();

    private RecyclerView mContactsRecyclerView;

    private ContactsAdapter mContactsAdapter;
    private List<ContactsRepo.Contact> mContacts;
    private CompositeDisposable mViewDisposable;
    private CompositeDisposable mInstanceDisposable;
    private MajorSharedViewModel mMajorSharedViewModel;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate() called");
        super.setRetainInstance(true);

        mContacts = new ArrayList<>();                                                              // a stub used to init an adapter
        mContactsAdapter = new ContactsAdapter(this.getActivity(), mContacts);
        mViewDisposable = new CompositeDisposable();
        mInstanceDisposable = new CompositeDisposable();
        mMajorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(super.getActivity())).get(MajorSharedViewModel.class);

        this.setInstanceListeners();
    }


    /**
     * View-related startup actions
     * @return Inflated content view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_major_invite, container, false);
        mContactsRecyclerView = fragmentView.findViewById(R.id.recyclerView_contacts);

        // Set up the RecyclerView
        mContactsRecyclerView.setLayoutManager(new LinearLayoutManager( this.getActivity() ));
        mContactsRecyclerView.setAdapter(mContactsAdapter);

        this.setViewListeners();

        this.getContacts();

        return fragmentView;
    }


    /**
     * A callback on process the runtime permission dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_READ_CONTACTS_PERMISSIONS:
                if ( ContactsRepo.get().isPermissionGranted() ) this.getContacts();                 // 2nd try, if granted
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * View clean-up
     */
    @Override
    public void onDestroyView() {
        mViewDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-related subscriptions are disposed");
        super.onDestroyView();
    }


    /**
     * Instance clean-up
     */
    @Override
    public void onDestroy() {
        mInstanceDisposable.clear();
        if (BuildConfig.DEBUG) Log.d(TAG, "View-unrelated subscriptions are disposed");
        super.onDestroy();
    }


    // --------------------------- Fragment lifecycle subroutines

    /**
     * Set listeners to view-related events
     */
    private void setViewListeners() {
    }


    /**
     * Set listeners to view-unrelated events
     */
    private void setInstanceListeners() {
    }


    // --------------------------- Use cases

    private void getContacts() {
        if (ContactsRepo.get().isPermissionGranted() ) {
            mContacts.clear();
            mContacts.addAll(ContactsRepo.get().getContacts());
            mContactsAdapter.notifyDataSetChanged();
        } else {
            super.requestPermissions(Constants.CONTACTS_PERMISSIONS, Constants.REQUEST_CODE_READ_CONTACTS_PERMISSIONS);
        }

    }
}
