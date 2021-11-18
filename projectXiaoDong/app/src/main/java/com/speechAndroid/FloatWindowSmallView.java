package com.speechAndroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

public class FloatWindowSmallView extends LinearLayout {

    public static int viewWidth;          // 悬浮窗的宽度
    public static int viewHeight;         // 悬浮窗的高度

    private static int statusBarHeight;   // 系统状态栏的高度
    private WindowManager windowManager;  // 更新小悬浮窗的位置

    private WindowManager.LayoutParams mParams;  // 小悬浮窗的参数

    private float xInScreen;              // 记录当前手指位置在屏幕上的横坐标值
    private float yInScreen;              // 记录当前手指位置在屏幕上的纵坐标值

    private float xDownInScreen;          // 记录手指按下时在屏幕上的横坐标的值
    private float yDownInScreen;          // 记录手指按下时在屏幕上的纵坐标的值

    private float xInView;                // 记录手指按下时在小悬浮窗的View上的横坐标的值
    private float yInView;                // 记录手指按下时在小悬浮窗的View上的纵坐标的值

    public FloatWindowSmallView(Context context) {
        super(context);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);

        View view = findViewById(R.id.small_window_layout);
        viewWidth  = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;

        TextView percentView = (TextView) findViewById(R.id.percent);
        percentView.setText(MyWindowManager.getUsedPercentValue(context));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();

                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;

            case MotionEvent.ACTION_MOVE:  // 手指移动的时候更新小悬浮窗的位置
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();
                break;

            case MotionEvent.ACTION_UP:    // 手指离开屏幕时，判断是否触发了单击事件
                // 若 xDownInScreen 和 xInScreen 相等
                // 且 yDownInScreen 和 yInScreen 相等
                // 则 视为触发了单击事件。
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
                    openBigWindow();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     * @param params    --小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }

    /**
     * 打开大悬浮窗，同时关闭小悬浮窗。
     */
    private void openBigWindow() {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    /**
     * 获取状态栏的高度。
     * @return   --返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");

                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}
