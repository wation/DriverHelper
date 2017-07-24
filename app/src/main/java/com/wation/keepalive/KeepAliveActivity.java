package com.wation.keepalive;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

/**
 * 监控手机锁屏解锁事件，在屏幕锁屏时启动1个像素的 Activity，在用户解锁时将 Activity 销毁掉。
 * 注意该 Activity 需设计成用户无感知。
 * 通过该方案，可以使进程的优先级在屏幕锁屏时间由4提升为最高优先级1
 * Created by Administrator on 2017/3/1.
 */

public class KeepAliveActivity extends Activity {
    private final static String TAG = "KeepAliveActivity";

//    Timer loginHxLooperTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        KeepAliveManager.getInstance().setKeepKeepAliveActivity(this);

        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
//        loginHxLooperTimer = new Timer();
//        loginHxLooperTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.e(TAG, "onScreenOff:run");
//                if (AppRuntimeWorker.getInitActModel() == null
//                        || AppRuntimeWorker.getInitActModel().getUser() == null) {
//                    return;
//                }
//                String mobile = AppRuntimeWorker.getInitActModel().getUser().getMobile();
//                Log.e(TAG, "HX login, mobile:" + mobile);
//                HXVideoTalkManager.getInstance().login(new EMCallBack() {
//                    @Override
//                    public void onSuccess() {
//                        Log.i(TAG, "login HX success");
//                    }
//
//                    @Override
//                    public void onError(int i, String s) {
//                        Log.i(TAG, "login HX onError, i:" + i + ",s:" + s);
//                    }
//
//                    @Override
//                    public void onProgress(int i, String s) {
//                        Log.i(TAG, "login HX onProgress, i:" + i + ",s:" + s);
//                    }
//                }, mobile, mobile);
//            }
//        }, 5000, 5000);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
//        if (loginHxLooperTimer != null) {
//            loginHxLooperTimer.cancel();
//        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }
}
