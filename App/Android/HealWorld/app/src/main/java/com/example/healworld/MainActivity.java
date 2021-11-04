package com.example.healworld;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.Message;

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
        MessageInput.InputListener,
        MessageInput.AttachmentsListener{

    private MessagesList messagesList;
    private User user;
    private User doctor;
    private MessageInput input;
    private MessageUtil messageUtil;
    private HttpConnection httpConnection;
    private MessagesListAdapter<ChatMessage> messagesListAdapter;
    private Handler myHandler;
    final private String SORRYCANTANSWER = "抱歉，暂时还无法解答你的问题，如需获取更多信息请咨询相关医生。";

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
                    // do nothing
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
        initInfo();
    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage){
        String copiedText = chatMessage.getText();
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message text", copiedText);
        clipboard.setPrimaryClip(clip);
        AppUtil.showToast(this, R.string.action_copy, true);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(new ChatMessage(messageUtil.generateId(), user, input.toString()), true);
        new Thread() {
            @Override
            public void run() {
                String macAddress = AppUtil.getMacAddress(getApplicationContext());
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

    private void initInfo(){
        user = new User("0", "FangXu", "Shaun", true);
        doctor = new User("1", "RobotDoctor", "Robot", true);
        messageUtil = new MessageUtil();
        httpConnection = new HttpConnection();
        myHandler = new MyHandler();
    }

}