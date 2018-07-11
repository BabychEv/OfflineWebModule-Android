package com.webmodule.offlinemodule.permission;

import rx.Observable;

public interface CheckPermissionGateway {
    Observable<Boolean> check();
}
