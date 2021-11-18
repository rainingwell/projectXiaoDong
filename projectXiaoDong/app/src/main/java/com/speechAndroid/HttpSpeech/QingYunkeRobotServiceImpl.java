package com.speechAndroid.HttpSpeech;

import android.util.Log;

import com.google.gson.Gson;
import java.net.URLEncoder;

public class QingYunkeRobotServiceImpl implements RobotService {
    //青云客平台的API地址
    private String uri = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=%s";
    private String api;
    Gson gson = new Gson();//用于解析json串

    /**
     * @param question：使用者输入的对话
     * @return Response对象，保存来自机器人的对话
     */
    @Override
    public Response answer(String question) {
        try {
            //需要对输入的对话进行编码，青云客平台使用utf-8编码
            api = String.format(uri, URLEncoder.encode(question, "UTF-8"));
            Log.e("format",api);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("线程",Thread.currentThread().getName());
        String ans = HttpUtils.request(api);//获得json串，用字符串储存
        if (!ans.contains("##出现错误，"))
            return gson.fromJson(ans, Response.class);//使用Gson对json串解析，存入Response中
        else {
            Response response = new Response();
            response.setContent(ans);
            return response;
        }
    }
}
