package com.speechAndroid;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyWindowManager {

    public static final String TAG = "MyWindowManager";
    // 悬浮窗View的实例
    private static FloatWindowSmallView smallWindow;
    private static FloatWindowBigView bigWindow;

    // 悬浮窗View的参数
    private static WindowManager.LayoutParams smallWindowParams;
    private static WindowManager.LayoutParams bigWindowParams;

    // 在屏幕上添加或移除悬浮窗
    private static WindowManager mWindowManager;

    // 获取手机可用内存
    private static ActivityManager mActivityManager;

    private static Point mPoint = new Point();

    public static boolean bool = true;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     * @param context --必须为应用程序的Context.
     */
    public static void createSmallWindow(Context context) {
        Log.d(TAG, "------------------------------createSmallWindow");
        WindowManager windowManager = getWindowManager(context);
        windowManager.getDefaultDisplay().getSize(mPoint);
        int screenWidth = mPoint.x;
        int screenHeight = mPoint.y;
        Log.d(TAG, "---wigth----" + screenWidth + "---height---" + screenHeight);

        if (smallWindow == null) {
            Log.d(TAG, "------- small window null ");
            smallWindow = new FloatWindowSmallView(context);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = FloatWindowSmallView.viewWidth;
                smallWindowParams.height = FloatWindowSmallView.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
            Log.d(TAG, "----------------------------add view");
        }
        else {
            Log.d(TAG, "------- small window not null ");
        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     * @param context --必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
            bool = false;
        }
    }

    /**
     * 创建一个大悬浮窗。位置为屏幕正中间。
     * @param context --必须为应用程序的Context.
     */
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        windowManager.getDefaultDisplay().getSize(mPoint);
        int screenWidth = mPoint.x;
        int screenHeight = mPoint.y;
        if (bigWindow == null) {
            bigWindow = new FloatWindowBigView(context);
            if (bigWindowParams == null) {
                bigWindowParams = new WindowManager.LayoutParams();
                bigWindowParams.x = screenWidth - FloatWindowBigView.viewWidth; //screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
                bigWindowParams.y = 0; //screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
                bigWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                bigWindowParams.width = FloatWindowBigView.viewWidth;
                bigWindowParams.height = screenHeight; // FloatWindowBigView.viewHeight;
            }
            windowManager.addView(bigWindow, bigWindowParams);
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     * @param context --必须为应用程序的Context.
     */
    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
     * @param context --可传入应用程序上下文。
     */
    public static void updateUsedPercent(Context context) {
        if (smallWindow != null) {
            TextView percentView = (TextView) smallWindow.findViewById(R.id.percent);
            percentView.setText(getUsedPercentValue(context));
        }
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return smallWindow != null || bigWindow != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     * @param context --必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     * @param context --可传入应用程序上下文。
     * @return ActivityManager的实例，用于获取手机可用内存。
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     * @param context --可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */
    public static String getUsedPercentValue(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
            return percent + "%";
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return "悬浮窗";
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     * @param context  --可传入应用程序上下文。
     * @return 当前可用内存。
     */
    private static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }
}