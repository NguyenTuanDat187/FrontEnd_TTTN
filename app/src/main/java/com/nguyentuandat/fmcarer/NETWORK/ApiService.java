package com.nguyentuandat.fmcarer.NETWORK;

import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ApiResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ChildrenResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

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
    @Multipart
    @POST("/api/users/upload")
    Call<UserResponse> uploadImage(@Part MultipartBody.Part avatar);
    @POST("/api/users/subuser/create-or-update")
    Call<ApiResponse> createOrUpdateSubUser(@Body SubUserRequest subUser);
    // call api danh sách trẻ
    // 🔍 [GET] Lấy danh sách trẻ theo userId
    // Gửi userId lên để lấy danh sách các trẻ thuộc tài khoản đó
    @GET("/api/children/{userId}")
    Call<ChildrenResponse> getChildrenByUser(@Path("userId") String userId);


    // ➕ [POST] Thêm trẻ mới
    // Gửi object Children dạng JSON lên để thêm mới vào hệ thống
    @POST("/api/children")
    Call<Children> addChild(@Body Children child);

    // 📝 [PUT] Cập nhật thông tin của một trẻ cụ thể
    // Truyền childId trong URL và object Children mới để cập nhật
    @PUT("/api/children/{childId}")
    Call<Children> updateChild(@Path("childId") String childId, @Body Children updatedChild);

    // ❌ [DELETE] Xóa một trẻ theo childId
    // Truyền childId cần xóa
    @DELETE("/api/children/{childId}")
    Call<Void> deleteChild(@Path("childId") String childId);

}
