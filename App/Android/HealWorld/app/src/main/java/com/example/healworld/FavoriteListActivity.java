package com.example.healworld;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.healworld.common.adapter.FavoriteListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {
    private String[] mDataset;
    private FavoriteListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private int spanCount = 2;
    private JSONArray mJSONArray;
    private String FAVORITEFILEDIR;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);
        intent = getIntent();
        FAVORITEFILEDIR = intent.getStringExtra("favorite_file_dir");
        setHomeButtonAndTitle();
        initRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                FavoriteListActivity.this.finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void initDataset() {
        mDataset = new String[40];
        for (int i = 0; i < 40; i++) {
            mDataset[i] = "This is element #" + i;
        }
    }

    // set the activity home button and title
    private void setHomeButtonAndTitle(){
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.menu_item_favorite);
    }

    private void initRecyclerView(){
        initDataset();
        mJSONArray = readFavoriteData(FAVORITEFILEDIR);
        mRecyclerView = findViewById(R.id.rv_favorite_list);
        setRecyclerViewAdapter();
        setRecyclerViewLayoutManager();
    }

    private void setRecyclerViewAdapter(){
        mAdapter = new FavoriteListAdapter(mJSONArray);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setRecyclerViewLayoutManager() {
        int[] mFirstVisibleItems = new int[spanCount];
        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            mFirstVisibleItems = ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPositions(mFirstVisibleItems);
        }
        mLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(mFirstVisibleItems[0]);
    }

    private JSONArray readFavoriteData(String path){
        try{
            FileInputStream fis = openFileInput(path);
            byte temp[] = new byte[1024];
            List<byte[]> byteList = new ArrayList<byte[]>();
            int len = 0;
            while ((len = fis.read(temp)) > 0){
                byte tmpByte[] = new byte[len];
                System.arraycopy(temp, 0, tmpByte, 0, len);
                byteList.add(tmpByte);
            }
            fis.close();
            byte allByte[];
            int totalLen = 0;
            for(int i = 0; i < byteList.size(); i++){
                totalLen += byteList.get(i).length;
            }
            allByte = new byte[totalLen];
            int alreadyCopy = 0;
            for(int i = 0; i < byteList.size(); i++){
                System.arraycopy(byteList.get(i), 0, allByte, alreadyCopy, byteList.get(i).length);
                alreadyCopy += byteList.get(i).length;
            }
            String jsonString = new String(allByte, 0, totalLen);
            jsonString = "[" + jsonString.substring(0, jsonString.length()-1) + "]";
            Log.i("json_string", jsonString);
            JSONArray jsonArray = new JSONArray(jsonString);
            return jsonArray;
        }
        catch (Exception e){
            return null;
        }
    }
}

