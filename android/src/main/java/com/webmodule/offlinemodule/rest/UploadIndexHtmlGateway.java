package com.webmodule.offlinemodule.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

public interface UploadIndexHtmlGateway {

    @GET
    Call<ResponseBody> getIndexContent(@Url String url, @Header("Authorization") String token);
}
