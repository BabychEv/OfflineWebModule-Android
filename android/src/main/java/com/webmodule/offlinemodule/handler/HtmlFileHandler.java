package com.webmodule.offlinemodule.handler;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.webmodule.offlinemodule.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HtmlFileHandler {
    private WebView webview;

    public HtmlFileHandler(WebView webview) {

        this.webview = webview;
    }

    public void loadSavedContent() {
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

    public void loadSavedPresentationContent(String pathDirectory) {
        File presentationDir = new File(pathDirectory);
        if (presentationDir.exists() && presentationDir.isDirectory()) {
            File[] files = presentationDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                final String fileName = files[i].getName();
                if (!TextUtils.isEmpty(fileName) && fileName.equals(Constants.FILE_NAME_PRESENTATION)) {
                    try {
                        File file = new File(pathDirectory + "/" + fileName);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[fileInputStream.available()];
                        fileInputStream.read(buffer);
                        fileInputStream.close();
                        final String base = Constants.FILE_PREFIX + pathDirectory;
                        changePresentationData(new String(buffer), base, "iminages", Constants.IMAGE_PREFIX)
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
                    break;
                }
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

    private Observable<String> changePresentationData(final String data, final String basePath, final String replacedPath, final String newPath) {
        return Observable.just(data)
                .map(s -> s.replace(replacedPath, basePath + "/" + newPath));
    }
}
