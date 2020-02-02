package com.example.a3dscannerapp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Url;

interface ApiConfig {
    @Multipart
    @POST("uploadVideo")
    Call<ResponseBody> uploadVideo(
            @Part MultipartBody.Part file
    );


    @PUT("multiscan/upload")
    Call<ResponseBody> upload(
            @Header("FILE_NAME") String filename,
            @Body RequestBody body
    );

    @PUT
    Call<ResponseBody> uploadFullUrl(
            @Url String url,
            @Header("FILE_NAME") String filename,
            @Body RequestBody body
    );
}