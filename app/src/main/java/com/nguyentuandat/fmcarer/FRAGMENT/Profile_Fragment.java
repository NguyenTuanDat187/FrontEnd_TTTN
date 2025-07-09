package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.CHANGE.FileUtils;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.RESPONSE.UserResponse;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;

import java.io.File;
import java.io.IOException;

import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_Fragment extends Fragment {

    private TextView btnLogout, btnEditProfile;
    private TextView textUserName, textEmail, textPhone, textSubEmail, textSubPhone;
    private ImageView imageAvatar;
    private Uri selectedImageUri;
    private static final int REQUEST_PICK_IMAGE = 101;
    private String userId;
    private Dialog updateProfileDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        // Khởi tạo view
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        imageAvatar = view.findViewById(R.id.imageAvatar);
        textUserName = view.findViewById(R.id.textUserName);
        textEmail = view.findViewById(R.id.textEmail);
        textPhone = view.findViewById(R.id.textPhone);
        textSubEmail = view.findViewById(R.id.textSubEmail);
        textSubPhone = view.findViewById(R.id.textSubPhone);

        // Lấy thông tin từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        String fullname = prefs.getString("fullname", "Tên chưa cập nhật");
        String email = prefs.getString("email", "Email chưa cập nhật");
        String phone = prefs.getString("numberphone", "SĐT chưa cập nhật");
        String image = prefs.getString("image", "");

        // Gán vào TextView
        textUserName.setText(fullname);
        textEmail.setText("Email: " + email);
        textPhone.setText("SĐT: " + phone);
        textSubEmail.setText("Email phụ: phu@example.com");
        textSubPhone.setText("SĐT phụ: 0123456789");

        // Ảnh đại diện
        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).placeholder(R.drawable.taikhoan).into(imageAvatar);
        } else {
            imageAvatar.setImageResource(R.drawable.taikhoan);
        }

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(requireActivity(), Login_Activity.class));
            requireActivity().finish();
        });

        // Cập nhật hồ sơ
        btnEditProfile.setOnClickListener(v -> showUpdateDialog());

        return view;
    }

    private void showUpdateDialog() {
        updateProfileDialog = new Dialog(requireContext());
        updateProfileDialog.setContentView(R.layout.diglog_infomation_update);

        TextInputEditText edtFullname = updateProfileDialog.findViewById(R.id.edtFullname);
        TextInputEditText edtPhone = updateProfileDialog.findViewById(R.id.edtPhone);
        ImageView imgAvatarInDialog = updateProfileDialog.findViewById(R.id.imgAvatar);
        MaterialButton btnUpdate = updateProfileDialog.findViewById(R.id.btnUpdate);
        MaterialButton btnChangeAvatar = updateProfileDialog.findViewById(R.id.btnChangeAvatar);
        MaterialButton btnBack = updateProfileDialog.findViewById(R.id.btnBack);

        // Dialog style
        if (updateProfileDialog.getWindow() != null) {
            updateProfileDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background));
            updateProfileDialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Gán dữ liệu cũ
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String currentFullname = prefs.getString("fullname", "");
        String currentPhone = prefs.getString("numberphone", "");
        String currentImage = prefs.getString("image", "");

        edtFullname.setText(currentFullname);
        edtPhone.setText(currentPhone);
        Glide.with(this).load(currentImage).placeholder(R.drawable.taikhoan).into(imgAvatarInDialog);

        selectedImageUri = null;

        btnBack.setOnClickListener(v -> updateProfileDialog.dismiss());

        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        });

        btnUpdate.setOnClickListener(v -> {
            String newName = edtFullname.getText().toString().trim();
            String newPhone = edtPhone.getText().toString().trim();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            btnUpdate.setEnabled(false);
            if (selectedImageUri != null) {
                uploadAvatarAndUpdateUser(selectedImageUri, newName, newPhone, updateProfileDialog);
            } else {
                updateUserInfo(newName, newPhone, currentImage, updateProfileDialog);
            }
        });

        updateProfileDialog.show();
    }

    private void uploadAvatarAndUpdateUser(Uri imageUri, String name, String phone, Dialog dialog) {
        File file = new File(FileUtils.getPath(requireContext(), imageUri));
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Không tìm thấy file ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
        RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), userId);

        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        apiService.uploadImage(userIdPart, avatarPart).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String imageUrl = response.body().getImageUrl();
                    updateUserInfo(name, phone, imageUrl, dialog);
                } else {
                    Toast.makeText(requireContext(), "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi mạng khi upload ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(String name, String phone, String imageUrl, Dialog dialog) {
        UserUpdateRequest req = new UserUpdateRequest(userId, name, phone, imageUrl);

        RetrofitClient.getInstance(requireContext()).create(ApiService.class).updateUser(req).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse user = response.body();

                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE).edit();
                    editor.putString("fullname", user.getUser().getFullname());
                    editor.putString("numberphone", user.getUser().getNumberphone());
                    editor.putString("image", user.getUser().getImage());
                    editor.apply();

                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    requireActivity().recreate();
                } else {
                    Toast.makeText(requireContext(), "Lỗi cập nhật: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            if (updateProfileDialog != null && updateProfileDialog.isShowing()) {
                ImageView imgAvatarInDialog = updateProfileDialog.findViewById(R.id.imgAvatar);
                if (imgAvatarInDialog != null) {
                    Glide.with(this).load(selectedImageUri).into(imgAvatarInDialog);
                }
            }
        }
    }
}
