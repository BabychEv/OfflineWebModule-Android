package com.webmodule.offlinemodule.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webmodule.offlinemodule.module.OfflineWebModule;

public class FeedBackReceiver extends BroadcastReceiver {
    public static final String FEEDBACK_ACTION = "offlinemodule_receiver_feedback";
    private OfflineWebModule webModule;

    public FeedBackReceiver(OfflineWebModule webModule) {
        this.webModule = webModule;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(FEEDBACK_ACTION))
                webModule.sendFeedBack(intent.getStringExtra(FEEDBACK_ACTION));
        }
    }
}
