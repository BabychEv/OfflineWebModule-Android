package com.webmodule.offlinemodule;

import android.content.Context;
import android.content.Intent;

public class OfflineModuleBilder {

    public static void startWebModule(Context context, String initialUrl){
        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra(Constants.INITIAL_URL_KEY, initialUrl);
        context.startActivity(intent);
    }
}
