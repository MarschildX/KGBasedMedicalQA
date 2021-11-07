package com.example.healworld;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class QAApplication extends Application {

    @Override
    public void onCreate() {
        SpeechUtility.createUtility(QAApplication.this, "appid=bca77093");
        super.onCreate();
    }
}
