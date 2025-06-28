package com.nguyentuandat.fmcarer.NETWORK;

import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ApiResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.CareScheludeResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ChildrenResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SingleCareScheludeResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserRequest;

import java.util.Map;

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

    // ✅ Đăng ký người dùng
    @POST("/api/users/register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    // ✅ Đăng nhập
    @POST("/api/users/login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    // ✅ Gửi và xác minh OTP
    @POST("/api/users/send-otp")
    Call<OtpResponse> sendOtp(@Body OtpRequest request);

    @POST("/api/users/verify")
    Call<UserResponse> verifyOtp(@Body OtpRequest request);

    // ✅ Cập nhật thông tin người dùng
    @POST("/api/users/update")
    Call<UserResponse> updateUser(@Body UserUpdateRequest request);

    // ✅ Upload avatar
    @Multipart
    @POST("/api/users/upload")
    Call<UserResponse> uploadImage(@Part MultipartBody.Part avatar);

    // ✅ Tài khoản phụ
    @POST("/api/users/subuser/create-or-update")
    Call<ApiResponse> createOrUpdateSubUser(@Body SubUserRequest subUser);

    // ✅ Children
    @GET("/api/children/{userId}")
    Call<ChildrenResponse> getChildrenByUser(@Path("userId") String userId);

    @POST("/api/children")
    Call<Children> addChild(@Body Children child);

    @PUT("/api/children/{childId}")
    Call<Children> updateChild(@Path("childId") String childId, @Body Children updatedChild);

    @DELETE("/api/children/{childId}")
    Call<Void> deleteChild(@Path("childId") String childId);

    // ✅ Care Schedules / Reminders

    // ➕ Tạo reminder → trả về 1 phần tử mới tạo
    @POST("/api/reminders")
    Call<SingleCareScheludeResponse> createReminder(@Body Map<String, Object> reminder);

    // 📋 Lấy toàn bộ danh sách reminder
    @GET("/api/reminders")
    Call<CareScheludeResponse> getAllReminders();

    // 🔍 Lấy reminder theo ID → trả về 1 phần tử
    @GET("/api/reminders/{id}")
    Call<SingleCareScheludeResponse> getReminderById(@Path("id") String reminderId);

    // 📝 Cập nhật reminder → trả về danh sách mới nhất (hoặc chỉ phần tử đó tuỳ backend)
    @PUT("/api/reminders/{id}")
    Call<SingleCareScheludeResponse> updateReminder(@Path("id") String reminderId, @Body Map<String, Object> updateData);

    // ❌ Xóa reminder theo ID
    @DELETE("/api/reminders/{id}")
    Call<ApiResponse> deleteReminder(@Path("id") String reminderId);
    @PUT("api/reminders/{id}/complete")
    Call<SingleCareScheludeResponse> completeReminder(@Path("id") String id);

}
