package com.webmodule.offlinemodule.rest.settings;

import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.broadcast.FeedBackReceiver;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DownloadFromURLClient {
    private static final String URL_REGEX = "/";
    private ReactApplicationContext context;

    public DownloadFromURLClient(ReactApplicationContext reactContext) {
        context = reactContext;
    }

    public void load(String updateUrl, String token, String preferencesKey) {
        Observable.just(token)
                .observeOn(Schedulers.io())
                .concatMap(s -> loadNewContent(updateUrl, s))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                            try {
                                context.getSharedPreferences(Constants.LOCAL_SETTINGS, Context.MODE_PRIVATE)
                                        .edit().putString(preferencesKey, response.string()).apply();
                                sendResult(Constants.RESULT_SUCCESS);
                            } catch (IOException e) {
                                e.printStackTrace();
                                sendResult(Constants.RESULT_SAVING_ERROR);
                            }
                        },
                        e -> sendResult(Constants.RESULT_LOADING_ERROR),
                        () -> {});
    }

    private void sendResult(String result) {
        if (context != null) {
            Intent intent = new Intent(FeedBackReceiver.DOWNLOAD_FROM_RESULT);
            intent.putExtra(FeedBackReceiver.DOWNLOAD_FROM_RESULT, result);
            context.sendBroadcast(intent);
        }
    }

    private Observable<ResponseBody> loadNewContent(String updateUrl, String token) {
        return Observable.create(subscriber -> {
            String[] urlParts = updateUrl.split(URL_REGEX);
            try {
                ResponseBody body = new Retrofit.Builder()
                        .baseUrl(urlParts[0] + URL_REGEX + URL_REGEX
                                + urlParts[1] + URL_REGEX
                                + urlParts[2])
                        .client(new OkHttpClient.Builder().build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(UpdateSettingsGateway.class)
                        .getContent(updateUrl, token)
                        .execute()
                        .body();
                subscriber.onNext(body);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
