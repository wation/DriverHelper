package com.wation.driverhelper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wation on 17/7/23.
 */

public class ConfigAdmin {
    private SharedPreferences sharedPreferences; //设置的文件类型问本app访问; // 配置文件

    public ConfigAdmin(Context context) {
        sharedPreferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
    }

    public void updatePlayTime(long currentTimeMs) {
        sharedPreferences.edit().putLong("lastPlayTime", currentTimeMs).commit();
        sharedPreferences.edit().putInt("type", 1).commit();
    }

    public void updatePauseTime(long currentTimeMs) {
        sharedPreferences.edit().putLong("lastPauseTime", currentTimeMs).commit();
        sharedPreferences.edit().putInt("type", 2).commit();
    }

    public long getLastPauseTime() {
        long startTimeStemp = sharedPreferences.getLong("lastPauseTime", -1);

        return startTimeStemp;
    }

    public long getLastPlayTime() {
        long startTimeStemp = sharedPreferences.getLong("lastPlayTime", -1);

        return startTimeStemp;
    }

    public TASK_STATUS getLastStatus() {
        int type = sharedPreferences.getInt("type", 0);
        switch (type) {
            case 1:
                return TASK_STATUS.TS_PLAY;
            case 2:
                return TASK_STATUS.TS_PAUSE;
        }

        return TASK_STATUS.TS_INIT;
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }

    public void setStartTime(long currentTimeMs) {
        sharedPreferences.edit().putLong("fullStartTimeStemp", currentTimeMs).commit();
    }

    public long getStartTime() {
        return sharedPreferences.getLong("fullStartTimeStemp", -1);
    }
}
