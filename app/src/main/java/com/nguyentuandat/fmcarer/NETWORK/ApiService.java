package com.nguyentuandat.fmcarer.NETWORK;

import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.MODEL.Post;
import com.nguyentuandat.fmcarer.MODEL.Payment; // Import Payment model
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PasswordVerificationRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserLoginRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.TopUpInitiateRequest; // Import TopUpInitiateRequest
import com.nguyentuandat.fmcarer.RESPONSE.ApiResponse;
import com.nguyentuandat.fmcarer.RESPONSE.CareScheludeResponse;
import com.nguyentuandat.fmcarer.RESPONSE.ChildrenResponse;
import com.nguyentuandat.fmcarer.RESPONSE.ImageUploadResponse;
import com.nguyentuandat.fmcarer.RESPONSE.MultiImageUploadResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.RESPONSE.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PostRequest;
import com.nguyentuandat.fmcarer.RESPONSE.PostResponse;
import com.nguyentuandat.fmcarer.RESPONSE.PaymentResponse; // Import PaymentResponse
import com.nguyentuandat.fmcarer.RESPONSE.PaymentHistoryResponse; // Import PaymentHistoryResponse
import com.nguyentuandat.fmcarer.RESPONSE.SingleCareScheludeResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.RESPONSE.UserListResponse;
import com.nguyentuandat.fmcarer.RESPONSE.UserResponse;
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
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 🔒 USER AUTHENTICATION
    @GET("/api/users/users")
    Call<UserListResponse> getAllUsers();

    @POST("/api/users/register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    @POST("/api/users/login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    @POST("/api/users/send-otp")
    Call<OtpResponse> sendOtp(@Body OtpRequest request);

    @POST("/api/users/update")
    Call<UserResponse> updateUser(@Body UserUpdateRequest request);

    // Endpoint mới để xác thực mật khẩu người dùng chính
    @POST("/api/users/verify-password")
    Call<ApiResponse> verifyUserPassword(
            @Header("Authorization") String authToken,
            @Body PasswordVerificationRequest request
    );

    @Multipart
    @POST("/api/users/upload-avatar")
    Call<UserResponse> uploadImage(
            @Part("userId") RequestBody userId,
            @Part MultipartBody.Part avatar
    );

    @POST("/api/users/subuser/create-or-update")
    Call<ApiResponse> createOrUpdateSubUser(
            @Header("Authorization") String bearerToken,
            @Body SubUserRequest subUser
    );

    @PUT("/api/users/subuser/{subuserId}")
    Call<ApiResponse> updateSubUser(
            @Header("Authorization") String bearerToken,
            @Path("subuserId") String subuserId,
            @Body SubUserRequest subUser
    );

    @GET("/api/users/subusers/parent/{parentId}")
    Call<UserListResponse> getAllSubusersByParentId(
            @Header("Authorization") String bearerToken,
            @Path("parentId") String parentId
    );

    @GET("/api/users/subuser/{subuserId}")
    Call<UserResponse> getSubuserById(
            @Header("Authorization") String bearerToken,
            @Path("subuserId") String subuserId
    );

    @DELETE("/api/users/subuser/{subuserId}")
    Call<ApiResponse> deleteSubuser(
            @Header("Authorization") String bearerToken,
            @Path("subuserId") String subuserId
    );

    @POST("/api/users/login-subuser")
    Call<UserResponse> loginSubUser(@Body SubUserLoginRequest request);

    // ✅ CHILDREN MANAGEMENT
    @GET("/api/children/my")
    Call<ChildrenResponse> getChildrenByUser(@Header("Authorization") String bearerToken);

    @GET("/api/children/{childId}")
    Call<Children> getChildById(@Header("Authorization") String bearerToken, @Path("childId") String childId);

    @POST("/api/children")
    Call<Children> addChild(@Header("Authorization") String bearerToken, @Body Children child);

    @PUT("/api/children/{childId}")
    Call<Children> updateChild(@Header("Authorization") String bearerToken, @Path("childId") String childId, @Body Children updatedChild);

    @DELETE("/api/children/{childId}")
    Call<Void> deleteChild(@Header("Authorization") String bearerToken, @Path("childId") String childId);

    // ✅ CARE SCHEDULES / REMINDERS
    @POST("/api/reminders")
    Call<SingleCareScheludeResponse> createReminder(
            @Header("Authorization") String token,
            @Body Map<String, Object> reminderData
    );

    @GET("/api/reminders")
    Call<CareScheludeResponse> getAllReminders(
            @Header("Authorization") String token
    );

    @GET("/api/reminders/{id}")
    Call<SingleCareScheludeResponse> getReminderById(
            @Header("Authorization") String token,
            @Path("id") String reminderId
    );

    @PUT("/api/reminders/{id}")
    Call<SingleCareScheludeResponse> updateReminder(
            @Header("Authorization") String token,
            @Path("id") String reminderId,
            @Body Map<String, Object> updateData
    );

    @DELETE("/api/reminders/{id}")
    Call<ApiResponse> deleteReminder(
            @Header("Authorization") String token,
            @Path("id") String reminderId
    );

    @PUT("/api/reminders/{id}/complete")
    Call<SingleCareScheludeResponse> completeReminder(
            @Header("Authorization") String token,
            @Path("id") String reminderId
    );

    @GET("/api/reminders/by-child/{childId}")
    Call<CareScheludeResponse> getRemindersByChild(
            @Header("Authorization") String token,
            @Path("childId") String childId
    );

    // ✅ POSTS
    @POST("/api/posts")
    Call<PostResponse> createPost(@Body PostRequest postRequest);

    @GET("/api/posts")
    Call<List<Post>> getAllPosts();

    @GET("/api/posts")
    Call<List<Post>> getPostsByUserId(@Query("userId") String userId);

    @PUT("/api/posts/{postId}")
    Call<Post> updatePost(@Path("postId") String postId, @Body Post updatedPost);

    @DELETE("/api/posts/{postId}")
    Call<ApiResponse> deletePost(@Path("postId") String postId);

    // ✅ IMAGE UPLOAD
    @Multipart
    @POST("/api/upload")
    Call<ImageUploadResponse> uploadSingleImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("/api/upload-multiple")
    Call<MultiImageUploadResponse> uploadMultipleImages(@Part List<MultipartBody.Part> images);

    // ✅ PAYMENT / TOP-UP ENDPOINTS

    /**
     * @desc Khởi tạo yêu cầu nạp tiền mới qua Momo.
     * @route POST /api/payments/topup/initiate
     * @access Private (cần xác thực người dùng)
     * @param authToken Token xác thực người dùng (Bearer token).
     * @param request Chứa amount và payment_method.
     * @return Call<PaymentResponse> Chứa payUrl để chuyển hướng người dùng.
     */
    @POST("/api/payments/topup/initiate")
    Call<PaymentResponse> initiateTopUp(
            @Header("Authorization") String authToken,
            @Body TopUpInitiateRequest request
    );

    /**
     * @desc Lấy lịch sử các giao dịch nạp tiền Momo của người dùng.
     * @route GET /api/payments/topup/history
     * @access Private (cần xác thực người dùng)
     * @param authToken Token xác thực người dùng (Bearer token).
     * @param limit Số lượng bản ghi trên mỗi trang (mặc định 10).
     * @param skip Số lượng bản ghi bỏ qua (offset).
     * @return Call<PaymentHistoryResponse> Chứa danh sách các giao dịch và thông tin phân trang.
     */
    @GET("/api/payments/topup/history")
    Call<PaymentHistoryResponse> getTopUpHistory(
            @Header("Authorization") String authToken,
            @Query("limit") Integer limit,
            @Query("skip") Integer skip
    );

    /**
     * @desc Lấy chi tiết của một giao dịch nạp tiền Momo cụ thể.
     * @route GET /api/payments/topup/{id}
     * @access Private (cần xác thực người dùng)
     * @param authToken Token xác thực người dùng (Bearer token).
     * @param paymentId ID của giao dịch cần lấy chi tiết.
     * @return Call<Payment> Chứa thông tin chi tiết của giao dịch.
     */
    @GET("/api/payments/topup/{id}")
    Call<Payment> getPaymentById(
            @Header("Authorization") String authToken,
            @Path("id") String paymentId
    );

}