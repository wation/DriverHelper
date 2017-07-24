package com.wation.driverhelper.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by wation on 17/7/23.
 */

public class AlarmManagerUtil {
    public static final String ALARM_ACTION = "com.test.alarm.clock";

    public static void cancelAlarm(Context context, String action, int id) {
        Intent intent = new Intent(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * @param id              闹钟的id
     * @param triggerAtMillis 开机后开始计算的毫秒值
     * @param tips            闹钟提示信息
     * @param soundOrVibrator 2表示声音和震动都执行，1表示只有铃声提醒，0表示只有震动提醒
     */
    public static void setAlarm(Context context, long triggerAtMillis, int id, String tips, int soundOrVibrator) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = 0;
        Intent intent = new Intent(ALARM_ACTION);
        intent.putExtra("msg", tips);
        intent.putExtra("id", id);
        intent.putExtra("soundOrVibrator", soundOrVibrator);
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis,
                    intervalMillis, sender);
        } else {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, sender);
        }
    }
}
