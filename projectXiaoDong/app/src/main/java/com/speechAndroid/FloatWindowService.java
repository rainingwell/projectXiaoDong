package com.speechAndroid;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FloatWindowService extends Service {

    public static final String FWS_TAG = "FloatWindowService";

    private Handler handler = new Handler();  // 在线程中创建或移除悬浮窗。
    private Timer timer;                      // 定时器，定时进行检测当前应该创建还是移除悬浮窗。

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(FWS_TAG, "------------定时器 <开启> 每 <10s> 刷新一次");
        if (timer == null) {                  // 开启定时器，每隔10秒刷新一次
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 1000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(FWS_TAG, "------------被终止");
        super.onDestroy();
        //timer.cancel();                    // Service被终止的同时也停止定时器继续运行
        timer = null;
    }

    class RefreshTask extends TimerTask {

        @Override
        public void run() {
            // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
            if (isHome() && !MyWindowManager.isWindowShowing() && MyWindowManager.bool) {
                Log.d(FWS_TAG, "------------当前界面 <是> 桌面 <没有> 悬浮窗显示, 则 <创建> 悬浮窗");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }

            // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
            else if (!isHome() && MyWindowManager.isWindowShowing()) {
                Log.d(FWS_TAG, "------------当前界面 <不是> 桌面 <有> 悬浮窗显示, 则 <移除> 悬浮窗");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.removeSmallWindow(getApplicationContext());
                        MyWindowManager.removeBigWindow(getApplicationContext());
                    }
                });
            }

            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            else if (isHome() && MyWindowManager.isWindowShowing()) {
                Log.d(FWS_TAG, "------------当前界面 <是> 桌面 <有> 悬浮窗显示, 则 <更新> 内存数据");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }
        }
    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);

        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     * @return   --返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }
}