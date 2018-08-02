package com.webmodule.offlinemodule.handler;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.activity.IActivityControlListener;
import com.webmodule.offlinemodule.activity.IControlProgressBarListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HtmlFileHandler {
    private WebView webview;
    private IControlProgressBarListener uiControlListener;
    private IActivityControlListener controlListener;

    public HtmlFileHandler(WebView webview, IControlProgressBarListener listener, IActivityControlListener controlListener) {
        uiControlListener = listener;
        this.webview = webview;
        this.controlListener = controlListener;
    }

    public void loadDefaultSavedContent() {
        File file = new File(webview.getContext().getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME);
        if (file.exists())
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                fileInputStream.close();
                final String base = Constants.FILE_PREFIX + webview.getContext().getExternalFilesDir(Constants.DIRECTORY_NAME).getAbsolutePath();
                changeData(new String(buffer), base, Constants.IMAGE_PREFIX)
                        .concatMap(str -> changeCSSData(str, base, Constants.CSS_PREFIX))
                        .concatMap(str1 -> changeData(str1, base, Constants.LIB_JS_PREFIX))
                        .concatMap(str3 -> changeJSData(str3, base, Constants.JS_PREFIX))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fullData -> {
                            webview.loadDataWithBaseURL(base, fullData, Constants.MIME, Constants.ENCODING, null);
                        }, e -> {
                        }, () -> {
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public Context getContext() {
        return webview.getContext();
    }

    public void loadSavedPresentationContent() {
        File presentation = new File(webview.getContext().getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME_PRESENTATION);
        if (presentation.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(presentation);
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                fileInputStream.close();
                final String base = Constants.FILE_PREFIX + webview.getContext().getExternalFilesDir(Constants.DIRECTORY_NAME).getAbsolutePath();
                changeData(new String(buffer), base, Constants.IMAGE_PREFIX)
                        .concatMap(str -> changeCSSData(str, base, Constants.CSS_PREFIX))
                        .concatMap(str1 -> changeData(str1, base, Constants.LIB_JS_PREFIX))
                        .concatMap(str3 -> changeJSData(str3, base, Constants.JS_PREFIX))
                        .concatMap(str4 -> changeJSData2(str4, base, Constants.JS_PREFIX_2))
                        .concatMap(str5 -> changeJSData3(str5, base, Constants.SRC_2))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fullData -> {
                            webview.loadDataWithBaseURL(base, fullData, Constants.MIME, Constants.ENCODING, null);
                            uiControlListener.hideProgressBar();
                            controlListener.setSlidingPage(0);
                        }, e -> {
                        }, () -> {
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Observable<String> changeCSSData(final String data, final String basePath, final String replacedPath) {
        return Observable.just(data)
                .map(s -> s.replace(replacedPath, Constants.HREF + basePath + "/" + Constants.CSS));
    }

    private Observable<String> changeJSData(final String data, final String basePath, final String replacedPath) {
        return Observable.just(data)
                .map(s -> s.replace(replacedPath, Constants.SRC + basePath + "/" + Constants.JS));
    }

    private Observable<String> changeData(final String data, final String basePath, final String replacedPath) {
        return Observable.just(data)
                .map(s -> s.replace(replacedPath, basePath + "/" + replacedPath));
    }

    private Observable<String> changeJSData2(final String data, final String basePath, final String replacedPath) {
        return Observable.just(data)
                .map(s -> s.replace(replacedPath, Constants.SRC_2 + basePath + "/" + Constants.JS));
    }

    private Observable<String> changeJSData3(final String data, final String basePath, final String replacedPath) {
        return Observable.just(data)
                .map(s -> s.replace(replacedPath, Constants.SRC_2 + basePath + "/"));
    }
}
