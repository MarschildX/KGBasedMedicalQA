package com.example.healworld.common;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

    /**
     * set some touch event here, such as exiting the keyboard if click the region out of EditText and keyboard
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Compare the coordinate of EditText and the region that user click,
     * because if user click EditText, do not exit the keyboard.
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                top = l[1],
                bottom = top + v.getHeight(),
                right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // if click EditText, ignore it.
                return false;
            } else {
                return true;
            }
        }
        // if the current focus is not EditText, then return false.
        return false;
    }

    /**
     * utilize InputMethodManager to hide the keyboard.
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
