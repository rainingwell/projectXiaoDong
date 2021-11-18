package com.speechAndroid;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.speechAndroid.ad.TouchHelperServiceImpl;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;
/**
 * 1.实现当前播放歌曲的播放/暂停,上一首/下一首
 *   模拟状态栏的下拉,拿到状态并模拟操作
 */
public class SoftwareService extends AccessibilityService {
    //判断软件类别
    public static SoftwareService softwareService = new SoftwareService();
    public static String softName = "";
    //qq音乐
    public static  String haveCommand = "";
    public static  String qqMusicPlay = "qqMusicPlay";  //播放音乐命令333
    public static  String qqMusicStop = "qqMusicStop";  //暂停播放命令
    public static  String qqMusicPresong = "qqMusicPresong";  //播放上一首命令
    public static  String qqMusicNextsong = "qqMusicNextsong";//播放下一首命令
    public static  String qqMusicSearch = "qqMusicSearch";  //搜索歌曲并播放命令
    public static  String qqMusicState_0 = "qqMusicUndo"; //当前的命令还没有执行
    public static  String qqMusicState_1= "qqMusicDone"; //当前的命令已经执行了,需要暂停
    public static  String qqMusicSongState_0 = "play"; //当前歌曲状态为正在播放
    public static  String qqMusicSongState_1 = "stop"; //当前歌曲状态为暂停
    public static  String qqMusicSearchSong = ""; //待搜索的歌曲名称
    public static String qqMusicComand= qqMusicState_0; //当前命令
    public static String qqMusicState = ""; //当前状态
    //其他软件
    private final String TAG = getClass().getName();
    public static String choice="";//视频or语音
    public static String name="";
    public static int counter = -1;
    public static String video = ""; //腾讯视频
    //判断活动是否正常运行
    //判断活动是否正常运行
    public static String condition = "START";
    //智慧东大
    public static String function = "";
    public static String barItem = "";

    private AccessibilityNodeInfo mRootNodeInfo = null;

