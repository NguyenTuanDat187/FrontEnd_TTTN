package com.nguyentuandat.fmcarer.NETWORK;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.1.9:6000/"; // 🔁 Thay IP theo server của bạn
   // private static final String BASE_URL = "http://10.0.2.2:6000/"; // chạy trên máy giả lập Android


    // 🔹 Tạo Retrofit với header Authorization nếu có token trong SharedPreferences
    public static Retrofit getInstance(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    SharedPreferences prefs = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
                    String token = prefs.getString("token", "");

                    Request.Builder requestBuilder = original.newBuilder();
                    if (!token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    Request requestWithHeaders = requestBuilder.build();
                    return chain.proceed(requestWithHeaders);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    // 🔹 Nếu muốn gọi Retrofit KHÔNG token (ví dụ gọi public API)
    public static Retrofit getInstanceWithoutAuth() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}
