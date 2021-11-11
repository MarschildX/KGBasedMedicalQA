package com.example.healworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FavoriteDetailActivity extends AppCompatActivity {
    private TextView textView;
    private TextView titleView;
    private TextView datetimeView;
    private Intent intent;
    private ScrollView scrollView;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_favorite_detail);
        setHomeButtonAndTitle();
        initTextView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                FavoriteDetailActivity.this.finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void initTextView(){
        textView = findViewById(R.id.tv_favorite_detail_text);
        titleView = findViewById(R.id.tv_favorite_detail_title);
        datetimeView = findViewById(R.id.tv_favorite_detail_datetime);
        intent = getIntent();
        String question = intent.getStringExtra("question");
        String text = intent.getStringExtra("text");
        String datetime = intent.getStringExtra("datetime");
        titleView.setText(question);
        datetimeView.setText(datetime);
        textView.setText(text);
        titleView.setTextIsSelectable(true);
        textView.setTextIsSelectable(true);
    }

    // set the activity home button and title
    private void setHomeButtonAndTitle(){
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.favorite_detail);
    }
}