    //广告
    public static TouchHelperServiceImpl serviceImpl = null;
    public final static int ACTION_REFRESH_KEYWORDS = 1;
    public final static int ACTION_REFRESH_PACKAGE = 2;
    public final static int ACTION_REFRESH_CUSTOMIZED_ACTIVITY = 3;
    public final static int ACTION_ACTIVITY_CUSTOMIZATION = 4;
    public final static int ACTION_STOP_SERVICE = 5;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        condition = "START";
        Log.e("运行证明","flag");
        if(softName.equals("QQ音乐"))
        {
            Log.e("测试qqMusic",qqMusicComand);
            Log.e("qqMusicState",qqMusicState);
            mRootNodeInfo = event.getSource();
            if(mRootNodeInfo==null){
                return ;
            }
            //窗口内容出现了变化,去识别媒体播放器
            try {
                if ((haveCommand.equals("yes")&& qqMusicState.equals(qqMusicState_0))) {
                    Thread.sleep(1000);
                    mRootNodeInfo = getRootInActiveWindow();
                    if (qqMusicComand.equals(qqMusicPlay)||qqMusicComand.equals(qqMusicStop)) {
                        AccessibilityNodeInfo album = findFirst(AbstractTF.newId("com.tencent.qqmusic:id/minibar_content_album"));
                        if (album != null) {
                            clickNodeByRect(album);
                            Thread.sleep(1000);
                        }
                        AccessibilityNodeInfo playButton = findFirst(AbstractTF.newContentDescription("播放",false));
                        if (playButton == null) {
                            condition = "STOP";
                            return ;
                        } else {
                            clickView(playButton);
                            qqMusicState = qqMusicState_1;
                        }
                    } else if (qqMusicComand.equals(qqMusicPresong)) {
                        AccessibilityNodeInfo album = findFirst(AbstractTF.newId("com.tencent.qqmusic:id/minibar_content_album"));
                        if (album != null) {
                            clickNodeByRect(album);
                            Thread.sleep(1000);
                        }
                        AccessibilityNodeInfo preSongButton = findFirst(AbstractTF.newText("上一曲",false)); //上一曲按钮
                        if (preSongButton == null) {
                            condition = "STOP";
                            return ;
                        } else {
                            clickView(preSongButton);
                            qqMusicState = qqMusicState_1;
                        }
                    } else if (qqMusicComand.equals(qqMusicNextsong)) {
                        AccessibilityNodeInfo album = findFirst(AbstractTF.newId("com.tencent.qqmusic:id/minibar_content_album"));
                        if (album != null) {
                            clickNodeByRect(album);
                            Thread.sleep(1000);
                        }
                        AccessibilityNodeInfo preSongButton = findFirst(AbstractTF.newText("下一曲",false)); //上一曲按钮
                        if (preSongButton == null) {
                            condition = "STOP";
                            return ;
                        } else {
                            clickView(preSongButton);
                            qqMusicState = qqMusicState_1;
                        }
                    } else if (qqMusicComand.equals(qqMusicSearch)) {
                        /*如果一开始就在歌曲的播放页面
                        * 就返回
                        * */
                        AccessibilityNodeInfo back = findFirst(AbstractTF.newContentDescription("返回",false));
                        if (back != null) {
                            clickNodeByRect(back);
                            Thread.sleep(1000);
                        }
                        /*
                        * 当一开始在主界面的时候，点开搜索框
                        * */
                        AccessibilityNodeInfo searchBar = findFirst(AbstractTF.newId("com.tencent.qqmusic:id/sub_edit_text"));  //检测下拉窗口的多媒体播放窗口
                        if (searchBar != null) {
                            clickNodeByRect(searchBar);
                            Thread.sleep(1000);
                        }
                        /*
                        * 进入搜索框并输入搜索的内容
                        * */
                        AccessibilityNodeInfo  searchText = findFirst(AbstractTF.newId("com.tencent.qqmusic:id/searchItem"));  //可编辑文本框
                        if (searchText != null) {
                            Bundle arguments = new Bundle();
                            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, qqMusicSearchSong);  //搜歌
                            searchText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            Thread.sleep(1000);
                        }
                        /*
                        *在搜索框已经有内容的前提下找到所有的歌曲列表并点击第一首歌曲
                        */
                        List<AccessibilityNodeInfo> songs = findAll(AbstractTF.newClassName(AbstractTF.ST_LISTVIEW));
                        if (songs.size()!=0) {
                            AccessibilityNodeInfo song = songs.get(0);
                            AccessibilityNodeInfo theFirstOption = song.getChild(0);
                            clickView(theFirstOption);
                            Thread.sleep(1000);
                        }
                        /*
                        * 已经在搜索完成了的页面，现在开始点击播放按钮
                        * */
                        AccessibilityNodeInfo singleSongButton = findFirst(AbstractTF.newText("单曲",true));
                        if (singleSongButton != null) {
                            AccessibilityNodeInfo playButton = singleSongButton.getParent().getChild(1);
                            clickNodeByRect(playButton);
                        }
                        qqMusicState = qqMusicState_1;
                    }
                    haveCommand = "no";
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(softName.equals("微信"))
        {
            if(name.equals("")||choice.equals("")){
                return;
            }
            Log.e("测试微信","开始测试");
            Timer timer = new Timer();//实例化Timer类
            AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.mm:id/fdi"));//搜索
            if (search != null && counter == -1) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.mm:id/fdi"));
                        clickView(search);
                        this.cancel();
                    }
                }, 1000);
                counter = 0;
            }
            AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mm:id/bxz"));//搜索框
            if (idInfo != null && counter == 0) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mm:id/bxz"));
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, name);
                        idInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                        this.cancel();
                    }
                }, 1000);
                counter = 1;
            }

            idInfo = findFirst(AbstractTF.newId("com.tencent.mm:id/ir3"));
            if (idInfo != null && counter == 1) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mm:id/ir3"));
                        clickView(idInfo);
                        this.cancel();
                    }
                }, 1000);
                counter = 2;
            }

            idInfo = findFirst(AbstractTF.newId("com.tencent.mm:id/au0"));//"+"号
            if (idInfo != null && counter == 2) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mm:id/au0"));
                        clickView(idInfo);
                        this.cancel();
                    }
                }, 1000);
                counter = 3;
            }

            idInfo = findFirst(AbstractTF.newText("视频通话", true));
            if (idInfo != null && counter == 3) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newText("视频通话", true));
                        clickView(idInfo);
                        this.cancel();
                    }
                }, 1500);
                counter = 4;
            }
            if(choice.equals("voice")){
                idInfo = findFirst(AbstractTF.newText("语音通话", true));
                if(idInfo != null && counter == 4){
                    timer.schedule(new TimerTask(){
                        public void run(){
                            AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newText("语音通话",true));
                            clickView(idInfo);
                            this.cancel();}},1000);
                    counter=5;
                }
            }else if(choice.equals("video")){
                idInfo = findFirst(AbstractTF.newText("视频通话", true));
                if (idInfo != null && counter == 4) {
                    timer.schedule(new TimerTask() {
                        public void run() {
                            AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newText("视频通话", true));
                            clickView(idInfo);
                            this.cancel(); }
                    }, 1000);
                    counter = 5;
                }
            }
            if(idInfo!=null){
                idInfo.recycle();
            }
        }
        if(softName.equals("QQ"))
        {
            if(name.equals("")||choice.equals("")){
                return;
            }
            Log.e("测试QQ","开始测试");

            /*qq语音与视频通话*/
            Timer timer = new Timer();//实例化Timer类
            AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/et_search_keyword"));//搜索
            if (search != null && counter == -1) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/et_search_keyword"));
                        clickView(search);
                        this.cancel();
                    }
                }, 0);
                counter = 0;
            }

            AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/et_search_keyword"));//搜索框
            if (idInfo != null && counter == 0) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/et_search_keyword"));
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, name);
                        idInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                        this.cancel();
                    }
                }, 500);
                counter = 1;
            }

            idInfo = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/image"));
            if (idInfo != null && counter == 1) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/image"));
                        clickView(idInfo);
                        this.cancel();
                    }
                }, 500);
                counter = 2;
            }

            idInfo = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/ivTitleBtnRightCall"));//"+"号
            if (idInfo != null && counter == 2) {
                timer.schedule(new TimerTask() {
                    public void run() {
                        AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newId("com.tencent.mobileqq:id/ivTitleBtnRightCall"));
                        clickView(idInfo);
                        this.cancel();
                    }
                }, 500);
                counter = 3;
            }
            if(choice.equals("voice")){
                idInfo = findFirst(AbstractTF.newText("语音通话", true));
                if(idInfo != null && counter == 3){
                    timer.schedule(new TimerTask(){
                        public void run(){
                            AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newText("语音通话",true));
                            clickView(idInfo);
                            this.cancel();}},1000);
                    counter=4;
                }
            }else if(choice.equals("video")){
                idInfo = findFirst(AbstractTF.newText("视频通话", true));
                if (idInfo != null && counter == 3) {
                    timer.schedule(new TimerTask() {
                        public void run() {
                            AccessibilityNodeInfo idInfo = findFirst(AbstractTF.newText("视频通话", true));
                            clickView(idInfo);
                            this.cancel(); }
                    }, 1000);
                    counter = 4;
                }
            }
            if(idInfo!=null){
                idInfo.recycle();
            }
        }
        if(softName.equals("腾讯视频")) {
            Log.e("测试腾讯视频","开始测试");
            Timer timer = new Timer();

            AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.qqlive:id/est"));
            if(search!=null&&counter==-1){
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.qqlive:id/est"));
                        clickView(search);
                        this.cancel();
                    }
                },1000);
                counter++;
            }

            AccessibilityNodeInfo pluse = findFirst(AbstractTF.newId("com.tencent.qqlive:id/erq"));
            if(pluse!=null&&counter == 0) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        AccessibilityNodeInfo pluse = findFirst(AbstractTF.newId("com.tencent.qqlive:id/erq"));
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, video);
                        pluse.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                        counter++;
                        this.cancel();
                    }
                }, 1000);
            }

            AccessibilityNodeInfo searchText = findFirst(AbstractTF.newId("com.tencent.qqlive:id/frk"));
            if(searchText!=null&&counter==1){
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        AccessibilityNodeInfo search = findFirst(AbstractTF.newId("com.tencent.qqlive:id/frk"));
                        clickView(search);
                        this.cancel();
                    }
                },1000);
                counter++;
            }

            List<AccessibilityNodeInfo> broadcast = findAll(AbstractTF.newText("立即播放",true));
            //System.out.println(broadcast.size()+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            if(counter==2){
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        List<AccessibilityNodeInfo> broadcast = findAll(AbstractTF.newText("立即播放",true));
                        searchClick(broadcast);
                        this.cancel();
                    }
                },3000);
                counter++;
            }
        }
        if(softName.equals("高德地图")) {
            Log.e("测试高德地图","开始测试");
            if(haveCommand.equals("yes"))
            {
                AccessibilityNodeInfo speak = findFirst(AbstractTF.newId("com.autonavi.minimap:id/lottie_view"));
                if(speak!=null)
                {
                    speak.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    haveCommand = "no";
                }else
                {
                    Log.e("测试高德地图","没找到");
                }
            }
        }if(softName.equals("智慧东大")){
            Log.e("测试智慧东大","开始测试");
            if(function.equals("健康填报")) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Timer timer = new Timer();
                //应用
                List<AccessibilityNodeInfo> applyItem = findAll(AbstractTF.newText("应用", true));
                if (null != applyItem && applyItem.size() != 0 && counter == -1) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            searchClick(applyItem);
                            this.cancel();
                        }
                    }, 1000);
                    counter++;
                }

                //健康信息填报
                List<AccessibilityNodeInfo> health = findAll(AbstractTF.newText("健康信息上报", true));
                if (null != health && health.size() != 0 && counter == 0) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            searchClick(health);
                            this.cancel();
                        }
                    }, 1500);
                    counter++;
                }

                //每日健康上报
                List<AccessibilityNodeInfo> dailyReport = findAll(AbstractTF.newContentDescription("待填报 " + new Date().getDate() + " 每日健康上报 Daily health report", true));
                if (null != dailyReport && dailyReport.size() != 0 && counter == 1) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            searchClick(dailyReport);
                            this.cancel();
                        }
                    }, 2500);
                    counter++;
                }

                //本人上报
                List<AccessibilityNodeInfo> selfRadio = findAll(AbstractTF.newClassName("android.widget.RadioButton"));
                if (null != selfRadio && selfRadio.size() != 0&&counter==2) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            clickView(selfRadio.get(0));
                            this.cancel();
                        }
                    }, 500);
                    counter++;
                }
            }else if(!SoftwareService.barItem.equals("")&&SoftwareService.function.equals("")){
                Log.e("智慧东大记录",SoftwareService.function +"    "+SoftwareService.barItem);
                try {
                    Thread.sleep(500);
                    Timer timer = new Timer();
                    //应用
                    List<AccessibilityNodeInfo> applyItem = findAll(AbstractTF.newText(barItem, true));
                    System.out.println(applyItem.toString()+applyItem.size());
                    if (null != applyItem && applyItem.size() != 0 && counter == -1) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                searchClick(applyItem);
                                this.cancel();
                            }
                        }, 1000);
                        counter++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else if(SoftwareService.barItem.equals("应用")&&SoftwareService.function.equals("课程表"))
            {
                Log.e("智慧东大记录",SoftwareService.function +"    "+SoftwareService.barItem);
                try {
                    Thread.sleep(500);
                    Timer timer = new Timer();
                    //应用
                    List<AccessibilityNodeInfo> applyItem = findAll(AbstractTF.newText(barItem, true));
                    System.out.println(applyItem.toString()+applyItem.size());
                    if (null != applyItem && applyItem.size() != 0 && counter == -1) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                searchClick(applyItem);
                                this.cancel();
                            }
                        }, 1000);
                        counter++;
                    }
                    List<AccessibilityNodeInfo> functionItem = findAll(AbstractTF.newText(function, true));
                    if (null != functionItem && functionItem.size() != 0 && counter == 0) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                searchClick(functionItem);
                                this.cancel();
                            }
                        }, 1000);
                        counter++;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else if(SoftwareService.barItem.equals("应用")&&SoftwareService.function.equals("校历"))
            {
                Log.e("智慧东大记录",SoftwareService.function +"    "+SoftwareService.barItem);
                try {
                    Thread.sleep(500);
                    Timer timer = new Timer();
                    //应用
                    List<AccessibilityNodeInfo> applyItem = findAll(AbstractTF.newText(barItem, true));
                    System.out.println(applyItem.toString()+applyItem.size());
                    if (null != applyItem && applyItem.size() != 0 && counter == -1) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                searchClick(applyItem);
                                this.cancel();
                            }
                        }, 1000);
                        counter++;
                    }
                    List<AccessibilityNodeInfo> functionItem = findAll(AbstractTF.newText(function, true));
                    if (null != functionItem && functionItem.size() != 0 && counter == 0) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                searchClick(functionItem);
                                this.cancel();
                            }
                        }, 1000);
                        counter++;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (serviceImpl != null) {
            serviceImpl.onAccessibilityEvent(event);
//            Log.e(TAG, "onAccessibilityEvent: 跳过广告");
        }
    }
    public void clickFunction(String func){
        Timer timer = new Timer();
        List<AccessibilityNodeInfo> fun = findAll(AbstractTF.newText(func, true));
        if (null != fun && fun.size() != 0 && counter == 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    searchClick(fun);
                    this.cancel();
                }
            }, 1500);
            counter++;
        }
    }

    public void clickBarItem(String item){
        try {
            Thread.sleep(500);
            Timer timer = new Timer();
            //应用
            List<AccessibilityNodeInfo> applyItem = findAll(AbstractTF.newText(item, true));
            if (null != applyItem && applyItem.size() != 0 && counter == -1) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        searchClick(applyItem);
                        this.cancel();
                    }
                }, 1000);
                counter++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        if (serviceImpl != null) {
            serviceImpl.onInterrupt();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //qqMusicComand = intent.getStringExtra("qqMusicCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    //初始化
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (serviceImpl == null) {
            serviceImpl = new TouchHelperServiceImpl(this);
        }
        if (serviceImpl != null) {
            serviceImpl.onServiceConnected();
        }
    }
    @Override
    public boolean onUnbind(Intent intent) {
        if (serviceImpl != null) {
            serviceImpl.onUnbind(intent);
            serviceImpl = null;
        }
        return super.onUnbind(intent);
    }


    /**
     * 点击该控件
     *
     * @return true表示点击成功
     */
    public boolean clickView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            } else {
                AccessibilityNodeInfo parent = nodeInfo.getParent();
                if (parent != null) {
                    boolean b = clickView(parent);
                    parent.recycle();
                    if (b) return true;
                }
            }
        }
        return false;
    }

    /**
     * 查找第一个匹配的控件
     *
     * @param tfs 匹配条件，多个AbstractTF是&&的关系，如：
     *            AbstractTF.newContentDescription("表情", true),AbstractTF.newClassName(AbstractTF.ST_IMAGEVIEW)
     *            表示描述内容是'表情'并且是imageview的控件
     */
    @Nullable
    public AccessibilityNodeInfo findFirst(@NonNull AbstractTF... tfs) {
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null) return null;

        int idTextTFCount = 0, idTextIndex = 0;
        for (int i = 0; i < tfs.length; i++) {
            if (tfs[i] instanceof AbstractTF.IdTextTF) {
                idTextTFCount++;
                idTextIndex = i;
            }
        }
        switch (idTextTFCount) {
            case 0://id或text数量为0，直接循环查找
                AccessibilityNodeInfo returnInfo = findFirstRecursive(rootInfo, tfs);
                rootInfo.recycle();
                return returnInfo;
            case 1://id或text数量为1，先查出对应的id或text，然后再查其他条件
                if (tfs.length == 1) {
                    AccessibilityNodeInfo returnInfo2 = ((AbstractTF.IdTextTF) tfs[idTextIndex]).findFirst(rootInfo);
                    rootInfo.recycle();
                    return returnInfo2;
                } else {
                    List<AccessibilityNodeInfo> listIdText = ((AbstractTF.IdTextTF) tfs[idTextIndex]).findAll(rootInfo);
                    if ( isEmptyArray(listIdText)) {
                        break;
                    }
                    AccessibilityNodeInfo returnInfo3 = null;
                    for (AccessibilityNodeInfo info : listIdText) {//遍历找到匹配的
                        if (returnInfo3 == null) {
                            boolean isOk = true;
                            for (AbstractTF tf : tfs) {
                                if (!tf.checkOk(info)) {
                                    isOk = false;
                                    break;
                                }
                            }
                            if (isOk) {
                                returnInfo3 = info;
                            } else {
                                info.recycle();
                            }
                        } else {
                            info.recycle();
                        }
                    }
                    rootInfo.recycle();
                    return returnInfo3;
                }
            default:
                throw new RuntimeException("由于时间有限，并且多了也没什么用，所以IdTF和TextTF只能有一个");
        }
        rootInfo.recycle();
        return null;
    }

    /**
     * @param tfs 由于是递归循环，会忽略IdTF和TextTF
     */
    public  AccessibilityNodeInfo findFirstRecursive(AccessibilityNodeInfo parent, @NonNull AbstractTF... tfs) {
        if (parent == null) return null;
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        for (int i = 0; i < parent.getChildCount(); i++) {
            AccessibilityNodeInfo child = parent.getChild(i);
            if (child == null) continue;
            boolean isOk = true;
            for (AbstractTF tf : tfs) {
                if (!tf.checkOk(child)) {
                    isOk = false;
                    break;
                }
            }
            if (isOk) {
                return child;
            } else {
                AccessibilityNodeInfo childChild = findFirstRecursive(child, tfs);
                child.recycle();
                if (childChild != null) {
                    return childChild;
                }
            }
        }
        return null;
    }

    /**
     * 查找全部匹配的控件
     *
     * @param tfs 匹配条件，多个AbstractTF是&&的关系，如：
     *            AbstractTF.newContentDescription("表情", true),AbstractTF.newClassName(AbstractTF.ST_IMAGEVIEW)
     *            表示描述内容是'表情'并且是imageview的控件
     */
    @NonNull
    public List<AccessibilityNodeInfo> findAll(@NonNull AbstractTF... tfs) {
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null) return list;

        int idTextTFCount = 0, idTextIndex = 0;
        for (int i = 0; i < tfs.length; i++) {
            if (tfs[i] instanceof AbstractTF.IdTextTF) {
                idTextTFCount++;
                idTextIndex = i;
            }
        }
        switch (idTextTFCount) {
            case 0://id或text数量为0，直接循环查找
                findAllRecursive(list, rootInfo, tfs);
                break;
            case 1://id或text数量为1，先查出对应的id或text，然后再循环
                List<AccessibilityNodeInfo> listIdText = ((AbstractTF.IdTextTF) tfs[idTextIndex]).findAll(rootInfo);
                if ( isEmptyArray(listIdText)) {
                    break;
                }
                if (tfs.length == 1) {
                    list.addAll(listIdText);
                } else {
                    for (AccessibilityNodeInfo info : listIdText) {
                        boolean isOk = true;
                        for (AbstractTF tf : tfs) {
                            if (!tf.checkOk(info)) {
                                isOk = false;
                                break;
                            }
                        }
                        if (isOk) {
                            list.add(info);
                        } else {
                            info.recycle();
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("由于时间有限，并且多了也没什么用，所以IdTF和TextTF只能有一个");
        }
        rootInfo.recycle();
        return list;
    }

    /*找到一个页面中所有的空间*/
    public List<AccessibilityNodeInfo> getAllInfos(AccessibilityNodeInfo rootInfo) {
        LinkedList<AccessibilityNodeInfo> queue = new LinkedList<>();
        queue.addLast(rootInfo);
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        list.add(rootInfo);
        while (!queue.isEmpty()) {
            AccessibilityNodeInfo node = queue.pollFirst();
            list.add(node);
            int counts = node.getChildCount();
            if (node.getText()!=null) {
                Log.e("has Text","yes");
                Log.e("nodeTest", (String) node.getText());
            }
            for(int i=0;i<counts;i++) {
                queue.addLast(node.getChild(i));
            }
        }
        Log.e("listSize", "list"+list.size());
        return list;
    }
    /**
     * @param tfs 由于是递归循环，会忽略IdTF和TextTF
     */
    public void findAllRecursive(List<AccessibilityNodeInfo> list, AccessibilityNodeInfo parent, @NonNull AbstractTF... tfs) {
        if (parent == null || list == null) return;
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        for (int i = 0; i < parent.getChildCount(); i++) {
            AccessibilityNodeInfo child = parent.getChild(i);
            if (child == null) continue;
            boolean isOk = true;
            for (AbstractTF tf : tfs) {
                if (!tf.checkOk(child)) {
                    isOk = false;
                    break;
                }
            }
            if (isOk) {
                list.add(child);
            } else {
                findAllRecursive(list, child, tfs);
                child.recycle();
            }
        }
    }
    //集合是否是空的
    public boolean isEmptyArray(Collection list) {
        return list == null || list.size() == 0;
    }
    //searchClick
    public boolean searchClick(List<AccessibilityNodeInfo> list){
        if(list.size()==0){
            return false;
        }else{
            for(AccessibilityNodeInfo nodeInfo:list){
                clickView(nodeInfo);
                nodeInfo.recycle();
                break;
            }
            return true;
        }
    }
    public void dispatchGestureClick(int x, int y) {
        Path path = new Path();
        path.moveTo(x - 1, y - 1);
        path.lineTo(x + 1, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                (path, 0, 100)).build(), null, null);
    }
    public void clickNodeByRect(AccessibilityNodeInfo node) {
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        int x = (rect.left+rect.right)/2;
        int y = (rect.top+rect.bottom)/2;
        dispatchGestureClick(x,y);
    }
}