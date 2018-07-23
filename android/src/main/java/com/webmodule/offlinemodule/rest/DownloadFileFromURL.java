package com.webmodule.offlinemodule.rest;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.handler.HtmlFileHandler;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DownloadFileFromURL {
    private HtmlFileHandler htmlFileHandler;

    public DownloadFileFromURL(HtmlFileHandler htmlFileHandler) {
        this.htmlFileHandler = htmlFileHandler;
    }

    public void load(String path, String updateUrl, String screenId) {
        Observable.just(screenId)
                .observeOn(Schedulers.io())
                .concatMap(s -> loadNewContent(updateUrl, screenId))
                .concatMap(responseBody -> saveNewContent(path, responseBody))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isOk -> htmlFileHandler.loadSavedContent(),
                        e -> {},
                        () -> {});
    }

    private Observable<ResponseBody> loadNewContent(String updateUrl, String screenId) {
        return Observable.create(subscriber -> {
            String[] urlParts = updateUrl.split(Constants.URL_REGEX);
            String url = updateUrl.replace(Constants.SCREEN_ID_KEY, screenId);
            try {
                ResponseBody body = new Retrofit.Builder()
                        .baseUrl(urlParts[0] + Constants.URL_REGEX + Constants.URL_REGEX
                                + urlParts[1] + Constants.URL_REGEX
                                + urlParts[2])
                        .client(new OkHttpClient.Builder().build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(UploadIndexHtmlGateway.class)
                        .getIndexContent(url, Constants.DEV_TOKEN)
                        .execute()
                        .body();
                subscriber.onNext(body);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    private Observable<Boolean> saveNewContent(String path, ResponseBody responseBody) {
        return Observable.create(subscriber -> {
            int count;
            try {
                InputStream input = responseBody.byteStream();
                OutputStream output = new FileOutputStream(path);
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                subscriber.onNext(true);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
