package com.webmodule.offlinemodule.permission;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Observable;
import rx.functions.Action1;

public class CheckExternalStoragePermissionGateway implements CheckPermissionGateway {
    private RxPermissions rxPermissions;
    private AppCompatActivity activity;

    public CheckExternalStoragePermissionGateway(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override public Observable<Boolean> check() {
        rxPermissions = new RxPermissions(activity);
        return rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .doOnNext(new Action1<Boolean>() {
                    @Override public void call(Boolean isGranted) {
                        if (!isGranted)
                            throw new RuntimeException();
                    }
                });
    }
}
