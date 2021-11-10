package com.example.healworld;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.healworld.common.activity.CommonMessageActivity;
import com.example.healworld.model.ChatMessage;
import com.example.healworld.model.User;
import com.example.healworld.utils.MessageUtil;
import com.example.healworld.utils.AppUtil;
import com.example.healworld.utils.HttpConnection;
import com.example.healworld.utils.JSONParser;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


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
    private final String SORRYCANTANSWER = "抱歉，暂时还无法解答你的问题，如需获取更多信息请咨询相关医生。";
    private ChatMessage currChatMessage;

    private final static String TAG = "MainActivity";
    private String speechLanguage; // speech recognition language
    private String myEngineType; // speech recognizer engine type
    private String resultType; // result data type
    private SpeechRecognizer mySpeechRecognizer;// speech recognizer object
    private RecognizerDialog myRecognizerDialog;// speech recognizer UI
    private HashMap<String, String> mySpeechRecogResult; // store the speech recognizing result in hashmap
    private SharedPreferences mySharedPreferences;// cache
    private EditText msgEditText; // edittext of user input
    private InitListener myInitListener; // speech recognizer initial listener
    private RecognizerDialogListener myRecognizerDialogListener; // speech recognizer UI listener

    /**
     * define a inner self-Handler class
     */
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
        initPermission();
        initASRComponent();
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

    /**
     * here is the listener of attachment button, to implement ASR module listener.
     */
    @Override
    public void onAddAttachments() {
        if(mySpeechRecognizer == null){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            showMsg("语音识别模块拉起失败");
            return;
        }
        mySpeechRecogResult.clear(); // clear the data
        myRecognizerDialog.show();
        TextView txt = (TextView)myRecognizerDialog.getWindow().getDecorView().findViewWithTag("textlink");
        txt.setText("静默一秒自动结束 或 点击麦克风结束");
        txt.setLinkTextColor(Color.WHITE);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  do nothing
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mySpeechRecognizer) {
            // release the connection when destroy
            mySpeechRecognizer.cancel();
            mySpeechRecognizer.destroy();
        }
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
            case R.id.btn_favorite:
                boolean isSuccessful = saveFavoriteToFile(currChatMessage);
                bottomDialog.dismiss();
                if(isSuccessful)
                    AppUtil.showToast(this, R.string.add_to_favorite_successfully, false);
                else
                    AppUtil.showToast(this, R.string.add_to_favorite_failed, false);
                break;
        }
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
        final Button btnFavorite = (Button) window.findViewById(R.id.btn_favorite);

        btnCopy.setOnClickListener(this);
        btnUseful.setOnClickListener(this);
        btnNotExactCorrect.setOnClickListener(this);
        btnWaitToAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnFavorite.setOnClickListener(this);
    }

    private void initInfo(){
        user = new User("0", "FangXu", "Shaun", true);
        doctor = new User("1", "RobotDoctor", "Robot", true);
        messageUtil = new MessageUtil();
        httpConnection = new HttpConnection();
        myHandler = new MyHandler();
        mySpeechRecogResult = new LinkedHashMap<String, String>();
    }

    private void initASRComponent(){
        speechLanguage = "zh_cn";
        myEngineType = SpeechConstant.TYPE_CLOUD;
        resultType = "json";
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mySpeechRecognizer = SpeechRecognizer.createRecognizer(MainActivity.this, myInitListener);
        myRecognizerDialog = new RecognizerDialog(MainActivity.this, myInitListener);
        mySharedPreferences = getSharedPreferences("ASR",
                Activity.MODE_PRIVATE);
        msgEditText = findViewById(R.id.messageInput);
        myInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(TAG, "SpeechRecognizer init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    showMsg("初始化失败，错误码：" + code);
                }
            }
        };
        myRecognizerDialogListener = new RecognizerDialogListener() {
            public void onResult(RecognizerResult results, boolean isLast) {
                parseRecogResult(results);
            }
            // handle the recall error
            public void onError(SpeechError error) {
                showMsg(error.getPlainDescription(true));
            }
        };
        setSpeechRecognizerParam();
        myRecognizerDialog.setListener(myRecognizerDialogListener);
    }

    /**
     * android 6.0 and higher can request the permissions dynamically.
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ArrayList<String> toApplyList = new ArrayList<String>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    /**
     * handle the JSON answer
     */
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

    /**
     * feedback QA quality to server
     */
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

    /**
     * set the params of ASR component
     */
    public void setSpeechRecognizerParam() {
        // clear all the parameters
        mySpeechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        // set recognizer engine
        mySpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, myEngineType);
        // set result type
        mySpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (speechLanguage.equals("zh_cn")) {
            String lag = mySharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + speechLanguage);
            mySpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // the language region
            mySpeechRecognizer.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mySpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, speechLanguage);
        }
        Log.e(TAG, "last language:" + mySpeechRecognizer.getParameter(SpeechConstant.LANGUAGE));

        // whether display the error info in dialog or not
        mySpeechRecognizer.setParameter("view_tips_plain","true");
        // voice start point: set the timeout limit, the time of user keep silent regard as timeout
        mySpeechRecognizer.setParameter(SpeechConstant.VAD_BOS, mySharedPreferences.getString("iat_vadbos_preference", "4000"));
        // voice stop point: the time user stop speaking, regard as ending of voice
        mySpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, mySharedPreferences.getString("iat_vadeos_preference", "1000"));
        // return punctuation or not, 1 is return
        mySpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, mySharedPreferences.getString("iat_punc_preference", "1"));
        // set audio storage direction and format, only support pcm and wav.
        mySpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mySpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * permission request result recall
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // dynamic recall of permission request in android 6.0 and higher, do nothing here.
    }

    /**
     * show the toast messages of speech recognizer
     */
    private void showMsg(String msg) {
        AppUtil.showToast(MainActivity.this, msg, false);
    }

    /**
     * parse the recognition result
     */
    private void parseRecogResult(RecognizerResult results) {
        String text = JSONParser.parseIatResult(results.getResultString());
        String sn = null;
        // get the "sn" segment.
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mySpeechRecogResult.put(sn, text);

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mySpeechRecogResult.keySet()) {
            resultBuffer.append(mySpeechRecogResult.get(key));
        }
        msgEditText.setText(resultBuffer.toString()); // set the speech recognition result to EditText
        msgEditText.setSelection(resultBuffer.toString().length()); // set the cursor to the end
    }

    private boolean saveFavoriteToFile(ChatMessage chatMessage){
        try{
            String text = chatMessage.getText();
            String question = chatMessage.getCorrespondingQuestion();
            JSONObject obj = new JSONObject();
            obj.put("question", question);
            obj.put("text", text);
            FileOutputStream fos = openFileOutput(FAVORITEFILEDIR, Context.MODE_APPEND);
            String data = obj.toString();
            data += ",";
            fos.write(data.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        }
        catch(Exception e){
            return false;
        }
        return true;
    }
}