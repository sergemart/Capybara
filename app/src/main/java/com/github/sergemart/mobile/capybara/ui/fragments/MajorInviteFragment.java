package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.ContactsRepo;
import com.github.sergemart.mobile.capybara.viewmodel.MajorSharedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
                    switch (result) {
                        case SUCCESS:
                            mContacts = (List)result.getData();
                            mContactsAdapter.notifyDataSetChanged();
                            break;
                        case FAILURE:
                            break;
                        default:
                    }
                })
            );
        } else {
            super.requestPermissions(Constants.CONTACTS_PERMISSIONS, Constants.REQUEST_CODE_READ_CONTACTS_PERMISSIONS);
        }
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

//        Disposable disposable = ContactsRepo.get().getContactPhotoObservable(item.id)
//            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
//                switch (result) {
//                    case SUCCESS:
//                        if (result.getData() != null) {
//                            if (BuildConfig.DEBUG) Log.d(TAG, "Contact photo for " + item.email + " received");
//                            holder.mmThumbnailImageView.setImageBitmap((Bitmap) result.getData());
//                        }
//                        break;
//                    case FAILURE:
//                        break;
//                    default:
//                }
//            })
//        ;

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
