package com.nguyentuandat.fmcarer.VIEWMODEL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.REPOSITORY.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private AuthRepository repository = new AuthRepository();
    private MutableLiveData<UserResponse> registerResult = new MutableLiveData<>();

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
