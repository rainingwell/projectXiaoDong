package com.speechAndroid.utils;

import android.util.Log;
import android.widget.Switch;

import com.speechAndroid.SpeechService;

import java.util.List;

public class FunctionUtils {
    /*遍历所有的功能配置*/
    public static void restConfig(List<Switch> switches) {
        for (Switch aSwitch : switches) {
            Log.e("aSwitch", aSwitch.getText().toString());
            /*if(aSwitch.getText().equals("系统功能")) {
                Log.e("进入了系统功能","dsadsad");
                if(aSwitch.isChecked()==true) {
                    SpeechService.SYSTEM_FUNCTION = "true";
                } else {
                    SpeechService.SYSTEM_FUNCTION = "false";
                }
            } else if (aSwitch.getText().equals("QQ")) {
                if(aSwitch.isChecked()==true) {

                } else {

                }
            } else if (aSwitch.getText().equals("微信")) {
                if(aSwitch.isChecked()==true) {

                } else {

                }
            } else if (aSwitch.getText().equals("腾讯视频")) {
                if(aSwitch.isChecked()==true) {

                } else {

                }
            } else if (aSwitch.getText().equals("高德地图")) {
                if(aSwitch.isChecked()==true) {

                } else {

                }
            } else if (aSwitch.getText().equals("QQ音乐")) {
                if(aSwitch.isChecked()==true) {

                } else {

                }
            } else if (aSwitch.getText().equals("智慧东大")) {
                if(aSwitch.isChecked()==true) {
                    ;
                } else {

                }
            } else if (aSwitch.getText().equals("广告跳过")) {
                if(aSwitch.isChecked()==true) {

                } else {

                }
            }*/
        }
    }
}
