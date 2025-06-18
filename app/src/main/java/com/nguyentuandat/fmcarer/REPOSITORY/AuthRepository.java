package com.nguyentuandat.fmcarer.REPOSITORY;

import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;

import retrofit2.Call;

public class AuthRepository {
    private ApiService apiService;

    public AuthRepository() {
        apiService = RetrofitClient.getInstance().create(ApiService.class);
    }

    public Call<UserResponse> registerUser(String email, String password) {
        return apiService.registerUser(new UserRequest(email, password));
    }
}
