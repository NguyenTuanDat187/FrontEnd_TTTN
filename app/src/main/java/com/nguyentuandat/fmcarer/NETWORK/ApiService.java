package com.nguyentuandat.fmcarer.NETWORK;

import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.MODEL.Post;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ApiResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.CareScheludeResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ChildrenResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ImageUploadResponse; // Sẽ cần cập nhật
import com.nguyentuandat.fmcarer.MODEL_CALL_API.MultiImageUploadResponse; // Thêm mới cho nhiều ảnh
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PostRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PostResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SingleCareScheludeResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    Call<CareScheludeResponse> getAllReminders(@retrofit2.http.Query("user_id") String userId);


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

    // ✅ Post APIs

    @POST("/api/posts")
    Call<PostResponse> createPost(@Body PostRequest postRequest);

    // ✅ Lấy tất cả bài viết
    @GET("/api/posts")
    Call<List<Post>> getAllPosts();

    // ✅ Lấy danh sách bài viết theo userId (lọc theo user)
    @GET("/api/posts")
    Call<List<Post>> getPostsByUserId(@Query("userId") String userId);

    // ✅ Cập nhật bài viết
    @PUT("/api/posts/{postId}")
    Call<Post> updatePost(@Path("postId") String postId, @Body Post updatedPost);

    // ✅ Xóa bài viết
    @DELETE("/api/posts/{postId}")
    Call<ApiResponse> deletePost(@Path("postId") String postId, @Query("user_id") String userId);



    // MARK: - API UPLOAD ẢNH

    // ✅ Upload một ảnh
    // Sử dụng @Multipart để chỉ định đây là request dạng multipart/form-data
    // @Part MultipartBody.Part "image" phải khớp với tên trường 'image' ở backend (upload.single('image'))
    @Multipart
    @POST("/api/upload")
    Call<ImageUploadResponse> uploadSingleImage(@Part MultipartBody.Part image);

    // ✅ Upload nhiều ảnh cùng lúc
    // Sử dụng List<MultipartBody.Part> để gửi nhiều file.
    // Tên trường "images" phải khớp với tên trường 'images' ở backend (upload.array('images', ...))
    @Multipart
    @POST("/api/upload-multiple")
    Call<MultiImageUploadResponse> uploadMultipleImages(@Part List<MultipartBody.Part> images);

    Call<UserResponse> uploadImage(MultipartBody.Part avatarPart);

    // Bạn cũng có thể thêm các trường dữ liệu khác cùng với file nếu cần:
    // Call<MultiImageUploadResponse> uploadMultipleImages(
    //     @Part List<MultipartBody.Part> images,
    //     @Part("description") RequestBody description
    // );
}