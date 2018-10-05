package com.github.sergemart.mobile.capybara.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sergemart.mobile.capybara.R;

import java.util.List;


class MainItemAdapter extends RecyclerView.Adapter<MainItemAdapter.MainItemHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Object> mItems;                                                                    // collection to display; TBC


    MainItemAdapter(Context context, List<Object> items) {                                          // TBC
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mItems = items;                                                                             // TBC

    }


    // --------------------------- Overrides

    @NonNull
    @Override
    public MainItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.list_item, parent, false);
        return new MainItemHolder(itemView);
    }


    /**
     * Should be lightweight to smooth scrolling. All possible preparations to be made outside this method
     */
    @Override
    public void onBindViewHolder(@NonNull MainItemHolder holder, int position) {
        Object item = mItems.get(position);

        holder.itemAttribute.setText( item.toString() );                                            // TBC
    }


    @Override
    public int getItemCount() {
        return mItems.size();                                                                       // TBC
    }


    // --------------------------- Inner classes: View holder

    class MainItemHolder extends RecyclerView.ViewHolder {

        TextView itemAttribute;


        MainItemHolder(View view) {
            super(view);
            itemAttribute = view.findViewById(R.id.textView_list_item_attribute);
        }
    }

}