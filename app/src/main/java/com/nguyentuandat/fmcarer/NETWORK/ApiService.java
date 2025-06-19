package com.nguyentuandat.fmcarer.NETWORK;

import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // ✅ 1. Đăng ký người dùng (cha mẹ)
    @POST("/api/users/register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    // ✅ 2. Đăng nhập
    @POST("/api/users/login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    // ✅ 3. Gửi OTP về email (nếu tách riêng API gửi mã OTP)
    @POST("/api/users/send-otp")
    Call<OtpResponse> sendOtp(@Body OtpRequest request);

    // ✅ 4. Xác minh OTP để kích hoạt tài khoản
    @POST("/api/users/verify")
    Call<UserResponse> verifyOtp(@Body OtpRequest request);

    // ✅ 5. Cập nhật thông tin người dùng: tên, số điện thoại, ảnh
    @POST("/api/users/update")
    Call<UserResponse> updateUser(@Body UserUpdateRequest request);
}
