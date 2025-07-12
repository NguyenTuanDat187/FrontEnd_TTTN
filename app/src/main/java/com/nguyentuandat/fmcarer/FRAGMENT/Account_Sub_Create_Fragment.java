package com.nguyentuandat.fmcarer.FRAGMENT;

import android.content.Context; // Import Context
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.nguyentuandat.fmcarer.RESPONSE.ApiResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
// import com.nguyentuandat.fmcarer.VIEW.Login_Activity; // Không còn cần thiết

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Account_Sub_Create_Fragment extends Fragment {

    private EditText editFullName, editPhone, editPassword;
    private Spinner spinnerRelationship;
    private Button btnSave;

    private final String[] relationships = {"Cha", "Mẹ", "Anh", "Chị", "Ông", "Bà"};

    // Đã loại bỏ: private Login_Activity loginActivity; // Không còn cần thiết

    private SharedPreferences userSessionPrefs; // Khai báo SharedPreferences cho phiên người dùng

    // Định nghĩa các khóa cho SharedPreferences (phải khớp với các khóa của Login_Activity)
    private static final String PREF_USER_SESSION = "user_session";
    private static final String KEY_AUTH_TOKEN = "token";
    private static final String KEY_USER_ID = "_id";
    private static final String TAG = "AccountSubCreateFragment"; // Tag cho Logcat

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_sub_create_fragment, container, false);

        // Ánh xạ view
        editFullName = view.findViewById(R.id.editFullName);
        editPhone = view.findViewById(R.id.editSubPhone);
        editPassword = view.findViewById(R.id.editSubPassword);
        spinnerRelationship = view.findViewById(R.id.spinnerRelationship);
        btnSave = view.findViewById(R.id.btnSaveSubAccount);

        // Cài đặt Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, relationships);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(adapter);

        // Khởi tạo SharedPreferences phiên người dùng
        userSessionPrefs = requireContext().getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);

        // Đã loại bỏ: loginActivity = new Login_Activity(); // Không còn cần thiết

        // Bắt sự kiện nút Lưu
        btnSave.setOnClickListener(v -> handleSaveSubUser());

        return view;
    }

    private void handleSaveSubUser() {
        String fullname = editFullName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String relationship = spinnerRelationship.getSelectedItem().toString();

        // Lấy parentId và token từ SharedPreferences
        String parentId = userSessionPrefs.getString(KEY_USER_ID, null);
        String authToken = userSessionPrefs.getString(KEY_AUTH_TOKEN, null);

        if (TextUtils.isEmpty(parentId)) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin tài khoản chính (Parent ID). Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(getContext(), "Không tìm thấy token xác thực. Vui lòng đăng nhập lại tài khoản chính.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Số điện thoại và mật khẩu là bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        SubUserRequest request = new SubUserRequest(fullname, "", phone, password, parentId, relationship);

        // 👉 In log để debug nếu cần
        Log.d(TAG, "Request Body: " + new Gson().toJson(request));
        Log.d(TAG, "Parent ID: " + parentId);
        Log.d(TAG, "Auth Token (first 10 chars): " + (authToken != null ? authToken.substring(0, Math.min(authToken.length(), 10)) : "null"));
        // Thêm log cho số điện thoại và mật khẩu
        Log.d(TAG, "Phone: " + phone);
        Log.d(TAG, "Password: " + password);


        // Sử dụng getAuthenticatedInstance vì đây là một cuộc gọi cần xác thực
        ApiService apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);

        // Thêm "Bearer " vào trước token
        String bearerToken = "Bearer " + authToken;

        // Gọi API với token xác thực
        Call<ApiResponse> call = apiService.createOrUpdateSubUser(bearerToken, request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    // Có thể reset form hoặc điều hướng sau khi tạo thành công
                    editFullName.setText("");
                    editPhone.setText("");
                    editPassword.setText("");
                    spinnerRelationship.setSelection(0); // Reset spinner
                } else {
                    String errorMessage = "Không thể tạo tài khoản phụ";
                    if (response.errorBody() != null) {
                        try {
                            // Cố gắng đọc lỗi từ errorBody
                            ApiResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), ApiResponse.class);
                            if (errorResponse != null && !TextUtils.isEmpty(errorResponse.getMessage())) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Response not successful: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
            }
        });
    }
}
