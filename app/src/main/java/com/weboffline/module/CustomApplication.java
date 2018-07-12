package com.weboffline.module;

import android.app.Application;

import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.webmodule.offlinemodule.module.OfflineWebPackage;

import java.util.Arrays;
import java.util.List;

public class CustomApplication extends Application {

    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
                new MainReactPackage(),
                new OfflineWebPackage()); // <-- Add this line with your package name.
    }
}
