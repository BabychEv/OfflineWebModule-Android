package com.webmodule.offlinemodule.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webmodule.offlinemodule.activity.FullscreenActivity;

public class RootBroadcastReceiver extends BroadcastReceiver {
    public static final String CLOSE_ACTIVITY_ACTION = "close_web_view_activity_action";
    private FullscreenActivity activity;

    public RootBroadcastReceiver(FullscreenActivity activity) {
        this.activity = activity;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(CLOSE_ACTIVITY_ACTION))
                activity.finish();
        }
    }
}
