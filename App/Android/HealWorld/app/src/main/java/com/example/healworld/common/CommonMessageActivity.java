package com.example.healworld.common;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.healworld.R;
import com.example.healworld.utils.AppUtil;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.example.healworld.model.ChatMessage;

public class CommonMessageActivity extends AppCompatActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener{
    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<ChatMessage> messagesAdapter;

    protected Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.feedback_menu, menu);
        return true;
    }

    // 右上角的两个按钮
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback_useful:
//                messagesAdapter.deleteSelectedMessages();
                AppUtil.showToast(this, R.string.feedback_successfully, false);
                break;
            case R.id.action_copy:
//                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtil.showToast(this, R.string.copied_message, true);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

    /*
    * load the history data
    * */
    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.i("TAG", "onLoadMore: " + page + " " + totalItemsCount);
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages();
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
//        menu.findItem(R.id.action_delete).setVisible(count > 0);
//        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    protected void loadMessages() {
        //imitation of internet connection
//        new Handler().postDelayed(() -> {
//            ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
//            lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
//            messagesAdapter.addToEnd(messages, false);
//        }, 1000);
    }

    private MessagesListAdapter.Formatter<ChatMessage> getMessageStringFormatter() {
        return chatMessage -> {
            String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(chatMessage.getCreatedAt());

            String text = chatMessage.getText();
            if (text == null) text = "[attachment]";

            return String.format(Locale.getDefault(), "%s: %s (%s)",
                    chatMessage.getUser().getName(), text, createdAt);
        };
    }

}
