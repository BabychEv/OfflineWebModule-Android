package com.saredpreferences.module;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class SharedPreferencesModule extends ReactContextBaseJavaModule {
    private static final String LOCAL_SETTINGS = "com.saredpreferences.module.local_settings";
    private static final String PREFERENCES_SAVE_EVENT = "com.saredpreferences.module.EVENT.SAVE";
    private static final String PREFERENCES_GET_EVENT = "com.saredpreferences.module.EVENT.GET";
    private static final String PREFERENCES_RESET_EVENT = "com.saredpreferences.module.EVENT.RESET";
    private static final String PREFERENCES_RESULT_KEY = "shared_preferences_result";
    private static final String PREFERENCES_REQUEST_KEY = "shared_preferences_request";
    private static final String PREFERENCES_RESULT_ERROR = "Error";
    private static final String PREFERENCES_RESULT_OK = "Done";

    public SharedPreferencesModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override public String getName() {
        return getClass().getSimpleName();
    }

    @ReactMethod
    public void savePreferences(String key, String value) {
        try {
            getPreferences().edit().putString(key, value).apply();
            sendResult(PREFERENCES_SAVE_EVENT, PREFERENCES_RESULT_OK, key);
        } catch (Exception e) {
            e.printStackTrace();
            sendResult(PREFERENCES_SAVE_EVENT, PREFERENCES_RESULT_ERROR, key);
        }
    }

    @ReactMethod
    public void getPreferences(String key) {
        try {
            sendResult(PREFERENCES_GET_EVENT, getPreferences().getString(key, ""), key);
        } catch (Exception e) {
            e.printStackTrace();
            sendResult(PREFERENCES_GET_EVENT, PREFERENCES_RESULT_ERROR, key);
        }
    }

    @ReactMethod
    public void resetPreferences(String key) {
        try {
            getPreferences().edit().putString(key, "").apply();
            sendResult(PREFERENCES_RESET_EVENT, PREFERENCES_RESULT_OK, key);
        } catch (Exception e) {
            e.printStackTrace();
            sendResult(PREFERENCES_RESET_EVENT, PREFERENCES_RESULT_ERROR, key);
        }
    }

    private SharedPreferences getPreferences() {
        return getReactApplicationContext().getSharedPreferences(LOCAL_SETTINGS, Context.MODE_PRIVATE);
    }

    private void sendResult(String event, String result, String key) {
        WritableMap params = Arguments.createMap();
        params.putString(PREFERENCES_RESULT_KEY, result);
        params.putString(PREFERENCES_REQUEST_KEY, key);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }
}
