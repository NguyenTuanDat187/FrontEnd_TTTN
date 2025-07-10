// AuthInterceptor.java
package com.nguyentuandat.fmcarer.CHANGE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;
    private static final String PREF_USER_SESSION = "user_session";
    private static final String KEY_AUTH_TOKEN = "token";
    private static final String TAG = "AuthInterceptor"; // ✅ Thêm TAG cho Logcat

    public AuthInterceptor(Context context) {
        this.context = context;
        Log.d(TAG, "AuthInterceptor initialized."); // ✅ Log khi interceptor được khởi tạo
    }

    @NonNull // ✅ Đảm bảo annotation này có
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // ✅ Log URL của request đang được intercept
        Log.d(TAG, "Intercepting request URL: " + originalRequest.url());

        SharedPreferences prefs = context.getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_AUTH_TOKEN, null);

        // ✅ Log trạng thái của token được tìm thấy trong SharedPreferences
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Token found in SharedPreferences. Length: " + token.length() + ". First 10 chars: " + token.substring(0, Math.min(token.length(), 10)) + "...");
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token) // Gắn token vào header
                    .build();
            // ✅ Log header Authorization đã được thêm vào
            Log.d(TAG, "Authorization Header added: " + newRequest.header("Authorization"));
            return chain.proceed(newRequest);
        } else {
            Log.w(TAG, "No token found in SharedPreferences for this request. Proceeding without Authorization header.");
            return chain.proceed(originalRequest);
        }
    }
}
