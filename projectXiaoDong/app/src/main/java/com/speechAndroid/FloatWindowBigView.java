package com.speechAndroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class FloatWindowBigView extends LinearLayout {
    private static final String TAG = "FloatWindowBigView";

    public static int viewWidth;   // 悬浮窗的宽度
    public static int viewHeight;  // 悬浮窗的高度

    public FloatWindowBigView(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_window_big, this);

        //获取屏幕宽高
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point(); display.getSize(point);
        int width = point.x;
        int height= point.y;
        Log.d(TAG, "+++++++ width:" + width + " height:" + height);

        View view  = findViewById(R.id.big_window_layout);
        viewWidth  = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;

        Log.d(TAG, "+++++++ viewWidth:" + viewWidth + " viewHeight:" + viewHeight);
        // 点击关闭悬浮窗的时候，移除悬浮窗
        Button close = (Button) findViewById(R.id.search_close_btn);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                Intent speechIntent = new Intent(getContext(),SpeechService.class);
                context.stopService(speechIntent);
                Intent intent = new Intent(getContext(), FloatWindowService.class);
                context.stopService(intent);
            }
        });

        // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });
    }
}
