package com.wation.keepalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wation.keepalive.KeepAliveManager;

/**
 * Created by Administrator on 2017/3/1.
 */

public class KeepAliveReceiver extends BroadcastReceiver {
    private final static String TAG = "KeepAliveReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int flags = intent.getFlags();

        Log.i(TAG, "action:" + action + ",flags:" + flags);

        Intent intent2 = new Intent(context, KeepAliveService.class);
        if (intent.hasExtra("delay")) {
            long delay = intent.getLongExtra("delay", -1);
            Log.i(TAG, "delay:" + delay);
            intent2.putExtra("delay", delay);
        }
        context.startService(intent2);
    }
}
