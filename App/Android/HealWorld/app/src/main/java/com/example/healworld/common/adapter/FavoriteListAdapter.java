package com.example.healworld.common.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healworld.FavoriteDetailActivity;
import com.example.healworld.R;
import com.example.healworld.utils.AppUtil;

import org.json.JSONArray;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {
    private int favoriteItemCount;
    private JSONArray jsonArray;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView titleView;
        private final TextView datetimeView;
        private final CardView cardView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = (TextView) view.findViewById(R.id.tv_cardview_favorite_list);
            titleView = (TextView) view.findViewById(R.id.tv_title_cardview_favorite_list);
            datetimeView = (TextView) view.findViewById(R.id.tv_datetime_cardview_favorite_list);
            cardView = (CardView) view.findViewById(R.id.cardview_favorite_list);
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getTitleView(){
            return titleView;
        }

        public TextView getDatetimeView(){
            return datetimeView;
        }

        public CardView getCardView(){
            return cardView;
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
        try {
            String question = jsonArray.getJSONObject(favoriteItemCount-position-1).getString("question");
            String text = jsonArray.getJSONObject(favoriteItemCount-position-1).getString("text");
            String datetime = jsonArray.getJSONObject(favoriteItemCount-position-1).getString("datetime");
            viewHolder.getTitleView().setText(question);
            viewHolder.getTextView().setText(text);
            viewHolder.getDatetimeView().setText(datetime);
            viewHolder.getCardView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(view.getContext(), FavoriteDetailActivity.class);
                        intent.putExtra("question", question);
                        intent.putExtra("text", text);
                        intent.putExtra("datetime", datetime);
                        view.getContext().startActivity(intent);
                    }
                    catch(Exception e){
                        AppUtil.showToast(view.getContext(), "无响应", false);
                    }
                }
            });
        }
        catch(Exception e){
            viewHolder.getTitleView().setText("解析不出标题");
            viewHolder.getTextView().setText("解析不出内容");
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return favoriteItemCount;
    }
}

