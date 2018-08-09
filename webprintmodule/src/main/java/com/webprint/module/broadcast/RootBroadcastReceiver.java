package com.webprint.module.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webprint.module.activity.PrintActivity;
import com.webprint.module.activity.BluetoothActivity;

public class RootBroadcastReceiver extends BroadcastReceiver {
    public static final String CLOSE_ACTIVITY_ACTION = "com.webprint.module.broadcast.RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION";
    private BluetoothActivity bluetoothActivity;
    private PrintActivity printActivity;

    public RootBroadcastReceiver(BluetoothActivity activity) {
        this.bluetoothActivity = activity;
    }

    public RootBroadcastReceiver(PrintActivity activity) {
        this.printActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(CLOSE_ACTIVITY_ACTION)) {
                if(bluetoothActivity != null) {
                    bluetoothActivity.finishAndRemoveTask();
                }
                if(printActivity != null) {
                    printActivity.finishAndRemoveTask();
                }
            }
        }
    }
}
