package com.wation.driverhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wation.driverhelper.utils.AlarmManagerUtil;
import com.wation.driverhelper.utils.SystemUtil;

/**
 * 接收闹钟广播
 * Created by wation on 17/7/23.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = AlarmBroadcastReceiver.class.getSimpleName();
    public final static String ACTION_ALARM_CLOCK = "com.wation.alarm.clock";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");

        if (!SystemUtil.isAppOnForeground(context, MainActivity.class.getName())) {

            Intent clockIntent = new Intent(context, MainActivity.class);
            clockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(clockIntent);
            Log.i(TAG, "startActivity");
        }

        // 取消当前闹钟
        int id = intent.getIntExtra("id", 0);
        AlarmManagerUtil.cancelAlarm(context, AlarmManagerUtil.ALARM_ACTION, id);
    }
}
