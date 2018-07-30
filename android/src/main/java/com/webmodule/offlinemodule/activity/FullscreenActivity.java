package com.webmodule.offlinemodule.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.admin.AdminAuthDialog;
import com.webmodule.offlinemodule.admin.AdminMenuDialog;
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
    private long lastClickTime;
    private int superUserClicksCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        setContentView(webview);
        updateUrl = getIntent().getStringExtra(Constants.INITIAL_URL_KEY);
        addWebViewSettings();
        initReceiver();
        htmlFileHandler = new HtmlFileHandler(webview);
        initRootMenuClickListener();
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

    @SuppressLint("ClickableViewAccessibility")
    private void initRootMenuClickListener() {
        webview.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long newClick = System.currentTimeMillis();
                if ((newClick - lastClickTime) < Constants.MINIMAL_CLICK_DELAY)
                    superUserClicksCounter++;
                else superUserClicksCounter = 0;
                if (superUserClicksCounter == 4)
                    new AdminAuthDialog().show(getSupportFragmentManager(), AdminAuthDialog.class.getSimpleName());
                lastClickTime = newClick;
            }
            return false;
        });
    }

    @Override protected void onStart() {
        super.onStart();
        fillUpWebView();
    }

    private void fillUpWebView() {
        final File presentationDirectory = getExternalFilesDir(Constants.DIRECTORY_NAME_PRESENTATION);
        if (presentationDirectory != null && presentationDirectory.exists())
            htmlFileHandler.loadSavedPresentationContent();
        else
            downloadContent(Constants.URL_PRESENTATION);
    }

    private void copyInitialContent() {
        AssetsHandler.copyAssets(this, Constants.DIRECTORY_NAME);
        htmlFileHandler.loadSavedContent();
    }

    private void downloadContent(String url) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addPort() {
        final WebMessagePort[] channel = webview.createWebMessageChannel();
        port = channel[0];
        port.setWebMessageCallback(new WebMessagePort.WebMessageCallback() {
            @Override public void onMessage(WebMessagePort port, WebMessage message) {
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
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void showAdminMenu() {
        new AdminMenuDialog().show(getSupportFragmentManager(), AdminMenuDialog.class.getSimpleName());
    }

    public void loadNewContent(String newScreenId, int selectedItemPosition) {
        Intent intent = new Intent(FeedBackReceiver.PRINT_MODE_ACTION);
        intent.putExtra(FeedBackReceiver.SELECTED_PRINT_MODE, selectedItemPosition);
        sendBroadcast(intent);
        new DownloadFileFromURL(htmlFileHandler)
                .load(getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME, updateUrl, newScreenId);
    }
}
