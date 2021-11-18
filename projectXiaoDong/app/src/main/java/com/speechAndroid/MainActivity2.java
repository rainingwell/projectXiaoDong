package com.speechAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.speechAndroid.utils.FunctionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    public static final String TAG = "MainActivity2";
    private Switch mySwitch1;
    private Switch mySwitch2;
    private Switch mySwitch3;
    private Switch mySwitch4;
    private Switch mySwitch5;
    private Switch mySwitch6;
    private Switch mySwitch7;
    private Switch mySwitch8;
    private Button button ;
    private Button startFloatWindow;
    private EventManager asr;   //识别
    private static int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyWindowManager.bool = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (!Settings.canDrawOverlays(MainActivity2.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 1);
        }
        initPermission();
        initView();
    }
    //初始化
    public void initView() {
        mySwitch1 = findViewById(R.id.switch1);
        mySwitch2 = findViewById(R.id.switch2);
        mySwitch3 = findViewById(R.id.switch3);
        mySwitch4 = findViewById(R.id.switch4);
        mySwitch5 = findViewById(R.id.switch5);
        mySwitch6 = findViewById(R.id.switch6);
        mySwitch7 = findViewById(R.id.switch7);
        mySwitch8 = findViewById(R.id.switch8);
        List<Switch> list = new ArrayList<>();
        list.add(mySwitch1);
        list.add(mySwitch2);
        list.add(mySwitch3);
        list.add(mySwitch4);
        list.add(mySwitch5);
        list.add(mySwitch6);
        list.add(mySwitch7);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    {
                        if(mySwitch1.isChecked()==true) {
                            SpeechService.SYSTEM_FUNCTION = "true";
                        }else{
                            SpeechService.SYSTEM_FUNCTION = "false";
                        }
                        if(mySwitch2.isChecked()==true) {
                            SpeechService.QQFunction = "true";
                        }else{
                            SpeechService.QQFunction = "false";
                        }
                        if(mySwitch3.isChecked()==true) {
                            SpeechService.WeChatFunction = "true";
                        }else{
                            SpeechService.WeChatFunction = "false";
                        }
                        if(mySwitch4.isChecked()==true) {
                            SpeechService.QQvideoFunction = "true";
                        }else{
                            SpeechService.QQvideoFunction = "false";
                        }
                        if(mySwitch5.isChecked()==true) {
                            SpeechService.QQmusicFunction = "true";
                        }else{
                            SpeechService.QQmusicFunction = "false";
                        }
                        if(mySwitch6.isChecked()==true) {
                            SpeechService.GaoDeMap = "true";
                        }else{
                            SpeechService.GaoDeMap = "false";
                        }
                        if(mySwitch7.isChecked()==true) {
                            SpeechService.NeuFunction = "true";
                        }else{
                            SpeechService.NeuFunction = "false";
                        }
                        if(mySwitch8.isChecked()==true) {
                            SpeechService.Advertisement = "true";
                        }else{
                            SpeechService.Advertisement = "false";
                        }
                        Intent speechIntent = new Intent(MainActivity2.this, SpeechService.class);
                        startService(speechIntent);
                        Intent intent = new Intent(MainActivity2.this, FloatWindowService.class);
                        startService(intent);
                        finish();
                    }
                }
            }
        });
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    //动态申请权限
    public void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.USE_FULL_SCREEN_INTENT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SYSTEM_ALERT_WINDOW
        };
        ArrayList<String> toApplyList = new ArrayList<String>();
        //判断需要的权限哪些还没有授予
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 1);
        }
        if (flag==0) {
            try {
                this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                flag=1;
            } catch (Exception e) {
                this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                e.printStackTrace();
            }
        }
    }
}