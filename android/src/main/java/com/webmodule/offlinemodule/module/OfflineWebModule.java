package com.webmodule.offlinemodule.module;

import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.activity.FullscreenActivity;
import com.webmodule.offlinemodule.broadcast.FeedBackReceiver;
import com.webmodule.offlinemodule.broadcast.RootBroadcastReceiver;
import com.webmodule.offlinemodule.rest.settings.DownloadFromURLClient;

public class OfflineWebModule extends ReactContextBaseJavaModule {
    private static final String WEB_CLICK_KEY = "web_click_key";
    private static final String WEB_CLICK_EVENT = "web_module_click_event";
    private static final String PRINT_MODE_KEY = "print_mode_key";
    private static final String PRINT_MODE_EVENT = "print_mode_event";
    private static final String UPDATE_ADMIN_SETTINGS_KEY = "update_admin_settings_key";
    private static final String UPDATE_ADMIN_SETTINGS_EVENT = "update_admin_settings_event";
    private static final String DOWNLOAD_FROM_RESULT_KEY = "download_from_result_key";
    private static final String DOWNLOAD_FROM_RESULT_EVENT = "download_from_result_event";
    private final DownloadFromURLClient downloadFromURLClient;

    public OfflineWebModule(ReactApplicationContext reactContext) {
        super(reactContext);
        FeedBackReceiver receiver = new FeedBackReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(FeedBackReceiver.FEEDBACK_ACTION);
        filter.addAction(FeedBackReceiver.PRINT_MODE_ACTION);
        filter.addAction(FeedBackReceiver.UPDATE_ACTION);
        filter.addAction(FeedBackReceiver.DOWNLOAD_FROM_RESULT);
        getReactApplicationContext().registerReceiver(receiver, filter);
        downloadFromURLClient = new DownloadFromURLClient(reactContext);
    }

    @Override public String getName() {
        return "OfflineWeb";
    }

    @ReactMethod
    public void startWebModule(String initialUrl) {
        Intent intent = new Intent(getReactApplicationContext(), FullscreenActivity.class);
        intent.putExtra(Constants.INITIAL_URL_KEY, initialUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void onBackPressed() {
        getReactApplicationContext().sendBroadcast(new Intent(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION));
    }

    public void sendFeedBack(String feedback) {
        send(feedback, WEB_CLICK_KEY, WEB_CLICK_EVENT);
    }

    public void sendPrintMode(String mode) {
        send(mode, PRINT_MODE_KEY, PRINT_MODE_EVENT);
    }

    public void sendUpdateSettings() {
        send(Constants.CLICK, UPDATE_ADMIN_SETTINGS_KEY, UPDATE_ADMIN_SETTINGS_EVENT);
    }

    @ReactMethod
    public void downloadFrom(String requestUrl, String token, String preferencesKey) {
        downloadFromURLClient.load(requestUrl, token, preferencesKey);
    }

    public void sendDownloadFromResult(String result) {
        send(result, DOWNLOAD_FROM_RESULT_KEY, DOWNLOAD_FROM_RESULT_EVENT);
    }

    private void send(String value, String valueKey, String event){
        WritableMap params = Arguments.createMap();
        params.putString(valueKey, value);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }
}
