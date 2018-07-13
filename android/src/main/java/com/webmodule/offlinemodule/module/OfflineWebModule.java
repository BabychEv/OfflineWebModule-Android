package com.webmodule.offlinemodule.module;

import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.activity.FullscreenActivity;
import com.webmodule.offlinemodule.broadcast.FeedBackReceiver;
import com.webmodule.offlinemodule.broadcast.RootBroadcastReceiver;

public class OfflineWebModule extends ReactContextBaseJavaModule {
    private final FeedBackReceiver receiver;
    private Promise promise;

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
    public void startWebModule(String initialUrl, Promise promise){
        this.promise = promise;
        Intent intent = new Intent(getReactApplicationContext(), FullscreenActivity.class);
            intent.putExtra(Constants.INITIAL_URL_KEY, initialUrl);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void onBackPressed(){
        getReactApplicationContext().sendBroadcast(new Intent(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION));
    }

    public void sendFeedBack(String feedback) {
        if (promise != null) promise.resolve(feedback);
    }

    public void close() {
        getReactApplicationContext().unregisterReceiver(receiver);
    }
}
