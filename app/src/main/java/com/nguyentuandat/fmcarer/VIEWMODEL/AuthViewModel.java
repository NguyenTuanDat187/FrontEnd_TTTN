package com.nguyentuandat.fmcarer.VIEWMODEL;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nguyentuandat.fmcarer.RESPONSE.UserResponse;
import com.nguyentuandat.fmcarer.REPOSITORY.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private AuthRepository repository;
    private MutableLiveData<UserResponse> registerResult = new MutableLiveData<>();

    public AuthViewModel(Context context) {
        repository = new AuthRepository(context); // cần truyền context
    }

    public LiveData<UserResponse> getRegisterResult() {
        return registerResult;
    }

    public void registerUser(String email, String password) {
        repository.registerUser(email, password).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    registerResult.setValue(response.body());
                } else {
                    registerResult.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                registerResult.setValue(null);
            }
        });
    }
}
