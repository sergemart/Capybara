package com.github.sergemart.mobile.capybara.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.CloudRepo;
import com.github.sergemart.mobile.capybara.data.ContactsRepo;
import com.github.sergemart.mobile.capybara.events.GenericEvent;
import com.github.sergemart.mobile.capybara.viewmodel.MajorSharedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
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

    private static final String SELECTION_ID = "selection_id";

    private RecyclerView mContactsRecyclerView;

    private List<ContactsRepo.Contact> mContacts;
    private ContactsAdapter mContactsAdapter;
    private SelectionTracker<String> mSelectionTracker;
    private LayoutInflater mLayoutInflater;
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

        mContacts = new ArrayList<>();
        mContactsAdapter = new ContactsAdapter();
        mLayoutInflater = LayoutInflater.from(pActivity);
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
        View fragmentView = super.inflateFragment(R.layout.fragment_major_invite, inflater, container);
        mContactsRecyclerView = fragmentView.findViewById(R.id.recyclerView_contacts);

        // Set up the RecyclerView
        mContactsRecyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(pActivity)));
        mContactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mContactsRecyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(pActivity), DividerItemDecoration.VERTICAL));
        mContactsRecyclerView.setAdapter(mContactsAdapter);
        mSelectionTracker = new SelectionTracker.Builder<>(                                         // a main engine of the selection library
            SELECTION_ID,
            mContactsRecyclerView,
            new MajorInviteFragment.ContactsKeyProvider(ItemKeyProvider.SCOPE_MAPPED),              // scope = all list data
            new MajorInviteFragment.ContactLookup(),
            StorageStrategy.createStringStorage()                                                   // key type is String (email)
        )
            .build()
        ;
        mContactsAdapter.setSelectionTracker(mSelectionTracker);

        // Recall the previous life
        mSelectionTracker.onRestoreInstanceState(savedInstanceState);                               // recall the selected items
        if (savedInstanceState != null && savedInstanceState.getBoolean(Constants.KEY_IS_IN_ACTION_MODE, false)){ // recall the Action Mode
            mIsInActionMode = true;
            mActionMode = (Objects.requireNonNull(pActivity)).startSupportActionMode(new ActionModeCallback());
        }

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


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectionTracker.onSaveInstanceState(outState);                                            // remember selected items
        outState.putBoolean(Constants.KEY_IS_IN_ACTION_MODE, mIsInActionMode);                      // remember Action Mode
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

        // Add listeners to selection events
        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver<String>() {

            // Manage a context menu depending on the selection
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (mSelectionTracker.hasSelection() && mActionMode == null) {                      // when an item selected and no menu shown...
                    mActionMode = (Objects.requireNonNull(pActivity)).startSupportActionMode(new ActionModeCallback()); // ... show the menu
                } else if (!mSelectionTracker.hasSelection() && mActionMode != null) {              // when no selected items left and the menu is shown...
                    mActionMode.finish();                                                           // ... destroy the menu
                    mActionMode = null;
                }
            }
        });


        // Add a listener to SendInvite event
        // Update a list item with a transaction result
        pInstanceDisposable.add(CloudRepo.get().getSendInviteSubject()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(inviteResult -> {
                int position = this.getContactIndexByEmail((String)inviteResult.getData());
                switch (inviteResult.getResult()) {
                    case SUCCESS:
                        mContacts.get(position).inviteSendResult = Constants.INVITE_SENT;
                        break;
                    case FAILURE:
                        mContacts.get(position).inviteSendResult = Constants.INVITE_NOT_SENT;
                        break;
                    default:
                }
                mContactsAdapter.notifyItemChanged(position);
            })
        );

    }


    // --------------------------- Use cases

    /**
     * Get contacts to display in the list
     */
    @SuppressWarnings({"unchecked", "RedundantCast"})
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
                    String contactEmail = ((ContactsRepo.Contact) contact).email;
                    return ContactsRepo.get().getEnrichedContactObservable(contactEmail);
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmapEvent -> {
                    switch ( ((GenericEvent)bitmapEvent).getResult() ) {
                        case SUCCESS:
                            ContactsRepo.Contact auxContactData = (ContactsRepo.Contact) ((GenericEvent)bitmapEvent).getData();
                            int position = this.getContactIndexByEmail(auxContactData.email);
                            mContacts.get(position).photo = auxContactData.photo;
                            mContactsAdapter.notifyItemChanged(position);
                            break;
                        case FAILURE:
                            break;
                        default:
                    }
                })
            );

            ContactsRepo.get().getContactsObservable().connect();                                   // init multicasting

        } else {
            super.requestPermissions(Constants.CONTACTS_PERMISSIONS, Constants.REQUEST_CODE_READ_CONTACTS_PERMISSIONS);
        }
    }


    /**
     * Send invites to the selected contacts
     */
    private void sendInvite() {
        for (String contactEmail : mSelectionTracker.getSelection()) {
            CloudRepo.get().sendInviteAsync(contactEmail);
        }
        Toast.makeText(pActivity, R.string.msg_invite_sent, Toast.LENGTH_LONG).show();
    }


    // --------------------------- Subroutines

    /**
     * Get a contact list index by its email
     */
    private int getContactIndexByEmail(String contactEmail) {
        for (int i = 0; i < mContacts.size(); i++) {
            ContactsRepo.Contact contact = mContacts.get(i);
            if (contact.email.equals(contactEmail)) return i;
        }
        return -1;
    }


    // ============================== Inner classes: Recycler View Adapter

    class ContactsAdapter extends RecyclerView.Adapter<ContactHolder> {


        private SelectionTracker<String> mmSelectionTracker;


        void setSelectionTracker(SelectionTracker<String> selectionTracker) {
            mmSelectionTracker = selectionTracker;
        }


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
            holder.bind(item, mmSelectionTracker.isSelected(item.email));
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

    class ContactHolder
        extends RecyclerView.ViewHolder
    {

        View mmItemView;
        ImageView mmThumbnailImageView;
        ImageView mmSuccessImageView;
        ImageView mmFailureImageView;
        TextView mmContactNameTextView;
        TextView mmContactEmailTextView;


        ContactHolder(View itemView) {
            super(itemView);
            mmItemView = itemView;
            mmThumbnailImageView = itemView.findViewById(R.id.imageView_thumbnail);
            mmSuccessImageView = itemView.findViewById(R.id.imageView_success);
            mmFailureImageView = itemView.findViewById(R.id.imageView_failure);
            mmContactNameTextView = itemView.findViewById(R.id.textView_contact_name);
            mmContactEmailTextView = itemView.findViewById(R.id.textView_contact_email);
        }


        void bind(ContactsRepo.Contact item, boolean isActive) {
            mmItemView.setActivated(isActive);
            mmContactNameTextView.setText(item.name);
            mmContactEmailTextView.setText(item.email);
            mmThumbnailImageView.setImageBitmap(item.photo);
            switch (item.inviteSendResult) {
                case Constants.INVITE_SENT:
                    mmSuccessImageView.setVisibility(View.VISIBLE);
                    mmFailureImageView.setVisibility(View.GONE);
                    break;
                case Constants.INVITE_NOT_SENT:
                    mmSuccessImageView.setVisibility(View.GONE);
                    mmFailureImageView.setVisibility(View.VISIBLE);
                    break;
                default:
                    mmSuccessImageView.setVisibility(View.GONE);
                    mmFailureImageView.setVisibility(View.GONE);

            }
        }


        /**
         * A helper method for the selection library
         * @return A position and a key of the item
         */
        ContactDetails getItemDetails() {
            int position = super.getAdapterPosition();
            return new ContactDetails(position, mContacts.get(position).email);
        }

    }


    // ============================== Inner classes: Item key provider

    /**
     * A helper class for the selection library.
     * Provides a two-way link between a key and a position
     */
    public class ContactsKeyProvider
        extends ItemKeyProvider<String>
    {

        ContactsKeyProvider(int scope) {
            super(scope);
        }


        @Nullable
        @Override
        public String getKey(int position) {
            return mContacts.get(position).email;
        }


        @Override
        public int getPosition(@NonNull String key) {
            return getContactIndexByEmail(key);
        }
    }


    // ============================== Inner classes: Item details

    /**
     * A helper class for the selection library.
     * Serves as a container for the item's key and position
     */
    public class ContactDetails
        extends ItemDetailsLookup.ItemDetails<String>
    {

        private final int mmPosition;
        private final String mmSelectionKey;


        ContactDetails(int position, String selectionKey) {
            this.mmPosition = position;
            this.mmSelectionKey = selectionKey;
        }


        @Override
        public int getPosition() {
            return mmPosition;
        }


        @Nullable
        @Override
        public String getSelectionKey() {
            return mmSelectionKey;
        }
    }


    // ============================== Inner classes: Item details lookup

    /**
     * A helper class for the selection library.
     * Produces an item details by the motion event
     */
    public class ContactLookup
        extends ItemDetailsLookup<String>
    {

        @Nullable
        @Override
        public ItemDetails<String> getItemDetails(@NonNull MotionEvent motionEvent) {
            View view = mContactsRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            if (view == null) return null;

            RecyclerView.ViewHolder viewHolder = mContactsRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof ContactHolder) return ((ContactHolder) viewHolder).getItemDetails();
            else return null;
        }
    }


    // ============================== Inner classes: Context menu controller (Action Mode callback)

    public class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_major_invite, menu);
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
                case R.id.menuItem_invite_send:
                    sendInvite();
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mSelectionTracker.clearSelection();
            mIsInActionMode = false;
        }
    }
}
