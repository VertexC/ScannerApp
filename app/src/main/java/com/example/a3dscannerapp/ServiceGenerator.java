package com.example.a3dscannerapp;

import android.text.TextUtils;

import com.wealdtech.hawk.HawkCredentials;

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private Retrofit mRetrofit;

    private Retrofit.Builder mBuilder;

    private OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder();

    private OkHttpClient.Builder longTimeOutHttpClient = new OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS);

    public <S> S createService(
            Class<S> serviceClass) {
        return mRetrofit.create(serviceClass);
    }

    public void setup(String baseUrl){

        mBuilder =
            new Retrofit.Builder()
                .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create()).client(longTimeOutHttpClient.build());

        mRetrofit = mBuilder.build();
    }
}
