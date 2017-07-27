package com.wation.driverhelper;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;
import android.widget.TextView;

import com.wation.driverhelper.utils.AlarmManagerUtil;
import com.wation.driverhelper.utils.SystemUtil;
import com.wation.keepalive.KeepAliveManager;
import com.wation.keepalive.KeepAliveService;
import com.wation.keepalive.ScreenListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 数据存储使用SharedPreferences<br/>
 * 开启全局定时器计算时间<br/>
 * 辅助提醒通过设置闹钟实现<br/>
 * 增加后台保活<br/>
 */
@SuppressLint("ShowToast")
public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();
//    private final static long PLAY_DEADLINE_MS = (3 * 60) * 60 * 1000; // 3小时
//    private final static long PAUSE_DEADLINE_MS = 20 * 60 * 1000; // 20分
//    private final static long PAUSE_DEADLINE_MS_2 = PAUSE_DEADLINE_MS + (2 * 60 * 1000); // 22分
    private final static long PLAY_DEADLINE_MS = 2 * 60 * 1000; // 5 *
    private final static long PAUSE_DEADLINE_MS = 3 * 20 * 1000; // 10 *
    private final static long PAUSE_DEADLINE_MS_2 = PAUSE_DEADLINE_MS + (1 * 60 * 1000); // 22分

    private final static String ALARM_TIP_TEXT = "您该停车休息了";
    private final static String REST_TIP_TEXT = "休息时间到，可以继续开车了";

    private Anticlockwise mTimer; // 行程计时器
    private Anticlockwise mTimer0; // 疲劳计时器
    private Anticlockwise mTimer1; // 休息计时器

    private TextView textViewTip; // 警告提示

    private TASK_STATUS status = TASK_STATUS.TS_INIT; // 当前状态

    private Context mContext;

    private ConfigAdmin configAdmin;

    private Timer globalTimer; // 为了保证界面时间变化一致，使用一个定时器即可

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );

        mContext = this;

        configAdmin = new ConfigAdmin(this);

        mTimer = (Anticlockwise) findViewById(R.id.id_timer);
        mTimer0 = (Anticlockwise) findViewById(R.id.id_timer0);
        mTimer1 = (Anticlockwise) findViewById(R.id.id_timer1);
        mTimer.setType(Anticlockwise.TYPE_UNKNOWN);
        mTimer0.setType(Anticlockwise.TYPE_PLAY);
        mTimer1.setType(Anticlockwise.TYPE_PAUSE);

        textViewTip = (TextView) findViewById(R.id.textViewTip);

