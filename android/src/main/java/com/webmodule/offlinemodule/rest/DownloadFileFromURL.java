package com.webmodule.offlinemodule.rest;

import android.os.AsyncTask;

import com.webmodule.offlinemodule.handler.HtmlFileHandler;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, String, String> {
    private HtmlFileHandler htmlFileHandler;
    private String rootPath;

    public DownloadFileFromURL(HtmlFileHandler htmlFileHandler, String rootPath) {
        this.htmlFileHandler = htmlFileHandler;
        this.rootPath = rootPath;
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
            String basicAuth = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwiaWF0IjoxNTMyMDg4NjE3fQ.lHleLyfZBAC8jdMcHerzHIWbPF4jCoI1naD1t8D80Ec";
            conection.setRequestProperty ("Authorization", basicAuth);
            conection.setUseCaches(false);
            conection.connect();
            int lenghtOfFile = conection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(rootPath);
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
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(String... progress) {
    }

    @Override
    protected void onPostExecute(String url) {
        htmlFileHandler.loadSavedContent();
    }
}
