package com.webmodule.offlinemodule.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webmodule.offlinemodule.R;
import com.webmodule.offlinemodule.module.OfflineWebModule;

public class FeedBackReceiver extends BroadcastReceiver {
    public static final String FEEDBACK_ACTION = "offlinemodule_receiver_feedback";
    public static final String PRINT_MODE_ACTION = "offlinemodule_print_mode_feedback";
    public static final String SELECTED_PRINT_MODE = "offlinemodule_print_mode";
    public static final String UPDATE_ACTION = "offlinemodule_update_action";
    public static final String DOWNLOAD_FROM_RESULT = "offlinemodule_download_from_url_result";
    private OfflineWebModule webModule;

    public FeedBackReceiver(OfflineWebModule webModule) {
        this.webModule = webModule;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(FEEDBACK_ACTION))
                webModule.sendFeedBack(intent.getStringExtra(FEEDBACK_ACTION));
            if (intent.getAction().equals(PRINT_MODE_ACTION))
                webModule.sendPrintMode(getSelectedMode(context, intent.getIntExtra(SELECTED_PRINT_MODE, 1)));
            if (intent.getAction().equals(UPDATE_ACTION))
                webModule.sendUpdateSettings();
            if (intent.getAction().equals(DOWNLOAD_FROM_RESULT))
                webModule.sendDownloadFromResult(intent.getStringExtra(DOWNLOAD_FROM_RESULT));
        }
    }

    private String getSelectedMode(Context context, int id) {
        return context.getResources().getStringArray(R.array.connection_types)[id];
    }
}
