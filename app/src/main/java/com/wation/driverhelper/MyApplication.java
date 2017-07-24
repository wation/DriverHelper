package com.wation.driverhelper;

import android.app.Application;
import android.content.Intent;

import com.wation.keepalive.KeepAliveService;

/**
 * Created by Administrator on 2017/7/24.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }

        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        // 启动保活进程
        sendBroadcast(new Intent(KeepAliveService.ACTION_KEEPALIVE));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
