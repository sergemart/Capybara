package com.github.sergemart.mobile.capybara.controller.fragment;

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
import com.github.sergemart.mobile.capybara.data.source.CloudService;
import com.github.sergemart.mobile.capybara.data.ContactRepo;
import com.github.sergemart.mobile.capybara.data.events.GenericEvent;
import com.github.sergemart.mobile.capybara.data.model.ContactData;
import com.github.sergemart.mobile.capybara.viewmodel.MajorSharedViewModel;

import java.util.ArrayList;
import java.util.Collections;
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

    private RecyclerView mContactsRecyclerView;

    private final List<ContactData> mContacts = Collections.synchronizedList(new ArrayList<>());
    private ContactsAdapter mContactsAdapter;
    private SelectionTracker<String> mSelectionTracker;
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

        mContactsAdapter = new ContactsAdapter();
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
            Constants.SELECTION_ID,
            mContactsRecyclerView,
            new MajorInviteFragment.ContactsKeyProvider(ItemKeyProvider.SCOPE_MAPPED),              // scope = all list data
            new MajorInviteFragment.ContactLookup(),
            StorageStrategy.createStringStorage()                                                   // key type is String (email)
        )
            .withOnItemActivatedListener( this::onItemActivated )
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

        super.showWaitingState();
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
                if ( ContactRepo.get().isPermissionGranted() ) this.getContacts();                 // 2nd try, if granted
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

        // Add listeners to selection events
        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver<String>() {

            // Hide a thumbnail overlay when select an item with the overlay
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                int position = getContactIndexByEmail(key);
                if (mContacts.get(position).getInviteSendResult() != Constants.INVITE_NONE) {
                    mContacts.get(position).setInviteSendResult(Constants.INVITE_NONE);
                }
            }


            // Show/hide a context menu depending on whether selection is made or not
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
        pInstanceDisposable.add(CloudService.get().getSendInviteSubject()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(inviteResult -> {
                int position = this.getContactIndexByEmail((String)inviteResult.getData());
                switch (inviteResult.getResult()) {
                    case SUCCESS:
                        mContacts.get(position).setInviteSendResult(Constants.INVITE_SENT);
                        break;
                    case FAILURE:
                        mContacts.get(position).setInviteSendResult(Constants.INVITE_NOT_SENT);
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

        if (ContactRepo.get().isPermissionGranted() ) {

            pInstanceDisposable.add(ContactRepo.get().getContactsObservable()
                .observeOn(Schedulers.io())                                                         // switch to background
                .doOnNext(contactsResult -> {                                                       // load contacts
                    switch (contactsResult.getResult()) {
                        case SUCCESS:
                            mContacts.clear();
                            mContacts.addAll( (List)contactsResult.getData() );
                            break;
                        case FAILURE:
                            break;
                        default:
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())                                          // UI operations follows, main thread required
                .doOnNext(contactsResult -> {
                    super.hideWaitingState();
                    mContactsAdapter.notifyDataSetChanged();
                })
                .observeOn(Schedulers.io())                                                         // switch to background
                .flatMap(contactsResult ->  Observable.fromIterable( (List)((GenericEvent)contactsResult).getData() )) // split contacts
                .flatMap(contact -> {                                                               // map every contact to bitmap observable
                    String contactEmail = ((ContactData) contact).getEmail();
                    return ContactRepo.get().getEnrichedContactObservable(contactEmail);
                })
                .observeOn(AndroidSchedulers.mainThread())                                          // UI operations follows, main thread required
                .subscribe(bitmapEvent -> {                                                         // update contact list with thumbnails
                    switch ( ((GenericEvent)bitmapEvent).getResult() ) {
                        case SUCCESS:
                            ContactData auxContactData = (ContactData) ((GenericEvent)bitmapEvent).getData();
                            int position = this.getContactIndexByEmail(auxContactData.getEmail());
                            mContacts.get(position).setPhoto(auxContactData.getPhoto());
                            mContactsAdapter.notifyItemChanged(position);
                            break;
                        case FAILURE:
                            break;
                        default:
                    }
                })
            );
            ContactRepo.get().getContactsObservable().connect();                                   // init multicasting

        } else {
            super.requestPermissions(Constants.CONTACTS_PERMISSIONS, Constants.REQUEST_CODE_READ_CONTACTS_PERMISSIONS);
        }
    }


    /**
     * Send invites to the selected contacts
     */
    private void sendInvite() {
        for (String contactEmail : mSelectionTracker.getSelection()) {
            CloudService.get().sendInviteAsync(contactEmail);
        }
        Toast.makeText(pActivity, R.string.msg_invite_sent, Toast.LENGTH_LONG).show();
    }


    // --------------------------- Subroutines

    /**
     * Get a contact list index by its email
     */
    private int getContactIndexByEmail(String contactEmail) {
        synchronized (mContacts) {
            for (int i = 0; i < mContacts.size(); i++) {
                ContactData contact = mContacts.get(i);
                if (contact.getEmail().equals(contactEmail)) return i;
            }
        }
        return -1;
    }


    /**
     * Handle item events, besides the selection ones, which are managed by a SelectionTracker (see the listeners section).
     * Method signature allows to use one as the method reference in place of OnItemActivatedListener callback
     * - Selects an item on tap, when not selected
     * - Hides the thumbnail overlay on tap, when displayed
     */
    private boolean onItemActivated(ItemDetailsLookup.ItemDetails<String> itemDetails, MotionEvent motionEvent) {
        if (itemDetails.getSelectionKey() != null) mSelectionTracker.select(itemDetails.getSelectionKey()); // select on short tap

        int position = itemDetails.getPosition();
        mContacts.get(position).setInviteSendResult(Constants.INVITE_NONE);                         // hide an overlay on short tap
        mContactsAdapter.notifyItemChanged(position);
        return true;
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
            View itemView = pLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
            return new ContactHolder(itemView);
        }


        /**
         * Should be lightweight to smooth scrolling. All possible preparations to be made outside this method
         */
        @Override
        public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
            ContactData item = mContacts.get(position);
            holder.bind(item, mmSelectionTracker.isSelected(item.getEmail()));
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
            mmSuccessImageView = itemView.findViewById(R.id.imageView_thumbnail_overlay_success);
            mmFailureImageView = itemView.findViewById(R.id.imageView_thumbnail_overlay_failure);
            mmContactNameTextView = itemView.findViewById(R.id.textView_minor_name);
            mmContactEmailTextView = itemView.findViewById(R.id.textView_contact_email);
        }


        void bind(ContactData item, boolean isActive) {
            mmItemView.setActivated(isActive);
            mmContactNameTextView.setText(item.getName());
            mmContactEmailTextView.setText(item.getEmail());
            mmThumbnailImageView.setImageBitmap(item.getPhoto());
            switch (item.getInviteSendResult()) {
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
            return new ContactDetails(position, mContacts.get(position).getEmail());
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
            return mContacts.get(position).getEmail();
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
