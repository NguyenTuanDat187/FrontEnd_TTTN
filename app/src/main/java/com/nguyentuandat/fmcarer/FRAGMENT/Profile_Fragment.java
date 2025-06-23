package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.CHANGE.FileUtils;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_Fragment extends Fragment {

    private TextView btnLogout, btnEditProfile;
    private TextView textUserName, textEmail, textPhone, textSubEmail, textSubPhone;
    private ImageView imageAvatar;
    private Uri selectedImageUri;
    private static final int REQUEST_PICK_IMAGE = 101;

    private String userId; // <== Lưu _id từ SharedPreferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        imageAvatar = view.findViewById(R.id.imageAvatar);
        textUserName = view.findViewById(R.id.textUserName);
        textEmail = view.findViewById(R.id.textEmail);
        textPhone = view.findViewById(R.id.textPhone);
        textSubEmail = view.findViewById(R.id.textSubEmail);
        textSubPhone = view.findViewById(R.id.textSubPhone);

        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        String fullname = prefs.getString("fullname", "Tên chưa cập nhật");
        String email = prefs.getString("email", "Email chưa cập nhật");
        String phone = prefs.getString("numberphone", "SĐT chưa cập nhật");
        String image = prefs.getString("image", "");

        textUserName.setText(fullname);
        textEmail.setText("Email: " + email);
        textPhone.setText("SĐT: " + phone);
        textSubEmail.setText("Email phụ: phu@example.com");
        textSubPhone.setText("SĐT phụ: 0123456789");

        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).placeholder(R.drawable.taikhoan).into(imageAvatar);
        }

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(requireActivity(), Login_Activity.class));
            requireActivity().finish();
        });

        btnEditProfile.setOnClickListener(v -> showUpdateDialog());

        return view;
    }

    private void showUpdateDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.diglog_infomation_update);

        TextInputEditText edtFullname = dialog.findViewById(R.id.edtFullname);
        TextInputEditText edtPhone = dialog.findViewById(R.id.edtPhone);
        ImageView imgAvatar = dialog.findViewById(R.id.imgAvatar);
        MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
        MaterialButton btnChangeAvatar = dialog.findViewById(R.id.btnChangeAvatar);
        MaterialButton btnBack = dialog.findViewById(R.id.btnBack);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background));

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        String fullname = prefs.getString("fullname", "");
        String phone = prefs.getString("numberphone", "");
        String image = prefs.getString("image", "");

        edtFullname.setText(fullname);
        edtPhone.setText(phone);
        Glide.with(this).load(image).placeholder(R.drawable.taikhoan).into(imgAvatar);

        btnBack.setOnClickListener(v -> dialog.dismiss());

        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        });

        btnUpdate.setOnClickListener(v -> {
            String newName = edtFullname.getText().toString().trim();
            String newPhone = edtPhone.getText().toString().trim();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadAvatarAndUpdateUser(selectedImageUri, newName, newPhone, dialog);
            } else {
                updateUserInfo(newName, newPhone, image, dialog);
            }
        });

        dialog.show();
    }

    private void uploadAvatarAndUpdateUser(Uri imageUri, String name, String phone, Dialog dialog) {
        File file = new File(FileUtils.getPath(requireContext(), imageUri));
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);

        RetrofitClient.getInstance().create(ApiService.class).uploadImage(avatarPart).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getImageUrl(); // ✅ Đúng

                    updateUserInfo(name, phone, imageUrl, dialog);
                } else {
                    Toast.makeText(requireContext(), "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(String name, String phone, String imageUrl, Dialog dialog) {
        UserUpdateRequest req = new UserUpdateRequest(userId, name, phone, imageUrl);

        RetrofitClient.getInstance().create(ApiService.class).updateUser(req).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse user = response.body();

                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE).edit();
                    editor.putString("fullname", user.getUser().getFullname());
                    editor.putString("numberphone", user.getUser().getNumberphone());
                    editor.putString("image", user.getUser().getImage());
                    editor.apply();

                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    requireActivity().recreate();
                } else {
                    Toast.makeText(requireContext(), "Lỗi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == -1 && data != null) {
            selectedImageUri = data.getData();

            ImageView imgAvatar = getDialogAvatarImageView();
            if (imgAvatar != null) {
                Glide.with(this).load(selectedImageUri).into(imgAvatar);
            }
        }
    }

    private ImageView getDialogAvatarImageView() {
        // Placeholder: Viết lại nếu cần tìm imageView dialog
        return null;
    }
}
