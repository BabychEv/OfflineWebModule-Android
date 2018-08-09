package com.webprint.module.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrintSharedPreferences {

    private static final String PREF_KEY_ADDRESS = "PREF_KEY_ADDRESS";

    public static void saveAddress(Context context, String address) {
        putString(context, PREF_KEY_ADDRESS, address);
    }

    public static String getAddress(Context context, String defaultValue) {
        return getString(context, PREF_KEY_ADDRESS, defaultValue);
    }

    private static SharedPreferences openSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_KEY_ADDRESS, Context.MODE_PRIVATE);
    }

    private static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = openSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static String getString(Context context, String key, String defaultValue) {
        return openSharedPreferences(context).getString(key, defaultValue);
    }

}
