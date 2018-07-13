package com.webmodule.offlinemodule.rest;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.handler.HtmlFileHandler;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, String, String> {
    private HtmlFileHandler htmlFileHandler;
    private WebView webview;

    public DownloadFileFromURL(HtmlFileHandler htmlFileHandler, WebView webview) {
        this.htmlFileHandler = htmlFileHandler;
        this.webview = webview;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();
            int lenghtOfFile = conection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);
            OutputStream output = new FileOutputStream(webview.getContext().getExternalFilesDir(Constants.DIRECTORY_NAME)
                    + Constants.FILE_NAME);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        return null;
    }

    protected void onProgressUpdate(String... progress) {
    }

    @Override
    protected void onPostExecute(String url) {
        htmlFileHandler.loadSavedContent(webview);
    }
}
