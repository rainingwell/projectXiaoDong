package com.speechAndroid;


import android.app.SearchManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.inputstream.InFileStream;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.speechAndroid.HttpSpeech.QingYunkeRobotServiceImpl;
import com.speechAndroid.feedback.KqwSpeechCompound;
import com.speechAndroid.utils.AppUtils;
import com.speechAndroid.utils.FlashlightUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SpeechService extends Service{
    //表示服务状态的标志
    public  static String START_NOT_WAKE = "START_NOT_WAKE";   //在主界面开启了服务，但是却没有唤醒111
    public static String WAKE_ED = "WAKE_ED"; //已经唤醒
    public static String SPEECH_ING = "SPEECH_ING"; //唤醒结束，开始语音识别
    public static String SPEECH_ED = "SPEECH_ED"; //识别完毕，处于动作执行阶段
    public static String SPEECH_NO_WORDS = "SPEECH_NO_WORDS"; //识别完毕，但是没说话，应该直接重新开启一个唤醒
    public static String SPEECH_NOT_ACTION = "SPEECH_NOT_ACTION"; //识别完毕，但是相应的动作没有执行成功
    public static String SPEECH_WAKE_START = "SPEECH_WAKE_START"; //动作执行完毕，需要一个新的唤醒开启
    public static String SYSTEM_END = "SYSTEM_END"; //系统服务关闭
    //开启的功能
    public static String SYSTEM_FUNCTION = "false";
    public static String QQFunction = "false";
    public static String WeChatFunction = "false";
    public static String GaoDeMap = "false";
    public static String NeuFunction = "false";
    public static String QQmusicFunction = "false";
    public static String QQvideoFunction = "false";
    public static String Advertisement = "false";
    //联系人列表
    private List<String> nameList = new ArrayList<>();
    //联系人map
    private Map<String, String> ContactsList = new HashMap<>();
    //当前服务状态
    public static String state = START_NOT_WAKE;

    private EventManager wakeup;  //唤醒
    private EventManager asr;   //识别

    private static KqwSpeechCompound kqwSpeechCompound;//语音合成

    /*系统相机*/
    private Camera camera;
    public  SpeechService() {};
    private FlashlightUtils flashlightUtils;

    public KqwSpeechCompound getKqwSpeechCompound(){
        return kqwSpeechCompound;
    }

    //语音聊天
    private static QingYunkeRobotServiceImpl qingyunkeRobotService ;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        Log.e("服务状态","onCreate");
        kqwSpeechCompound=new KqwSpeechCompound(this);
        new Thread(r).start();
        readContact();
    }
    /*
    服务被调用的反应
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("服务状态","onStartCommand");
        if(state.equals(START_NOT_WAKE))
        {
            initWeakUp(); //初始化一个唤醒服务
            start();  //打开唤醒服务
        }
        return START_STICKY;
    }
    /*
    服务销毁
     */
    @Override
    public void onDestroy() {
        Log.e("服务状态","onDestroy");
        if (state.equals(SYSTEM_END)) {
            kqwSpeechCompound.speaking("服务当前是关闭状态");
           // Toast.makeText(SpeechService.this, "该功能已关闭", Toast.LENGTH_SHORT).show();
        } else if (state.equals(START_NOT_WAKE)) { //如果这个时候处于打开但是却没有唤醒的状态
            super.onDestroy(); //直接销毁
        } else if(state.equals(WAKE_ED)){ //唤醒结束
            stop();
        } else if(state.equals(SPEECH_ING)){ //唤醒结束，开始语音识别
            stopSpeech();
        } else if(state.equals(SPEECH_ED)) //识别结束，正在执行动作
        {
            stopSpeech();
        } else if(state.equals(SPEECH_NO_WORDS)) //识别结束，没说话
        {
            stopSpeech();
        } else if(state.equals(SPEECH_NOT_ACTION)) //识别结束，动作执行未成功
        {
            stopSpeech();
        } else
        {
            stopSpeech();
        }
        state = SYSTEM_END;  //标志现在服务已经关闭了
        super.onDestroy();
    }

    /**
     * 测试参数填在这里
     * 基于SDK唤醒词集成第2.1 设置唤醒的输入参数
     */
    private void start() {
        Log.e("服务状态","start");
        // 基于SDK唤醒词集成第2.1 设置唤醒的输入参数
        Map<String, Object> params = new TreeMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        params.put(SpeechConstant.APP_ID,"23407735");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        InFileStream.setContext(this);
        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        wakeup.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }

    // 基于SDK唤醒词集成第4.1 发送停止事件
    private void stop() {
        wakeup.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0);
    }

    //  基于SDK唤醒词集成1.2 自定义输出事件类 EventListener  回调方法
    // 基于SDK唤醒3.1 开始回调事件


    //打开实时语音识别
    public void setSpeechAuto() {
        //先实例化一个识别模型
        initSpeech();
        //然后把识别模型打开
        asr.send(SpeechConstant.ASR_START, null, null, 0, 0);
        state = SPEECH_ING; //开始语音识别
    }

    public void initSpeech() {
        asr = EventManagerFactory.create(this, "asr");
        asr.registerListener(new EventListener() {
            @Override
            public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {
                if (s1 == null || s1.isEmpty()) {
                    return;
                }
                if (s1.contains("\"final_result\"")) {
                    Log.e("测试","I don't know");
                    String results_recognition = null;
                    //最终识别结果
                    //对结果做一个解析
                    try {
                        JSONObject jsonObject = new JSONObject(s1);
                        results_recognition = jsonObject.getString("results_recognition");
                        Log.e("测试", results_recognition);
                        state = SPEECH_ED; //识别结束，开始执行下列的动作之一
                        if(SYSTEM_FUNCTION.equals("true")) {
                            toCallNumber(results_recognition);
                            readContacts(results_recognition);
                            readAppName(results_recognition);
                            doWebSearch(results_recognition);
                            volumeAdjust(results_recognition);
                            flashOpen(results_recognition);
                            flashClose(results_recognition);
                            flashOpenSOS(results_recognition);
                            flashCloseSOS(results_recognition);
                        }
                        SoftwareOperate(results_recognition);
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    } catch(NullPointerException e)
                    {
                        System.out.println("抛出一个NullPointerException错误");
                        e.printStackTrace();
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    if(!state.equals(SPEECH_WAKE_START)) //说的话没有录入命令
                    {

                        int pos1 = results_recognition.indexOf("\"")+1;
                        int pos2 = results_recognition.lastIndexOf("\"");
                        String requestion = results_recognition.substring(pos1,pos2);
                        state = SPEECH_WAKE_START;
                    }
                }
                if (s1.contains("no speech")) {
                    state = SPEECH_NO_WORDS;
                    Log.e("测试", "嘛都没有");
                }
            }
        });
    }

    public void initWeakUp() {
        // 基于SDK唤醒词集成1.1 初始化EventManager
        wakeup = EventManagerFactory.create(this, "wp");
        Log.e("服务状态","initWeakUp");
        // 基于SDK唤醒词集成1.3 注册输出事件
        wakeup.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.e("响应函数","onEvent");
                //做一个解析
                JSONObject jsonObject = null;
                if (params != null) {
                    try {
                        jsonObject = new JSONObject(params);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        String result = jsonObject.getString("word");
                        if (result.equals("小东小东")) {
                            stop(); //唤醒成功，把唤醒关掉
                            wakeup = null;
                            state = WAKE_ED;
                            /*这两个音频会互相影响，用不同线程隔开*/
                            kqwSpeechCompound.speaking("有什么事吗?");
                            Thread.sleep(1000);
//                          Toast.makeText(SpeechService.this, "你好", Toast.LENGTH_SHORT).show();
                            setSpeechAuto();
                        }
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }); //  EventListener 中 onEvent方法

    }

    //打开asr
    public void startSpeech() {
        asr.send(SpeechConstant.ASR_START, null, null, 0, 0);
    }

    //关闭asr
    public void stopSpeech() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
    }

    //循环扫描,线程记录
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);//每100ms进行一次扫描
                    //动作执行完毕，需要一个新的唤醒开启 或者 //识别完毕，但是没说话，应该直接重新开启一个唤醒
                    if (state.equals(SPEECH_WAKE_START)||state.equals(SPEECH_NO_WORDS)) {
                        Log.e("重开","重开");
                        //把asr销毁了
                        asr = null;
                        //stopSpeech();
                        //开启一个新的wakeup
                        initWeakUp();
                        start();
                        state = START_NOT_WAKE; //已经成功打开了唤醒
                        Log.e("live","或者的进程");
                    }
                    //执行活动失败了
                    if(SoftwareService.condition.equals("STOP"))
                    {
                        kqwSpeechCompound.speaking("指令未成功执行，请重新呼唤小东");
                        //把asr销毁了
                        stopSpeech();
                        asr = null;
                        //开启一个新的wakeup
                        initWeakUp();
                        start();
                        state = START_NOT_WAKE; //已经成功打开了唤醒
                    }
                    Log.e("stateCondition", String.valueOf(state));
