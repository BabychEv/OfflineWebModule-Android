package com.webmodule.offlinemodule.handler;

import android.webkit.WebView;

import com.webmodule.offlinemodule.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HtmlFileHandler {

    public void loadSavedContent(WebView webview) {
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
                        }, e -> { }, ()-> { });
            } catch (IOException e) {
                e.printStackTrace();
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
}
