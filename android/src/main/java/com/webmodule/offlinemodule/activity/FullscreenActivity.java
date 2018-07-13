package com.webmodule.offlinemodule.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.broadcast.FeedBackReceiver;
import com.webmodule.offlinemodule.broadcast.RootBroadcastReceiver;
import com.webmodule.offlinemodule.handler.AssetsHandler;
import com.webmodule.offlinemodule.handler.HtmlFileHandler;
import com.webmodule.offlinemodule.rest.DownloadFileFromURL;

import java.io.File;

public class FullscreenActivity extends AppCompatActivity {
    private WebView webview;
    private RootBroadcastReceiver receiver;
    private String updateUrl;
    private HtmlFileHandler htmlFileHandler;
    private WebMessagePort port;
    private DownloadFileFromURL htmlLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        setContentView(webview);
        updateUrl = getIntent().getStringExtra(Constants.INITIAL_URL_KEY);
        addWebViewSettings();
        initReceiver();
        htmlFileHandler = new HtmlFileHandler();
        htmlLoader = new DownloadFileFromURL(htmlFileHandler, webview);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void addWebViewSettings() {
        WebSettings webSetting = webview.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    addPort();
                }
            });
        }
    }

    private void initReceiver() {
        receiver = new RootBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION);
        registerReceiver(receiver, filter);
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
            htmlLoader.execute(updateUrl);
        else
            htmlFileHandler.loadSavedContent(webview);
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

    private void copyInitialContent() {
        AssetsHandler.copyAssets(this, Constants.DIRECTORY_NAME);
        htmlFileHandler.loadSavedContent(webview);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addPort() {
        final WebMessagePort[] channel = webview.createWebMessageChannel();
        port = channel[0];
        port.setWebMessageCallback(new WebMessagePort.WebMessageCallback(){
            @Override public void onMessage(WebMessagePort port, WebMessage message) {
                Log.d("promise", "send result - " + message.getData());
                Intent intent = new Intent(FeedBackReceiver.FEEDBACK_ACTION);
                intent.putExtra(FeedBackReceiver.FEEDBACK_ACTION, message.getData());
                sendBroadcast(intent);
            }
        });
        webview.postWebMessage(new WebMessage("", new WebMessagePort[]{channel[1]}), Uri.EMPTY);
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack())
            webview.goBack();
        else
            super.onBackPressed();
    }

    @Override protected void onDestroy() {
        sendBroadcast(new Intent(FeedBackReceiver.CLOSE_ACTION));
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
