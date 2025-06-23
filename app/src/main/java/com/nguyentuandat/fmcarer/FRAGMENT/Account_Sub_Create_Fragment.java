package com.nguyentuandat.fmcarer.FRAGMENT;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ApiResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Account_Sub_Create_Fragment extends Fragment {

    private EditText editFullName, editPhone, editPassword;
    private Spinner spinnerRelationship;
    private Button btnSave;

    private final String[] relationships = {"Cha", "Mẹ", "Anh", "Chị", "Ông", "Bà"};

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

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, relationships);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(adapter);

        // Bắt sự kiện nút Lưu
        btnSave.setOnClickListener(v -> handleSaveSubUser());

        return view;
    }

    private void handleSaveSubUser() {
        String fullname = editFullName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String relationship = spinnerRelationship.getSelectedItem().toString();

        // Lấy parentId từ SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String parentId = preferences.getString("userId", null);

        if (TextUtils.isEmpty(parentId)) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin tài khoản chính", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Số điện thoại và mật khẩu là bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        SubUserRequest request = new SubUserRequest(fullname, "", phone, password, parentId, relationship);

        // 👉 In log để debug nếu cần
        Log.d("SUBUSER_REQUEST", new Gson().toJson(request));

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<ApiResponse> call = apiService.createOrUpdateSubUser(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Không thể tạo tài khoản phụ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
