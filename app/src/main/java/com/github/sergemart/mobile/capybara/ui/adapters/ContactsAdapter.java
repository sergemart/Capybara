package com.github.sergemart.mobile.capybara.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.ContactsRepo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ContactsRepo.Contact> mItems;


    public ContactsAdapter(Context context, List<ContactsRepo.Contact> items) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mItems = items;

    }


    // --------------------------- Overrides

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
        ContactsRepo.Contact item = mItems.get(position);

        holder.mmContactNameTextView.setText(item.name);
        holder.mmContactEmailTextView.setText(item.email);
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


    // --------------------------- Inner classes: View holder

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