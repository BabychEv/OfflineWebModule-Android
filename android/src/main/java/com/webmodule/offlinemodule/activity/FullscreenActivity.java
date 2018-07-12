package com.webmodule.offlinemodule.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.webmodule.offlinemodule.AssetsHandler;
import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.rest.DownloadFileFromURL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FullscreenActivity extends AppCompatActivity {
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        setContentView(webview);
        addWebViewSettings();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void addWebViewSettings() {
        WebSettings webSetting = webview.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
    }

    @Override protected void onStart() {
        super.onStart();
        fillUpWebView();
    }

    private void fillUpWebView() {
        if (new File(getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME).exists())
            loadContent();
        else
            copyInitialContent();
    }

    private void loadContent() {
        if (isNetworkAvailable())
            updateContent();
        else
            loadSavedContent();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else
            return false;
    }

    private void updateContent() {
        String updateUrl = getIntent().getStringExtra(Constants.INITIAL_URL_KEY);
        new DownloadFileFromURL(this).execute(updateUrl);
    }

    private void copyInitialContent() {
        AssetsHandler.copyAssets(this, Constants.DIRECTORY_NAME);
        loadSavedContent();
    }

    public void loadSavedContent() {
        File file = new File(getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME);
        if (file.exists())
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                fileInputStream.close();
                final String base = Constants.FILE_PREFIX + getExternalFilesDir(Constants.DIRECTORY_NAME).getAbsolutePath();
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

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
