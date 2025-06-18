package com.nguyentuandat.fmcarer.NETWORK;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    @POST("/api/auth/login")
    Call<UserResponse> loginUser(@Body UserRequest request);
    @POST("api/otp/send-otp")
    Call<OtpResponse> sendOtp(@Body OtpRequest request);
}
