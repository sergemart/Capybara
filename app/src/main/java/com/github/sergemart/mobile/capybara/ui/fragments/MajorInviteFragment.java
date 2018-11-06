package com.github.sergemart.mobile.capybara.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.ContactsRepo;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.viewmodel.MajorSharedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MajorInviteFragment
    extends AbstractFragment
{

    private RecyclerView mContactsRecyclerView;

    private List<ContactsRepo.Contact> mContacts;
    private ContactsAdapter mContactsAdapter;
    private LayoutInflater mLayoutInflater;
    private MajorSharedViewModel mMajorSharedViewModel;


    // --------------------------- Override fragment lifecycle event handlers

    /**
     * Instance creation actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContacts = new ArrayList<>();
        mContactsAdapter = new ContactsAdapter();
        mLayoutInflater = LayoutInflater.from(super.getActivity());
        mMajorSharedViewModel = ViewModelProviders.of(Objects.requireNonNull( super.getActivity() )).get(MajorSharedViewModel.class);

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

        pBackgroundImageView = fragmentView.findViewById(R.id.imageView_background);
        mContactsRecyclerView = fragmentView.findViewById(R.id.recyclerView_contacts);

        // Set up the RecyclerView
        mContactsRecyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull( super.getActivity() )));
        mContactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mContactsRecyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull( super.getActivity() ), DividerItemDecoration.VERTICAL));
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

    @SuppressWarnings("unchecked")
    private void getContacts() {

        if (ContactsRepo.get().isPermissionGranted() ) {

            pInstanceDisposable.add(ContactsRepo.get().getContactsObservable()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(contactsResult -> {
                    switch (contactsResult.getResult()) {
                        case SUCCESS:
                            mContacts.clear();
                            mContacts.addAll( (List)contactsResult.getData() );
                            mContactsAdapter.notifyDataSetChanged();
                            break;
                        case FAILURE:
                            break;
                        default:
                    }
                })
            );

            pInstanceDisposable.add(ContactsRepo.get().getContactsObservable()
                .flatMap(event ->  Observable.fromIterable( (List)event.getData() ))
                .flatMap(contact -> {
                    String contactId = ((ContactsRepo.Contact) contact).id;
                    return ContactsRepo.get().getEnrichedContactObservable(contactId);
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmapEvent -> {
                    switch ( ((GenericEvent)bitmapEvent).getResult() ) {
                        case SUCCESS:
                            ContactsRepo.Contact auxContactData = (ContactsRepo.Contact) ((GenericEvent)bitmapEvent).getData();
                            int position = this.getContactIndexById(auxContactData.id);
                            mContacts.get(position).photo = auxContactData.photo;
                            mContactsAdapter.notifyItemChanged(position);
                            break;
                        case FAILURE:
                            break;
                        default:
                    }
                })
            );

            ContactsRepo.get().getContactsObservable().connect();

        } else {
            super.requestPermissions(Constants.CONTACTS_PERMISSIONS, Constants.REQUEST_CODE_READ_CONTACTS_PERMISSIONS);
        }
    }


    // --------------------------- Subroutines

    /**
     * Get a contact list index by its ID
     */
    private int getContactIndexById(String contactId) {
        for (int i = 0; i < mContacts.size(); i++) {
            ContactsRepo.Contact contact = mContacts.get(i);
            if (contact.id.equals(contactId)) return i;
        }
        return -1;
    }


    // ============================== Inner classes: Recycler View Adapter

    class ContactsAdapter extends RecyclerView.Adapter<ContactHolder> {


        ContactsAdapter() {
        }


        // ============================== Overrides

        /**
         * @return View holder instance
         */
        @NonNull
        @Override
        public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = mLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
            return new ContactHolder(itemView);
        }


        /**
         * Should be lightweight to smooth scrolling. All possible preparations to be made outside this method
         */
        @Override
        public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
            ContactsRepo.Contact item = mContacts.get(position);
            holder.mmContactNameTextView.setText(item.name);
            holder.mmContactEmailTextView.setText(item.email);
            holder.mmThumbnailImageView.setImageBitmap(item.photo);
        }


        /**
         * @return A collection size
         */
        @Override
        public int getItemCount() {
            return mContacts.size();
        }


        /**
         * Clean-up actions
         */
        @Override
        public void onViewRecycled(@NonNull ContactHolder holder) {
            super.onViewRecycled(holder);
        }

    }


    // ============================== Inner classes: View holder

    class ContactHolder extends RecyclerView.ViewHolder {

        ImageView mmThumbnailImageView;
        TextView mmContactNameTextView;
        TextView mmContactEmailTextView;


        ContactHolder(View view) {
            super(view);
            mmThumbnailImageView = view.findViewById(R.id.imageView_thumbnail);
            mmContactNameTextView = view.findViewById(R.id.textView_contact_name);
            mmContactEmailTextView = view.findViewById(R.id.textView_contact_email);
        }

    }

}
