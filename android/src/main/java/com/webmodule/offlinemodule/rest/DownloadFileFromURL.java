package com.webmodule.offlinemodule.rest;

import android.content.Context;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.handler.HtmlFileHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
                .concatMap(responseBody -> saveNewContent(path, path, responseBody))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isOk -> htmlFileHandler.loadSavedContent(),
                        e -> {
                        },
                        () -> {
                        });
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

    private Observable<Boolean> saveNewContent(String pathToDirectory, String zipFileName, ResponseBody responseBody) {
        return Observable.create(subscriber -> {
            int count;
            try {
                deleteFile(htmlFileHandler.getContext(), pathToDirectory);
                File dir = new File(pathToDirectory);
                dir.mkdirs();
                InputStream input = responseBody.byteStream();
                File file = new File(pathToDirectory, zipFileName);
                file.createNewFile();
                OutputStream output = new FileOutputStream(file);
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

    private void deleteFile(Context context, String path) throws IOException {
        File file = new File(path);
        file.delete();
        if (file.exists()) {
            file.getCanonicalFile().delete();
            if (file.exists()) {
                context.deleteFile(file.getName());
            }
        }
    }

    public void loadPresentation(String zipFileName, String pathToDirectoryUnZip, String url) {
        Observable.just(true)
                .observeOn(Schedulers.io())
                .concatMap(s -> loadNewContent(url))
                .concatMap(responseBody -> saveNewContent(pathToDirectoryUnZip, zipFileName, responseBody))
                .concatMap(responseBody -> unzip(zipFileName, pathToDirectoryUnZip))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isOk -> htmlFileHandler.loadSavedPresentationContent(pathToDirectoryUnZip),
                        e -> {
                        },
                        () -> {
                        });
    }

    private Observable<ResponseBody> loadNewContent(final String url) {
        return Observable.create(subscriber -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();
                ResponseBody body = new Retrofit.Builder()
                        .baseUrl(url)
                        .client(okHttpClient)
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

    private Observable<Boolean> unzip(final String zipFileName, final String directoryUnzip) {
        return Observable.create(subscriber -> {
            boolean isOk = true;
            File zipFile = new File(directoryUnzip, zipFileName);
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(
                        new BufferedInputStream(new FileInputStream(zipFile)));
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(directoryUnzip, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } finally {
                        fout.close();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                isOk = false;
                subscriber.onError(e);
            } catch (IOException e) {
                e.printStackTrace();
                isOk = false;
                subscriber.onError(e);
            } finally {
                if (zis != null) {
                    try {
                        zis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (isOk) {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }
            }
        });
    }
}
