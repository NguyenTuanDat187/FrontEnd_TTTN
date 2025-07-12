package com.nguyentuandat.fmcarer.NETWORK;

import android.content.Context;

import com.nguyentuandat.fmcarer.CHANGE.AuthInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ✅ Đảm bảo đây là IP backend của bạn (chỉ giữ 1 dòng BASE_URL)
    private static final String BASE_URL = "http://192.168.1.9:6000/";

    //private static final String BASE_URL = "http://10.0.2.2:6000/";
    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient; // Một instance duy nhất của OkHttpClient

    // 🔹 Tạo Retrofit với header Authorization nếu có token trong SharedPreferences
    // Đảm bảo method này được gọi ở những nơi cần xác thực (ví dụ: Home_Fragment, Post_ADAPTER)
    public static Retrofit getInstance(Context context) {
        // Chỉ khởi tạo OkHttpClient một lần duy nhất
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context)) // ✅ SỬ DỤNG LỚP AuthInterceptor CỦA BẠN
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }

        // Chỉ khởi tạo Retrofit một lần duy nhất
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient) // ✅ Sử dụng OkHttpClient đã có AuthInterceptor
                    .build();
        }
        return retrofit;
    }

    // 🔹 Nếu muốn gọi Retrofit KHÔNG token (ví dụ gọi public API)
    // Bạn có thể giữ lại hoặc xóa bỏ nếu không cần thiết
    public static Retrofit getInstanceWithoutAuth() {
        // Đảm bảo không sử dụng chung instance okHttpClient với getInstance(context)
        OkHttpClient clientWithoutAuth = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientWithoutAuth)
                .build();
    }

    // Constructor riêng tư để ngăn việc tạo nhiều instance
    private RetrofitClient() {
        // private constructor
    }
}