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

public class OfflineWebModule extends ReactContextBaseJavaModule {
    private static final String WEB_CLICK_KEY = "web_click_key";
    private static final String WEB_CLICK_EVENT = "web_module_click_event";
    private final FeedBackReceiver receiver;

    public OfflineWebModule(ReactApplicationContext reactContext) {
        super(reactContext);
        receiver = new FeedBackReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(FeedBackReceiver.FEEDBACK_ACTION);
        filter.addAction(FeedBackReceiver.CLOSE_ACTION);
        getReactApplicationContext().registerReceiver(receiver, filter);
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
        WritableMap params = Arguments.createMap();
        params.putString(WEB_CLICK_KEY, feedback);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(WEB_CLICK_EVENT, params);
    }

    public void close() {
        getReactApplicationContext().unregisterReceiver(receiver);
    }
}
