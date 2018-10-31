package com.github.sergemart.mobile.capybara.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.BuildConfig;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.ContactsRepo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactHolder> {

    private static final String TAG = ContactsAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ContactsRepo.Contact> mItems;


    public ContactsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mItems = new ArrayList<>();
    }


    // --------------------------- Getters/ setters

    public List<ContactsRepo.Contact> getItems() {
        return mItems;
    }


    public void setItems(List<ContactsRepo.Contact> items) {
        mItems = items;
    }


    // --------------------------- Overrides


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
        ContactsRepo.Contact item = mItems.get(position);
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
        return mItems.size();
    }


    /**
     * Clean-up actions
     */
    @Override
    public void onViewRecycled(@NonNull ContactHolder holder) {
        super.onViewRecycled(holder);
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