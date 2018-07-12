package com.webmodule.offlinemodule.module;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.activity.FullscreenActivity;

public class OfflineWebModule extends ReactContextBaseJavaModule {

    public OfflineWebModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override public String getName() {
        return "OfflineWeb";
    }

    @ReactMethod
    public void startWebModule(String initialUrl){
        Intent intent = new Intent(getReactApplicationContext(), FullscreenActivity.class);
        intent.putExtra(Constants.INITIAL_URL_KEY, initialUrl);
        getReactApplicationContext().startActivity(intent);
    }
}
