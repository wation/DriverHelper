package com.wation.keepalive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.wation.driverhelper.AlarmBroadcastReceiver;
import com.wation.driverhelper.MyApplication;
import com.wation.driverhelper.utils.SystemUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 保活服务： 单独进程 白屏 黑屏 1. 5秒心跳 1. 10秒心跳 2. 账号互斥停止心跳 2. 账号互斥停止心跳 3. 来电发广播 3. 来电发广播亮屏 Created by Administrator on 2017/3/6.
 */

public class KeepAliveService extends Service {

    private final static String TAG = "KeepAliveService";
    public final static String ACTION_KEEPALIVE = "com.wation.daemon.KeepAlive";
    private final static String MAIN_APP_PACKAGE = "com.wation.driverhelper";

    private static KeepAliveService mKeepAliveService;

    private ScreenListener mScreenListener;

    private final static int CHECK_ALIVE_INTERVAL = 10 * 1000;
    Timer checkAliveTimer;

    private Context mContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long delay = -1;
        if (intent.hasExtra("delay")) {
            delay = intent.getLongExtra("delay", -1);
        }
        Log.i(TAG, "onStartCommand delay:" + delay);

        // 如果APP在前台运行则不周期性发送心跳
        startCheckOnlineTimer(delay);

        return START_STICKY;
    }

    private void startCheckOnlineTimer(long delay) {
        Log.i(TAG, "startCheckOnlineTimer");

        if (delay < 0) {
            return;
        }

        if (checkAliveTimer != null) {
            checkAliveTimer.cancel();
            checkAliveTimer = null;
        }
        checkAliveTimer = new Timer();
        checkAliveTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Log.i(TAG, "run");
//                if (SystemUtil.isScreenLocked(mContext)) {
//                    Log.i(TAG, "screen locked, restart app.");
//                    SystemUtil.killProcess(mContext, MAIN_APP_PACKAGE);
//                }
                // 发送广播启动APP
                sendBroadcast(new Intent(AlarmBroadcastReceiver.ACTION_ALARM_CLOCK));
//                if (!SystemUtil.isAppRunning(mContext, MAIN_APP_PACKAGE)) {
//                    Log.w(TAG, "app not run, start it");
//
//                    // 发送广播启动APP
//                    sendBroadcast(new Intent(AlarmBroadcastReceiver.ACTION_ALARM_CLOCK));
//                }
            }
        }, 10, delay);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        mKeepAliveService = this;
        mContext = this;

        /**
         * 屏幕监听器 针对华为手机超级有效，如果不做如下处理，5分钟内必定被杀死 做了处理后可以一直运行。
         */
        if (mScreenListener == null) {
            mScreenListener = new ScreenListener(mContext);
        }
        mScreenListener.begin(new ScreenListener.ScreenStateListener() {

            @Override
            public void onUserPresent() {
                Log.e(TAG, "onUserPresent");
            }

            @Override
            public void onScreenOn() {
                Log.e(TAG, "onScreenOn");
                KeepAliveManager.getInstance().finishKeepAliveActivity(mContext);
                KeepAliveManager.getInstance().startKeepAliveService(mContext);
                startService(new Intent(mContext, InnerService.class));
            }

            @Override
            public void onScreenOff() {
                Log.e(TAG, "onScreenOff");
                KeepAliveManager.getInstance().startKeepAliveActivity(mContext);
                KeepAliveManager.getInstance().startKeepAliveService(mContext);
                startService(new Intent(mContext, InnerService.class));
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (checkAliveTimer != null) {
            checkAliveTimer.cancel();
            checkAliveTimer = null;
        }
        if (mScreenListener != null) {
            mScreenListener.unregisterListener();
            mScreenListener = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class InnerService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i(TAG, "InnerService onStartCommand");
            KeepAliveManager.getInstance().setForeground(mKeepAliveService, this);
            return super.onStartCommand(intent, flags, startId);
        }
    }
}
