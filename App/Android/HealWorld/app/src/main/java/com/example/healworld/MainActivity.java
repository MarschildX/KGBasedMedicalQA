package com.example.healworld;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.StringRes;

import com.example.healworld.common.CommonMessageActivity;
import com.example.healworld.model.ChatMessage;
import com.example.healworld.model.User;
import com.example.healworld.utils.MessageUtil;
import com.example.healworld.utils.AppUtil;
import com.example.healworld.utils.HttpConnection;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends CommonMessageActivity
        implements MessagesListAdapter.OnMessageLongClickListener<ChatMessage>,
        MessageInput.InputListener, MessageInput.AttachmentsListener, View.OnClickListener{

    private MessagesList messagesList;
    private User user;
    private User doctor;
    private MessageInput input;
    private MessageUtil messageUtil;
    private HttpConnection httpConnection;
    private MessagesListAdapter<ChatMessage> messagesListAdapter;
    private Handler myHandler;
    private Dialog bottomDialog;
    final private String SORRYCANTANSWER = "抱歉，暂时还无法解答你的问题，如需获取更多信息请咨询相关医生。";
    private ChatMessage currChatMessage;

    // define a inner self-Handler class
    class MyHandler extends Handler {
        // override the handleMessage function to define UI action
        @Override
        public void handleMessage(Message msg) {
            // do different UI actions according to msg.what value
            switch (msg.what) {
                case 1:
                    if(msg.obj != null){
                        HashMap<String, String> result = handleAnswerJSON(msg.obj.toString());
                        messagesListAdapter.addToStart(new ChatMessage(messageUtil.generateId(), doctor, result.get("answer"), result.get("question"), result.get("context")), true);
                    }
                    else{
                        messagesListAdapter.addToStart(new ChatMessage(messageUtil.generateId(), doctor, SORRYCANTANSWER), true);
                    }
                    break;
                case 2:
                    if(msg.arg1 == 1){
                        AppUtil.showToast(getApplicationContext(), R.string.feedback_successfully, true);
                    }
                    else{
                        AppUtil.showToast(getApplicationContext(), R.string.feedback_failed, true);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list_main);

        initMessageList();
        initInputBar();
        initBottomDialog();
        initInfo();
    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage){
        currChatMessage = chatMessage;
        bottomDialog.show();
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(new ChatMessage(messageUtil.generateId(), user, input.toString()), true);
        new Thread() {
            @Override
            public void run() {
                String macAddress = AppUtil.getMacAddress(getApplicationContext());
                Log.i("mac_address", macAddress == null ? "null" : macAddress);
                String answer = httpConnection.qa_communicate((macAddress == null ? "null" : macAddress), input.toString());
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = answer;
                myHandler.sendMessage(msg);
            }
        }.start();
        return true; // clear text that in textview automatically if return true;
    }

    @Override
    public void onAddAttachments() {

    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_copy:
                String copiedText = currChatMessage.getText();
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("message text", copiedText);
                clipboard.setPrimaryClip(clip);
                bottomDialog.dismiss();
                AppUtil.showToast(this, R.string.action_copy, false);
                break;
            case R.id.btn_feedback_useful:
                feedback(currChatMessage, R.string.feedback_useful);
                bottomDialog.dismiss();
                break;
            case R.id.btn_feedback_not_exact_correct:
                feedback(currChatMessage, R.string.feedback_not_exact_correct);
                bottomDialog.dismiss();
                break;
            case R.id.btn_feedback_wait_to_add:
                feedback(currChatMessage, R.string.feedback_wait_to_add);
                bottomDialog.dismiss();
                break;
            case R.id.btn_cancel:
                bottomDialog.dismiss();
                break;
        }
    }

    // handle the JSON answer
    private HashMap<String, String> handleAnswerJSON(String jsonAnswer){
        HashMap<String, String> result = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonAnswer);

            // handle the answer part
            JSONArray answers = jsonObject.getJSONArray("answers");
            String finalAnswer = "";
            for (int i = 0; i < answers.length(); i++) {
                finalAnswer = finalAnswer + answers.getString(i) + '\n';
            }
            result.put("answer", finalAnswer.trim());

            // handle the origin question part
            String question = jsonObject.getString("question");
            result.put("question", question);

            // handle the question context part
            String questionContext = jsonObject.getString("context");
            result.put("context", questionContext);
        }
        catch (Exception e){
            result.put("answer", SORRYCANTANSWER);
            result.put("question", "null");
            result.put("context", "null");
        }
        return result;
    }

    private void initMessageList(){
        messagesList = findViewById(R.id.messagesList);
        super.messagesAdapter = new MessagesListAdapter<>("0", null);
        super.messagesAdapter.setOnMessageLongClickListener(this);
        messagesList.setAdapter(super.messagesAdapter);
        messagesListAdapter = super.messagesAdapter;
    }

    private void initInputBar(){
        input = findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);
    }

    private void initBottomDialog(){
        bottomDialog = new Dialog(this, R.style.message_bottom_dialog);
        bottomDialog.setContentView(View.inflate(this, R.layout.message_bottom_dialog, null));
        // dialog window setup
        Window window = bottomDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.y = 20;
        final Button btnCopy = (Button) window.findViewById(R.id.btn_copy);
        final Button btnUseful = (Button) window.findViewById(R.id.btn_feedback_useful);
        final Button btnNotExactCorrect = (Button) window.findViewById(R.id.btn_feedback_not_exact_correct);
        final Button btnWaitToAdd = (Button) window.findViewById(R.id.btn_feedback_wait_to_add);
        final Button btnCancel = (Button) window.findViewById(R.id.btn_cancel);

        btnCopy.setOnClickListener(this);
        btnUseful.setOnClickListener(this);
        btnNotExactCorrect.setOnClickListener(this);
        btnWaitToAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private void initInfo(){
        user = new User("0", "FangXu", "Shaun", true);
        doctor = new User("1", "RobotDoctor", "Robot", true);
        messageUtil = new MessageUtil();
        httpConnection = new HttpConnection();
        myHandler = new MyHandler();
    }

    private void feedback(ChatMessage chatMessage, @StringRes int fb){
        String feedbackText = this.getString(fb);
        if(chatMessage.getUser() == user){
            new Thread() {
                @Override
                public void run() {
                    String macAddress = AppUtil.getMacAddress(getApplicationContext());
                    boolean isSuccess = httpConnection.feedback_communicate((macAddress == null ? "null" : macAddress),
                            chatMessage.getText(), feedbackText, "null");
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.arg1 = isSuccess ? 1 : 0;
                    myHandler.sendMessage(msg);
                }
            }.start();
        }
        else if(chatMessage.getUser() == doctor){
            new Thread() {
                @Override
                public void run() {
                    String macAddress = AppUtil.getMacAddress(getApplicationContext());
                    boolean isSuccess = httpConnection.feedback_communicate((macAddress == null ? "null" : macAddress),
                            chatMessage.getCorrespondingQuestion(), feedbackText, chatMessage.getQuestionContext());
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.arg1 = isSuccess ? 1 : 0;
                    myHandler.sendMessage(msg);
                }
            }.start();
        }
    }
}