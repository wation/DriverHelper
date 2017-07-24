package com.wation.keepalive;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by Administrator on 2017/3/1.
 */

public class KeepAliveManager {
    private static KeepAliveManager instance;
    public static KeepAliveManager getInstance() {
        if (instance == null) {
            instance = new KeepAliveManager();
        }
        return instance;
    }

    private KeepAliveActivity activity;
    public void setKeepKeepAliveActivity(KeepAliveActivity activity) {
        this.activity = activity;
    }

    public void startKeepAliveActivity(Context context) {
        if (context != null) {
            Intent intent = new Intent(context, KeepAliveActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void finishKeepAliveActivity(Context context) {
        if (activity != null && !activity.isDestroyed()) {
            activity.finish();
        }
        activity = null;
    }

    public void startKeepAliveService(Context context) {
        if (context != null) {
            Intent intent = new Intent(context, KeepAliveService.class);
            context.startService(intent);
        }
    }

    public void startKeepAliveServiceBackground(Context context) {
        if (context != null) {
            Intent intent = new Intent(context, KeepAliveService.class);
            intent.putExtra("background", true);
            context.startService(intent);
        }
    }

    public void startKeepAliveServiceForeground(Context context) {
        if (context != null) {
            Intent intent = new Intent(context, KeepAliveService.class);
            intent.putExtra("background", false);
            context.startService(intent);
        }
    }

    public void setForeground(final Service keepAliveService, final Service innerService) {
        if (keepAliveService != null) {
            keepAliveService.startForeground(1, new Notification());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (innerService != null) {
                    innerService.startForeground(1, new Notification());
                    innerService.stopSelf();
                }
            }
        }
    }
}
