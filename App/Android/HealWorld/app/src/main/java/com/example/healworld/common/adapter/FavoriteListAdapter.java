package com.example.healworld.common.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.healworld.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {

    private String[] localDataSet;
    private int favoriteItemCount;
    private JSONArray jsonArray;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.tv_cardview_favorite_list);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     */
    public FavoriteListAdapter(JSONArray jsonData) {
        jsonArray = jsonData;
        try {
            favoriteItemCount = jsonArray.length();
        }
        catch(Exception e){

        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_item_favorite, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // 设定textview的高度，应该基于内容长度来定，而且需要固定
        Random random = new Random();
        int randomHeight = random.nextInt((500 - 150) + 1) + 150;
        viewHolder.getTextView().setHeight(randomHeight);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.getTextView().setText(localDataSet[position]);
        try {
            viewHolder.getTextView().setText(jsonArray.getJSONObject(position).getString("text"));
        }
        catch(Exception e){
            viewHolder.getTextView().setText("解析不出数据");
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return favoriteItemCount;
    }
}

