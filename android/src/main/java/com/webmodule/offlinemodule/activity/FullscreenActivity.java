package com.webmodule.offlinemodule.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
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
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.Utils;
import com.webmodule.offlinemodule.admin.AdminAuthDialog;
import com.webmodule.offlinemodule.admin.AdminMenuDialog;
import com.webmodule.offlinemodule.broadcast.FeedBackReceiver;
import com.webmodule.offlinemodule.broadcast.RootBroadcastReceiver;
import com.webmodule.offlinemodule.handler.AssetsHandler;
import com.webmodule.offlinemodule.handler.HtmlFileHandler;
import com.webmodule.offlinemodule.rest.DownloadFileFromURL;
import com.webprint.module.activity.BluetoothActivity;

import java.io.File;

public class FullscreenActivity extends AppCompatActivity implements IControlProgressBarListener, IActivityControlListener {

    private Dialog dialog;
    private WebView webview;
    private RootBroadcastReceiver receiver;
    private String updateUrl;
    private HtmlFileHandler htmlFileHandler;
    private long lastClickTime;
    private int superUserClicksCounter;
    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        setContentView(webview);
        dialog = Utils.getProgressDialog(this);
        updateUrl = getIntent().getStringExtra(Constants.INITIAL_URL_KEY);
        page = getIntent().getIntExtra(Constants.INITIAL_PAGE_NUMBER_KEY, 0);
        addWebViewSettings();
        initReceiver();
        htmlFileHandler = new HtmlFileHandler(webview, this, this);
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
                    if (page > 0) {
                        webview.loadUrl("javascript:" + "Reveal.slide(" + page + ");");
                        page = 0;
                    }
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

    @Override
    protected void onStart() {
        super.onStart();
        fillUpWebView();
    }

    private void fillUpWebView() {
        if (new File(getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME).exists()) {
            htmlFileHandler.loadDefaultSavedContent();
        } else if (new File(getExternalFilesDir(Constants.DIRECTORY_NAME) + Constants.FILE_NAME_PRESENTATION).exists()) {
            htmlFileHandler.loadSavedPresentationContent();
        } else {
            copyInitialContent();
        }
    }

    private void copyInitialContent() {
        AssetsHandler.copyAssets(this, Constants.DIRECTORY_NAME);
        htmlFileHandler.loadDefaultSavedContent();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addPort() {
        final WebMessagePort[] channel = webview.createWebMessageChannel();
        WebMessagePort port = channel[0];
        port.setWebMessageCallback(new WebMessagePort.WebMessageCallback() {
            @Override
            public void onMessage(WebMessagePort port, WebMessage message) {
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void showAdminMenu() {
        new AdminMenuDialog().show(getSupportFragmentManager(), AdminMenuDialog.class.getSimpleName());
    }

    @SuppressLint("CheckResult")
    public void loadNewContent(String newScreenId) {
        new RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        visibleProgressBar();
                        final String url = String.format(Constants.URL_PRESENTATION, newScreenId);
                        final String directoryName = getExternalFilesDir(Constants.DIRECTORY_NAME).getAbsolutePath();
                        new DownloadFileFromURL(htmlFileHandler, FullscreenActivity.this, FullscreenActivity.this)
                                .loadPresentation(Constants.FILE_NAME_PRESENTATION_ZIP, directoryName, url);
                    } else {

                    }
                }, error -> {
                });

    }

    public void changePrintMode(int selectedMode) {
        Intent sendEventIntent = new Intent(FeedBackReceiver.PRINT_MODE_ACTION);
        sendEventIntent.putExtra(FeedBackReceiver.SELECTED_PRINT_MODE, selectedMode);
        sendBroadcast(sendEventIntent);

        if (selectedMode == 1) {
            Intent startActivityIntent = new Intent(this, BluetoothActivity.class);
            startActivity(startActivityIntent);
        } else {
            //TODO: need implement call open WiFi-Direction
        }
    }

    @Override
    public void visibleProgressBar() {
        dialog.show();
    }

    @Override
    public void hideProgressBar() {
        dialog.hide();
    }

    @Override
    public void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSlidingPage(int page) {
        webview.loadUrl("javascript:" + "Reveal.slide(" + page + ");");
        this.page = page;
    }
}
