package com.webprint.module.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webprint.module.module.WebPrintModule;

public class PrintBluetoothModuleReceiver extends BroadcastReceiver {

    public static final String ACTION_DEVICES_PAIRED = "com.webprint.module.broadcast.PrintBluetoothModuleReceiver.ACTION_DEVICES_PAIRED";
    public static final String ACTION_SEND_PRINT_TEXT = "com.webprint.module.broadcast.PrintBluetoothModuleReceiver.ACTION_SEND_PRINT_TEXT";

    private static final String RESULT_DEVICES_PAIRED = "bluetooth_devices_paired";
    private static final String RESULT_SEND_PRINT_TEXT = "send_print_text";

    private WebPrintModule module;

    public PrintBluetoothModuleReceiver(WebPrintModule module) {
        this.module = module;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_DEVICES_PAIRED)) {
                module.sendResult(intent.getStringExtra(RESULT_DEVICES_PAIRED));
            } else if (intent.getAction().equals(ACTION_SEND_PRINT_TEXT)) {
                module.sendResult(intent.getStringExtra(RESULT_SEND_PRINT_TEXT));
            }
        }
    }
}