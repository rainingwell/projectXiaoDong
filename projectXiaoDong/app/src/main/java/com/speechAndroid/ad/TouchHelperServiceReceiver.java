package com.speechAndroid.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.speechAndroid.SoftwareService;

public class TouchHelperServiceReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast, just dispatch message to TouchHelperService
        String action = intent.getAction();
        Log.d(TAG, action);
        if(action.contains("PACKAGE_ADDED") || action.contains("PACKAGE_REMOVED")) {
            if (SoftwareService.serviceImpl != null) {
                SoftwareService.serviceImpl.receiverHandler.sendEmptyMessage(SoftwareService.ACTION_REFRESH_PACKAGE);
            }
        }
    }
}