//                    if(String.valueOf(state) ！)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //电话操作
    //13840429366
    //读取联系人权限拨打电话
    private void readContacts(String name) {
        if (name.contains("电话") && !name.contains("微信") && !name.contains("QQ")) {
            //模糊匹配,找到匹配度最高的联系人
            if(nameList==null)
            {
                Log.e("联系人的数量", "null");
            }
            Log.e("联系人的数量", String.valueOf(nameList.size()));
            String[] list = new String[nameList.size()];
            int i = 0;
            for (String n : nameList) {
                list[i++] = n;
            }
            //找到对应的联系人
            int pos1 = name.indexOf("给") + 1;
            int pos2 = name.indexOf("打电话");
            String contactname = name.substring(pos1, pos2);
            if (AppUtils.isNumeric(contactname) == false) {
                try {
                    Search d = new Search(list);
                    Search.Score correctNameScore = d.search(contactname, 10).get(0);

                    String correctName = correctNameScore.word.word;
                    Log.e("打电话的人",correctName);
                    Toast.makeText(this, "给"+correctName+"打电话", Toast.LENGTH_SHORT).show();
                    System.out.println(correctName + ContactsList.get(correctName));
                    String num = ContactsList.get(correctName);
                    setCall(num);
                    state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
                } catch (PinyinException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*读取通讯录信息*/
    public void readContact() {
        ContactsList = new HashMap<String, String>(); //储存联系人的名字与电话号码,然后用作模糊匹配
        nameList = new ArrayList<String>(); //扫描整个通讯录,储存联系人名字用于模糊匹配
        //遍历联系人
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String displayName = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    number = number.replace(" ", "");
                    //把遍历到的信息都加到列表和Map里面
                    nameList.add(displayName);
                    ContactsList.put(displayName,number);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    //读取用户说的电话号码,打电话
    public void toCallNumber(String name) throws InterruptedException {
        if (name.contains("给") && name.contains("打电话") && !name.contains("微信") && !name.contains("QQ")) {
            int pos1 = name.indexOf("给") + 1;
            int pos2 = name.indexOf("打电话");
            String number = name.substring(pos1, pos2);
            if (AppUtils.isNumeric(number)) {
                setCall(number);
                state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
                Thread.sleep(1000);
                Log.e("大撒大撒","asdasdasd");
            }

        }
    }

    public void setCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + number);
        intent.setData(data);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    //拉起其他APP
    //读取用户输入的应用名
    public void readAppName(String name) {
        String packageName = null;
        if (name.contains("打开QQ音乐")) {
            packageName = AppUtils.returnPackageName("QQ音乐");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开百度地图")) {
            packageName = AppUtils.returnPackageName("百度地图");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开高德地图")) {
            packageName = AppUtils.returnPackageName("高德地图");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开QQ")) {
            packageName = AppUtils.returnPackageName("QQ");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开微信")) {
            packageName = AppUtils.returnPackageName("微信");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开知乎")) {
            packageName = AppUtils.returnPackageName("知乎");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开淘宝")) {
            packageName = AppUtils.returnPackageName("淘宝");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开钉钉")) {
            packageName = AppUtils.returnPackageName("钉钉");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开网易云音乐")) {
            packageName = AppUtils.returnPackageName("网易云音乐");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开腾讯视频")) {
            packageName = AppUtils.returnPackageName("腾讯视频");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (name.contains("打开智慧东大")) {
            packageName = AppUtils.returnPackageName("智慧东大");
            startApp(packageName);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        }
    }

    public void startApp(String packageName) {

        PackageManager packageManager = getPackageManager();
        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
        launchIntentForPackage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (launchIntentForPackage != null)
            startActivity(launchIntentForPackage);
        else{
            kqwSpeechCompound.speaking("手机未安装该应用");
        }
            //Toast.makeText(this, "手机未安装该应用", Toast.LENGTH_SHORT).show();
    }

    public void doWebSearch(String Content) {
        //从浏览器中搜索
        //浏览器搜索内容
        if (Content.contains("搜索")) {
            String content = Content.replace("[\"搜索", "");
            content = content.replace("。\"]", "");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Content == null) {
                Toast.makeText(this, "您没有输入搜索内容", Toast.LENGTH_SHORT).show();
            }
            intent.putExtra(SearchManager.QUERY, content);
            Toast.makeText(this, "搜索 "+content, Toast.LENGTH_SHORT).show();
            startActivity(intent);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        }
    }

    //打开闪光灯
    public void flashOpen(String message) {
        if(message.contains("开")&&(message.contains("手电筒")||message.contains("闪光灯"))) {
            if (flashlightUtils == null) {
                flashlightUtils = new FlashlightUtils();
            }
            flashlightUtils.lightsOn(getApplicationContext());
        }
    }
    //关闭闪光灯
    public void flashClose(String message) {
        if(message.contains("关")&&(message.contains("手电筒")||message.contains("闪光灯"))) {
            if (flashlightUtils == null) {
                flashlightUtils = new FlashlightUtils();
            }
            flashlightUtils.lightOff();
        }
    }
    /*开启闪光灯SOS模式*/
    public void flashOpenSOS(String message) {
        if(message.contains("开")&&(message.contains("SOS")||message.contains("求救"))) {
            if (flashlightUtils == null) {
                flashlightUtils = new FlashlightUtils();
            }
            flashlightUtils.sos(getApplicationContext(),1);
        }
    }
    /*关闭闪光灯SOS模式*/
    public void flashCloseSOS(String message) {
        if(message.contains("关")&&(message.contains("SOS")||message.contains("求救"))) {
            if (flashlightUtils == null) {
                flashlightUtils = new FlashlightUtils();
            }
            flashlightUtils.offSos();
        }
    }
    //针对各个软件的操作函数
    public void SoftwareOperate(String message) throws InterruptedException {
        Intent SoftService = new Intent(SpeechService.this, SoftwareService.class);
        startService(SoftService);
        SoftwareService.softName = "QQ音乐";
        SoftwareService.haveCommand = "yes";
        //QQ音乐操作
        if ("true".equals(SpeechService.QQmusicFunction)&&(message.contains("qq音乐") || message.contains("QQ音乐")) && !message.contains("打开")) {
            //音乐类软件界面的切换可能系统检测不到变化，因此每次认为制造一次页面的改变，模拟home效果（速度极快，肉眼感觉不到）
            if (message.contains("听歌")) {
                Intent intent2 = new Intent(Intent.ACTION_MAIN);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent2);
                //由于某些原因,如果是 暂停之后直接 听歌,由于两个页面完全之一,因此SoftwareService接收不到,因此必须先模拟一下home效果,人造一个界面切换效果
                SoftwareService.qqMusicComand = SoftwareService.qqMusicPlay;
                SoftwareService.qqMusicState = SoftwareService.qqMusicState_0;
                readAppName("打开QQ音乐");
            } else if (message.contains("暂停")) {
                Intent intent2 = new Intent(Intent.ACTION_MAIN);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent2);
                SoftwareService.qqMusicComand = SoftwareService.qqMusicStop;
                SoftwareService.qqMusicState = SoftwareService.qqMusicState_0;
                readAppName("打开QQ音乐");
            } else if (message.contains("上一首") || message.contains("上一曲")) {
                SoftwareService.qqMusicComand = SoftwareService.qqMusicPresong;
                SoftwareService.qqMusicState = SoftwareService.qqMusicState_0;
                readAppName("打开QQ音乐");
            } else if (message.contains("下一首") || message.contains("下一曲")) {
                SoftwareService.qqMusicComand = SoftwareService.qqMusicNextsong;
                SoftwareService.qqMusicState = SoftwareService.qqMusicState_0;
                readAppName("打开QQ音乐");
            } else if (message.contains("播放")) {
                int index1 = message.indexOf("播放") + 2;
                int index2 = message.indexOf('。');
                String songName = message.substring(index1, index2);
                if (songName.equals("")) {
                    return;
                }
                Toast.makeText(SpeechService.this, "QQ音乐播放 "+songName, Toast.LENGTH_SHORT).show();
                SoftwareService.qqMusicComand = SoftwareService.qqMusicSearch;
                SoftwareService.qqMusicState = SoftwareService.qqMusicState_0;
                SoftwareService.qqMusicSearchSong = songName;
                readAppName("打开QQ音乐");
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if ("true".equals(SpeechService.WeChatFunction)&&message.contains("微信") && !message.contains("打开")) {
            SoftwareService.softName = "微信";
            if (message.contains("电话") || message.contains("通话") || message.contains("语音") || message.contains("视频")) {
                if ((message.contains("语音") || message.contains("电话") || message.contains("通话")) && !message.contains("视频")) {
                    SoftwareService.choice = "voice";
                } else if (message.contains("视频")) {
                    SoftwareService.choice = "video";
                }
            }
            int index1 = 0;//name
            if (message.contains("给")) {
                index1 = message.indexOf("给") + 1;
            }
            int index2 = 0;//name
            if (message.contains("用") && (message.indexOf("用") > message.indexOf("给"))) {
                index2 = message.indexOf("用");
            } else if (message.contains("微") && (message.indexOf("微") > message.indexOf("给"))) {
                index2 = message.indexOf("微");
            } else if (message.contains("打")) {
                index2 = message.indexOf("打");
            } else if (message.contains("进")) {
                index2 = message.indexOf("进");
            } else if (message.contains("语")) {
                index2 = message.indexOf("语");
            } else if (message.contains("视")) {
                index2 = message.indexOf("视");
            } else if (message.contains("通")) {
                index2 = message.indexOf("通");
            }
            if (index1 < index2 && index1 != 0 && index2 != 0) {
                String Name = message.substring(index1, index2);
                Name = Name.replaceAll("，", "");//去掉逗号
                SoftwareService.name = ChineseToEnglish2.getFirstSpell(Name); //名字转换首字母缩写
                SoftwareService.counter = -1;
                Toast.makeText(SpeechService.this, "正在打开微信", Toast.LENGTH_SHORT).show();
                Toast.makeText(SpeechService.this, "使用微信进行语音通话", Toast.LENGTH_SHORT).show();
                /*打开服务
                Intent wechartService = new Intent(SpeechService.this,SoftwareService.class);
                startService(wechartService);*/
                PackageManager packageManager = getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);//无论app是什么状态  重启app 使打开app时是主界面
                SoftwareService.softName = "微信";//重启之后打开service里的功能
            } else {
                Toast.makeText(SpeechService.this, "识别失败", Toast.LENGTH_SHORT).show();
            }
            readAppName("打开微信");
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if ("true".equals(SpeechService.QQFunction)&&message.contains("QQ") && !message.contains("打开") && !message.contains("音乐")) {
            SoftwareService.softName = "QQ";
            if (message.contains("电话") || message.contains("通话") || message.contains("语音") || message.contains("视频")) {
                if ((message.contains("语音") || message.contains("电话") || message.contains("通话")) && !message.contains("视频")) {
                    SoftwareService.choice = "voice";
                } else if (message.contains("视频")) {
                    SoftwareService.choice = "video";
                }
            }
            int index1 = 0;//name
            if (message.contains("给")) {
                index1 = message.indexOf("给") + 1;
            }
            int index2 = 0;//name
            if (message.contains("用") && (message.indexOf("用") > message.indexOf("给"))) {
                index2 = message.indexOf("用");
            } else if (message.contains("Q") && (message.indexOf("Q") > message.indexOf("给"))) {
                index2 = message.indexOf("Q");
            } else if (message.contains("打")) {
                index2 = message.indexOf("打");
            } else if (message.contains("进")) {
                index2 = message.indexOf("进");
            } else if (message.contains("语")) {
                index2 = message.indexOf("语");
            } else if (message.contains("视")) {
                index2 = message.indexOf("视");
            } else if (message.contains("通")) {
                index2 = message.indexOf("通");
            }
            if (index1 < index2 && index1 != 0 && index2 != 0) {
                String Name = message.substring(index1, index2);
                Name = Name.replaceAll("，", "");//去掉逗号
                SoftwareService.name = ChineseToEnglish2.getFirstSpell(Name); //名字转换首字母缩写
                SoftwareService.counter = -1;
                Toast.makeText(SpeechService.this, "使用QQ进行语音通话", Toast.LENGTH_SHORT).show();
                PackageManager packageManager = getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mobileqq");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);//无论app是什么状态  重启app 使打开app时是主界面
                SoftwareService.softName = "QQ";//重启之后打开service里的功能
            } else {
                Toast.makeText(SpeechService.this, "识别失败", Toast.LENGTH_SHORT).show();
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if ("true".equals(SpeechService.QQvideoFunction)&&message.contains("腾讯视频") && !message.contains("打开")) {
            SoftwareService.softName = "腾讯视频";
            if (message.contains("播放")) {
                String[] videos = message.split("播放");
                String videoName = videos[1].replace(",", "").replace("。", "").replace("\"", "").replace("]", "");
                if (videoName != null) {
                    SoftwareService.video = videoName;
                    SoftwareService.counter = -1;
                    PackageManager packageManager = getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.qqlive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if ("true".equals(SpeechService.GaoDeMap)&&message.contains("高德地图") && message.contains("使用")) {
            SoftwareService.softName = "高德地图";
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage("com.autonavi.minimap");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if ("true".equals(SpeechService.NeuFunction)&&message.contains("智慧东大") && message.contains("使用")) {
            SoftwareService.softName = "智慧东大";
            SoftwareService.counter = -1;
            if (message.contains("健康填报")) {
                SoftwareService.barItem = "应用";
                SoftwareService.function = "健康填报";
            } else if (message.contains("查看")) {
                if (message.contains("新闻")) {
                    SoftwareService.barItem = "新闻";
                    SoftwareService.function = "";
                } else if (message.contains("校园码")) {
                    SoftwareService.barItem = "校园码";
                    SoftwareService.function = "";
                } else if (message.contains("应用")) {
                    SoftwareService.barItem = "应用";
                    SoftwareService.function = "";
                } else if (message.contains("个人信息")) {
                    SoftwareService.barItem = "我的";
                    SoftwareService.function = "";
                } else if (message.contains("课程表")) {
                    SoftwareService.barItem = "应用";
                    SoftwareService.function = "课程表";
                } else if (message.contains("校历")) {
                    SoftwareService.barItem = "应用";
                    SoftwareService.function = "校历";
                }
            }
                PackageManager packageManager = getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.sunyt.testdemo");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
            } else if (message.contains("退出")) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
            }
            stopService(SoftService);
    }

    /**
     * 音量调节
     *
     * @param message
     * @throws InterruptedException
     */
    public void volumeAdjust(String message) throws InterruptedException {
        //获取系统的Audio管理者
        AudioManager mAudioManage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //最大音量
        int maxVolume = mAudioManage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        int currentVolume = mAudioManage.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (message.contains("音量") && message.contains("最大")) {
            mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (message.contains("静音")) {
            mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (message.contains("音量") && (message.contains("高") || message.contains("大"))) {
            //自动+5%
            int setVolume = (int)(currentVolume + maxVolume*0.05);
            if (setVolume <= maxVolume) {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, setVolume, 0);
            } else {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, setVolume, 0);
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (message.contains("音量") && (message.contains("低") || message.contains("小"))) {
            //自动-5%
            int setVolume = (int)(currentVolume - maxVolume*0.05);
            if (setVolume >= 0) {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, setVolume, 0);
            } else {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (message.contains("音量加") || message.contains("音量增加")) {
            /**
             * 设置加音量
             */
            int index0 = message.indexOf("加") + 1;
            int index1 = message.indexOf("%");
            if(index0==-1||index1==-1)
            {
                return ;
            }
            String addVolume = message.substring(index0, index1);
            Log.e("音量测试", addVolume);
            int add = 0;
            if(AppUtils.isNumeric(addVolume))
            {
                add = Integer.parseInt(addVolume);
            }else
            {
                add = AppUtils.chineseToNumber(addVolume);
            }
            Log.e("音量测试1", String.valueOf(add));
            int setVolume = (int)(currentVolume + currentVolume*0.01*add);
            if(setVolume == 0)
            {
                setVolume = (int)(maxVolume*0.01*add);
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, setVolume, 0);
            }else if (setVolume <= maxVolume) {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, setVolume, 0);
            } else {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        } else if (message.contains("音量减") || message.contains("音量减小") || message.contains("音量降低")) {
            /**
             * 设置减音量
             */
            int index0 = 0;
            if(message.contains("音量减"))
            {
                index0 = message.indexOf("减")+1;
            }else if(message.contains("音量减小"))
            {
                index0 = message.indexOf("小")+1;
            }else if(message.contains("音量降低"))
            {
                index0 = message.indexOf("低")+1;
            }
            int index1 = message.indexOf("%");
            String subVolume = message.substring(index0, index1);
            Log.e("音量测试", subVolume);
            int sub = 0 ;
            if(AppUtils.isNumeric(subVolume))
            {
                sub = Integer.parseInt(subVolume);
            }else
            {
                sub = AppUtils.chineseToNumber(subVolume);
            }
            AppUtils.chineseToNumber(subVolume);
            Log.e("音量测试1", String.valueOf(sub));
            int setVolume = (int)(currentVolume - currentVolume*0.01*sub);
            if (setVolume >= 0) {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, setVolume, 0);
            } else {
                mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            }
            state = SPEECH_WAKE_START; //动作执行完毕了，状态改变
        }
    }
}
