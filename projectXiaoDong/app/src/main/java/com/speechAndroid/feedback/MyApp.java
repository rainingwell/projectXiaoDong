package com.speechAndroid.feedback;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class MyApp extends Application {

    private static Context context;
    public static Context getAppContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(MyApp.this, SpeechConstant.APPID + "=7c3b1c97"); //初始化
        MyApp.context = getApplicationContext();
    }
}