//        /**
//         * 屏幕监听器 黑屏杀死自己
//         */
//        ScreenListener mScreenListener = new ScreenListener(mContext);
//        mScreenListener.begin(new ScreenListener.ScreenStateListener() {
//
//            @Override
//            public void onUserPresent() {
//                Log.e(TAG, "onUserPresent");
//            }
//
//            @Override
//            public void onScreenOn() {
//                Log.e(TAG, "onScreenOn");
//            }
//
//            @Override
//            public void onScreenOff() {
//                Log.e(TAG, "onScreenOff");
//                MainActivity.this.finish();
//            }
//        });
    }

    private void setAlarmTipText() {
        textViewTip.setText(ALARM_TIP_TEXT);
        textViewTip.setTextColor(Color.RED);
    }

    private void setRestTipText() {
        textViewTip.setText(REST_TIP_TEXT);
        textViewTip.setTextColor(Color.GREEN);
    }

    private void clearTipText() {
        textViewTip.setText("");
    }

    private void initData() {
        /**
         * 读配置数据，获取之前保存的开始时间，然后用当前时间减去开始时间，计算出运行时长<br/>
         * 关于SharedPreferences操作，参考：http://blog.csdn.net/fengshizty/article/details/40340441
         */
        long currentTimeMs = System.currentTimeMillis();
        status = configAdmin.getLastStatus();
        long startTime = configAdmin.getStartTime();
        Log.i(TAG, "currentTimeStemp:" + currentTimeMs + ", status:" + status + ", startTime:" + startTime);
        long fullTimeDiffMs = currentTimeMs - startTime;
        Log.i(TAG, "fullTimeDiffMs:" + fullTimeDiffMs);
        // 更新界面
        setButtonsEnabledByTaskStatus(status);

        if (startTime >= 0) {
            startTimer(mTimer, fullTimeDiffMs);

            if (status == TASK_STATUS.TS_PLAY) { // 读取到数据，认为之前的任务未完成，继续计时
                long lastTime = configAdmin.getLastPlayTime();
                // 计算时间差
                long timeDiffMs = currentTimeMs - lastTime;
                Log.i(TAG, "timeDiffMs:" + timeDiffMs + ", lastTime:" + lastTime);
                startTimer(mTimer0, timeDiffMs);
            } else if (status == TASK_STATUS.TS_PAUSE) { // 读取到数据，认为之前的任务未完成，继续计时
                long lastTime = configAdmin.getLastPauseTime();
                // 计算时间差
                long timeDiffMs = currentTimeMs - lastTime;
                Log.i(TAG, "timeDiffMs:" + timeDiffMs + ", lastTime:" + lastTime);
                startTimer(mTimer1, timeDiffMs);

                // 当休息时，设置行车计数器运行
                lastTime = configAdmin.getLastPlayTime();
                // 计算时间差
                timeDiffMs = currentTimeMs - lastTime;
                mTimer0.setTime(timeDiffMs);
                mTimer0.setRunning(true);
            }
        }
        mTimer.updateText();
        mTimer0.updateText();
        mTimer1.updateText();

        // 启定时器
        if (globalTimer != null) {
            globalTimer.cancel();
        }
        globalTimer = new Timer();
        globalTimer.schedule(new TimerTask() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                Log.i(TAG, "run status:" + status);
                if (MainActivity.this.isDestroyed()) {
                    if (globalTimer != null) {
                        globalTimer.cancel();
                        globalTimer = null;
                    }
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 计数器自增
                        mTimer.timeAutoIncrement();
                        mTimer0.timeAutoIncrement();
                        mTimer1.timeAutoIncrement();

                        // 更新UI
                        mTimer.updateText();
                        mTimer0.updateText();
                        mTimer1.updateText();

                        // 报警
                        if (status == TASK_STATUS.TS_PLAY) {
                            // 行车计时器
                            if (mTimer0.getTime() > PLAY_DEADLINE_MS && !mTimer0.isAlarming()) {
                                Log.i(TAG, "ALARM PLAY");
                                mTimer0.setAlarming(true);
                                SystemUtil.playAlarmMusic(mContext);
                                setAlarmTipText();
                            }
                        } else if (status == TASK_STATUS.TS_PAUSE) {
                            // 休息计时器
                            if (mTimer1.getTime() > PAUSE_DEADLINE_MS && !mTimer1.isAlarming()) {
                                Log.i(TAG, "ALARM PAUSE");
                                mTimer1.setAlarming(true);
                                SystemUtil.playRestMusic(mContext);
                                setRestTipText();
                            }
                        }

                        // 针对休息的不同处理
                        if (status == TASK_STATUS.TS_PAUSE) {
                            // 休息计时器
                            if (mTimer1.getTime() > PAUSE_DEADLINE_MS_2) {
                                Log.i(TAG, "set PLAY enabled");
                                // findViewById(R.id.button2).setEnabled(true); // 满21分才有效
                                // stopTimer(mTimer0, false, false); // 只有休息够了，时间才清零
                            }
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    PowerManager.WakeLock mPowerWakeLock;

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mPowerWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, this.getClass().getCanonicalName());
        mPowerWakeLock.acquire();

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                mPowerWakeLock.release();
//            }
//        }, 1000);

        initData();

        super.onResume();
    }

    private void startTimer(Anticlockwise timer, final long timeDiffMs) {
        if (timer != null) {
            timer.setRunning(true);
            timer.setTime(timeDiffMs);
            // 设置闹钟进行辅助，就算程序被杀掉同样被激活
            int type = timer.getType();
            long delay = -1;
            if (type == Anticlockwise.TYPE_PLAY) {
                long triggerAtMillis = PLAY_DEADLINE_MS - timeDiffMs + SystemClock.elapsedRealtime();
                Log.i(TAG, "PLAY, triggerAtMillis:" + triggerAtMillis);
                AlarmManagerUtil.setAlarm(mContext, triggerAtMillis, type, ALARM_TIP_TEXT, 2);

                // 发送广播到守护进程，确保指定时间内启动
                delay = PLAY_DEADLINE_MS - timeDiffMs - 5000;
            } else if (type == Anticlockwise.TYPE_PAUSE) {
                long triggerAtMillis = PAUSE_DEADLINE_MS - timeDiffMs + SystemClock.elapsedRealtime();
                Log.i(TAG, "PAUSE, triggerAtMillis:" + triggerAtMillis);
                AlarmManagerUtil.setAlarm(mContext, triggerAtMillis, type, REST_TIP_TEXT, 2);

                // 发送广播到守护进程，确保指定时间内启动
                delay = PAUSE_DEADLINE_MS - timeDiffMs - 5000;
            }

            if (delay >= 0) {
                Intent intent = new Intent(KeepAliveService.ACTION_KEEPALIVE);
                Log.i(TAG, "delay:" + delay);
                intent.putExtra("delay", delay); // 提前5秒启动程序
                sendBroadcast(intent);
            }
        }
    }

    private void stopTimer(Anticlockwise timer) {
        if (timer != null) {
            if (timer.getType() == Anticlockwise.TYPE_PLAY || timer.getType() == Anticlockwise.TYPE_PAUSE) {
                AlarmManagerUtil.cancelAlarm(mContext, AlarmManagerUtil.ALARM_ACTION, timer.getType());
            }
            timer.setRunning(false);
            timer.setAlarming(false);
        }
    }

    private void stopTimer(Anticlockwise timer, boolean resetTime, boolean keepTimeUpdate) {
        if (timer != null) {
            if (timer.getType() == Anticlockwise.TYPE_PLAY || timer.getType() == Anticlockwise.TYPE_PAUSE) {
                AlarmManagerUtil.cancelAlarm(mContext, AlarmManagerUtil.ALARM_ACTION, timer.getType());
            }
            if (resetTime) {
                timer.reset();
            }
            timer.setRunning(keepTimeUpdate);
            timer.setAlarming(false);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
//         releaseWakeLock();
        if (globalTimer != null) {
            globalTimer.cancel();
            globalTimer = null;
        }
        System.out.println("执行 onDestroy()");

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "event:" + event);
        mPowerWakeLock.release();
        return super.onTouchEvent(event);
    }

//    //以下两个函数，是用来使程序息屏后，继续执行。（但未实现）
//    private void acquireWakeLock() {      //使程序不休眠
//        if (wakeLock == null) {
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
//            wakeLock.acquire();
//        }
//    }
//
//    private void releaseWakeLock() {     //释放
//        if (wakeLock != null && wakeLock.isHeld()) {
//            wakeLock.release();
//            wakeLock = null;
//        }
//    }

    public void btnClick(View v) {
        int id = v.getId();

        // 停止报警
        SystemUtil.stopMusic();

        // 清除文字提示
        clearTipText();

        long currentTimsMs = System.currentTimeMillis();

        switch (id) {
            case R.id.button1:   //出車
                status = TASK_STATUS.TS_PLAY; // 设置按钮状态

                // 保存到配置文件，更新开始时间，设置为行车类型
                configAdmin.setStartTime(currentTimsMs);
                configAdmin.updatePlayTime(currentTimsMs);

                startTimer(mTimer, 0); // 行程计时器开始计时
                startTimer(mTimer0, 0); // 行车计时器开始计时
                stopTimer(mTimer1, true, false); // 休息计时器停止计时

                break;

            case R.id.button2:  //行車 // 设置按钮状态
                status = TASK_STATUS.TS_PLAY; // 设置按钮状态

                // 考虑临时停车，只有休息足够时间才重新计算行车时间
                if (mTimer1.getTime() > PAUSE_DEADLINE_MS_2) {
                    // 保存到配置文件，更新开始时间，设置为行车类型
                    configAdmin.updatePlayTime(currentTimsMs);

                    startTimer(mTimer0, 0); // 行车计时器开始计时
                } else {
                    startTimer(mTimer0, mTimer0.getTime()); // 行车计时器继续计时
                }
                stopTimer(mTimer1, true, false); // 休息计时器停止计时

                break;

            case R.id.button3:  //休息
                status = TASK_STATUS.TS_PAUSE; // 设置按钮状态

                // 保存配置文件，更新时间，设置为休息类型
                configAdmin.updatePauseTime(currentTimsMs);

                startTimer(mTimer1, 0); // 休息计时器开始计时
                stopTimer(mTimer0, false, true); // 行车计时器停止计时
                break;

            case R.id.button4:  //完成
                status = TASK_STATUS.TS_STOP; // 设置按钮状态

                // 清空配置文件
                configAdmin.clear();

                stopTimer(mTimer, true, false); // 行程计时器停止计时
                stopTimer(mTimer0, true, false); // 行车计时器停止计时
                stopTimer(mTimer1, true, false); // 休息计时器停止计时

                break;

            default:
                break;
        }

        setButtonsEnabledByTaskStatus(status);
    }

    private void setButtonsEnabledByTaskStatus(TASK_STATUS status) {
        Log.i(TAG, "setButtonsEnabledByTaskStatus status：" + status);

        switch (status) {
            case TS_INIT: // 初始状态，只有出车有效
            case TS_STOP: // 完成状态，恢复初始状态
                findViewById(R.id.button1).setEnabled(true);
                findViewById(R.id.button2).setEnabled(false);
                findViewById(R.id.button3).setEnabled(false);
                findViewById(R.id.button4).setEnabled(false);
                break;

            case TS_PLAY: // 行车状态，只有休息、完成有效
                findViewById(R.id.button1).setEnabled(false);
                findViewById(R.id.button2).setEnabled(false);
                findViewById(R.id.button3).setEnabled(true);
                findViewById(R.id.button4).setEnabled(true);
                break;

            case TS_PAUSE: // 休息状态，只有行车有效
                findViewById(R.id.button1).setEnabled(false);
                findViewById(R.id.button2).setEnabled(true);
                findViewById(R.id.button3).setEnabled(false);
                findViewById(R.id.button4).setEnabled(true);
                break;
        }
    }

}

