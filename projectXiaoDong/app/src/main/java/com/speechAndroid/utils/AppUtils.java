package com.speechAndroid.utils;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;


public class AppUtils {
public static String returnPackageName(String appName){
    if(appName.equals("QQ音乐"))
    {
        return "com.tencent.qqmusic";
    }
    if(appName.equals("美团"))
    {
        return "com.sankuai.meituan.takeoutnew";
    }
    if(appName.equals("知乎"))
    {
        return "com.zhihu.android";
    }
    if(appName.equals("淘宝"))
    {
        return "com.taobao.taobao";
    }if(appName.equals("百度地图"))
    {
        return "com.baidu.BaiduMap";
    }if(appName.equals("高德地图"))
    {
        return "com.autonavi.minimap";
    }if(appName.equals("微信"))
    {
        return "com.tencent.mm";
    }if(appName.equals("QQ"))
    {
        return "com.tencent.mobileqq";
    }if (appName.equals("钉钉"))
    {
        return "com.alibaba.android.rimet";
    }if(appName.equals("网易云音乐"))
    {
        return "com.netease.cloudmusic";
    }if(appName.equals("腾讯视频"))
    {
        return "com.tencent.qqlive";
    }if(appName.equals("智慧东大"))
    {
        return "com.sunyt.testdemo";
    }
    return null;
}
    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    public static int chineseToNumber(String chineseNumber){
        int result = 0;
        int temp = 1;//存放一个单位的数字如：十万
        int count = 0;//判断是否有chArr
        char[] cnArr = new char[]{'一','二','三','四','五','六','七','八','九'};
        char[] chArr = new char[]{'十','百','千','万','亿'};
        for (int i = 0; i < chineseNumber.length(); i++) {
            boolean b = true;//判断是否是chArr
            char c = chineseNumber.charAt(i);
            for (int j = 0; j < cnArr.length; j++) {//非单位，即数字
                if (c == cnArr[j]) {
                    if(0 != count){//添加下一个单位之前，先把上一个单位值添加到结果中
                        result += temp;
                        temp = 1;
                        count = 0;
                    }
                    // 下标+1，就是对应的值
                    temp = j + 1;
                    b = false;
                    break;
                }
            }
            if(b){//单位{'十','百','千','万','亿'}
                for (int j = 0; j < chArr.length; j++) {
                    if (c == chArr[j]) {
                        switch (j) {
                            case 0:
                                temp *= 10;
                                break;
                            case 1:
                                temp *= 100;
                                break;
                            case 2:
                                temp *= 1000;
                                break;
                            case 3:
                                temp *= 10000;
                                break;
                            case 4:
                                temp *= 100000000;
                                break;
                            default:
                                break;
                        }
                        count++;
                    }
                }
            }
            if (i == chineseNumber.length() - 1) {//遍历到最后一个字符
                result += temp;
            }
        }
        return result;
    }
    public static String answer(String question) {//名字随意
        Log.e("线程", Thread.currentThread().getName());
        String answer = "";
        try {
            String info = URLEncoder.encode(question, "utf-8");//处理字符串
            String getURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + info;//网址拼接
            URL getUrl = new URL(getURL);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            if(connection!=null)
            {
                Log.e("HttpURLConnection", String.valueOf(connection));
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuffer last = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null)
                {
                    last.append(line);
                }
                reader.close();
                connection.disconnect();//获取结束，得到返回的json
                JSONObject object = new JSONObject(last.toString());
                answer = object.getString("content");
                Log.e("Answer", String.valueOf(answer));
            }
            return answer;//返回结果
            } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return answer;
    }
}



