package com.wation.driverhelper.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.text.TextUtils;
import android.util.Log;

import com.wation.driverhelper.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wation on 17/7/23.
 */

public class SystemUtil {
    private static final String TAG = SystemUtil.class.getSimpleName();
    private static boolean playing = false;
    private static Timer timer;
    private static final String ALARM_AUDIO_PATH = "/sdcard/DriverHelper/alarm.wav";
    private static final String REST_AUDIO_PATH = "/sdcard/DriverHelper/rest.wav";


    public static void playAlarmMusic(final Context context) {
        playMusic(context, ALARM_AUDIO_PATH);
    }

    public static void playRestMusic(final Context context) {
        playMusic(context, REST_AUDIO_PATH);
    }

    public static void playMusic(final Context context, final String audioPath) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        playing = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!playing) {
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    return;
                }

                AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int maxVolumeValue = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                int lastVolumeValue = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

                // 设置最大音量
                mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxVolumeValue, 0);

                SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
                soundPool.load(audioPath, 1);

                try {
                    Thread.sleep(200); // 给予初始化音乐文件足够时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "play sound.");
                soundPool.play(1, 1, 1, 0, 0, 1);
                soundPool.unload(1);

                // 恢复原来音量
                mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, lastVolumeValue, 0);
            }
        }, 10, 60 * 1000);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SoundPool soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
//                soundPool.load(context, R.raw.alarm,1);
//
//                try {
//                    Thread.sleep(100); // 给予初始化音乐文件足够时间
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                playing = true;
//                while (playing) {
//                    soundPool.play(1, 1, 1, 0, 0, 1);
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }

    public static void stopMusic() {
        playing = false;
    }

    private static long lastBroadCastLaunchTime = 0;
    private static long lastActivityLaunchTime = 0;

    public static long getLastBroadCastLaunchTime() {
        return lastBroadCastLaunchTime;
    }

    public static void setLastBroadCastLaunchTime(long lastBroadCastLaunchTime) {
        SystemUtil.lastBroadCastLaunchTime = lastBroadCastLaunchTime;
    }

    public static long getLastActivityLaunchTime() {
        return lastActivityLaunchTime;
    }

    public static void setLastActivityLaunchTime(long lastActivityLaunchTime) {
        SystemUtil.lastActivityLaunchTime = lastActivityLaunchTime;
    }

    public static boolean isLaunchedByAlarm() {
        return lastActivityLaunchTime - lastBroadCastLaunchTime > 0;
    }

    public static boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if(!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
            return true;
        }

        return false;
    }

    public static boolean isAppRunning(Context context, String packegeName) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packegeName) || info.baseActivity.getPackageName().equals(packegeName)) {
                isAppRunning = true;
                Log.i(TAG, info.topActivity.getPackageName() + " info.baseActivity.getPackageName()="+info.baseActivity.getPackageName());
                break;
            }
        }

        return isAppRunning;
    }

    /**
   * 需要权限:android.permission.GET_TASKS
   * 已测试华为手机、OPPO手机
   * @param context
   * @return
   */
    public static boolean isAppOnForeground(Context context, String activityClassPrefix) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            for (ActivityManager.RunningTaskInfo info : tasks) {
                ComponentName topActivity = info.topActivity;
                Log.i(TAG, "topActivity:" + topActivity.flattenToString());
                if (topActivity.getClassName().startsWith(activityClassPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list){
            if (runningAppProcessInfo.pid == pid){
                processName = runningAppProcessInfo.processName;
            }
        }

        Log.i(TAG,"当前进程名称:" + processName);

        return processName;
    }
}
