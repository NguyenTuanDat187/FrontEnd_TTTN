package com.nguyentuandat.fmcarer.REPOSITORY;

import android.content.Context;

import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.RESPONSE.UserResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;

import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository(Context context) {
        apiService = RetrofitClient.getInstance(context).create(ApiService.class);
    }

    public Call<UserResponse> registerUser(String email, String password) {
        return apiService.registerUser(new UserRequest(email, password));
    }
}

