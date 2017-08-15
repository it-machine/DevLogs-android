package ru.it_machine.devlogs;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Mark on 04/08/2017.
 */

interface RequestInterface {

    @Multipart
    @POST("/api/v1/logs")
    Call<Void> fetchSendLog(
            @Part("user_token") String user_token,
            @Part("app_token") String app_token,
            @Part("platform_id") Integer platform_id,
            @Part("platform_version") String platform_version,
            @Part("app_version") String app_version,
            @Part("device") String device,
            @Part("body") String body,
            @Part("comment") String comment
    );

}