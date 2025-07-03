package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.Activity; // Import Activity
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
import com.nguyentuandat.fmcarer.CHANGE.FileUtils; // Make sure FileUtils is correctly implemented
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse; // Make sure this model exists and is correct
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;

import java.io.File; // Import File
import java.io.IOException;

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
    private Uri selectedImageUri; // Stores the URI of the newly selected image
    private static final int REQUEST_PICK_IMAGE = 101;

    private String userId; // Stores _id from SharedPreferences
    private Dialog updateProfileDialog; // ✅ Keep a reference to the dialog

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

        // Retrieve user data from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        String fullname = prefs.getString("fullname", "Tên chưa cập nhật");
        String email = prefs.getString("email", "Email chưa cập nhật");
        String phone = prefs.getString("numberphone", "SĐT chưa cập nhật");
        String image = prefs.getString("image", ""); // Current avatar URL

        // Set retrieved data to TextViews
        textUserName.setText(fullname);
        textEmail.setText("Email: " + email);
        textPhone.setText("SĐT: " + phone);
        textSubEmail.setText("Email phụ: phu@example.com"); // Placeholder, update if you have real data
        textSubPhone.setText("SĐT phụ: 0123456789"); // Placeholder, update if you have real data

        // Load current avatar
        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).placeholder(R.drawable.taikhoan).into(imageAvatar);
        } else {
            imageAvatar.setImageResource(R.drawable.taikhoan); // Default avatar if URL is empty
        }

        // Setup listeners
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply(); // Clear user session
            startActivity(new Intent(requireActivity(), Login_Activity.class));
            requireActivity().finish(); // Finish current activity
        });

        btnEditProfile.setOnClickListener(v -> showUpdateDialog());

        return view;
    }

    private void showUpdateDialog() {
        // ✅ Assign to the global dialog variable
        updateProfileDialog = new Dialog(requireContext());
        updateProfileDialog.setContentView(R.layout.diglog_infomation_update);

        // Get dialog views
        TextInputEditText edtFullname = updateProfileDialog.findViewById(R.id.edtFullname);
        TextInputEditText edtPhone = updateProfileDialog.findViewById(R.id.edtPhone);
        ImageView imgAvatarInDialog = updateProfileDialog.findViewById(R.id.imgAvatar); // This is the ImageView in the dialog
        MaterialButton btnUpdate = updateProfileDialog.findViewById(R.id.btnUpdate);
        MaterialButton btnChangeAvatar = updateProfileDialog.findViewById(R.id.btnChangeAvatar);
        MaterialButton btnBack = updateProfileDialog.findViewById(R.id.btnBack);

        // Set dialog window properties
        if (updateProfileDialog.getWindow() != null) {
            updateProfileDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background));
            updateProfileDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // Populate dialog with current user data
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        String currentFullname = prefs.getString("fullname", "");
        String currentPhone = prefs.getString("numberphone", "");
        String currentImage = prefs.getString("image", ""); // Current avatar URL

        edtFullname.setText(currentFullname);
        edtPhone.setText(currentPhone);

        // Load current avatar into the dialog's ImageView
        Glide.with(this)
                .load(currentImage)
                .placeholder(R.drawable.taikhoan)
                .error(R.drawable.taikhoan) // Show default if loading fails
                .into(imgAvatarInDialog);

        // Initialize selectedImageUri to null each time dialog is opened
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

            // Disable button to prevent multiple clicks during update
            btnUpdate.setEnabled(false);

            if (selectedImageUri != null) {
                // If a new image is selected, upload it first
                uploadAvatarAndUpdateUser(selectedImageUri, newName, newPhone, updateProfileDialog);
            } else {
                // If no new image, use the current image URL
                updateUserInfo(newName, newPhone, currentImage, updateProfileDialog);
            }
        });

        updateProfileDialog.show();
    }

    private void uploadAvatarAndUpdateUser(Uri imageUri, String name, String phone, Dialog dialog) {
        // Ensure context is still available and URI is valid
        if (getContext() == null || !isAdded()) {
            Toast.makeText(getContext(), "Fragment không còn hoạt động.", Toast.LENGTH_SHORT).show();
            // Re-enable button if applicable before returning
            if (dialog != null) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);
            }
            return;
        }

        File file = new File(FileUtils.getPath(requireContext(), imageUri));
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Không tìm thấy file ảnh", Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);
            }
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);

        RetrofitClient.getInstance().create(ApiService.class).uploadImage(avatarPart).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!isAdded()) return; // Check if fragment is still attached

                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true); // Always re-enable button

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String imageUrl = response.body().getImageUrl(); // Get the URL of the uploaded image
                    Log.d("ProfileFragment", "Image uploaded. URL: " + imageUrl);
                    updateUserInfo(name, phone, imageUrl, dialog); // Proceed to update user info with new avatar URL
                } else {
                    String errorMsg = "Lỗi upload ảnh: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("ProfileFragment", "Upload failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return; // Check if fragment is still attached

                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true); // Always re-enable button

                Toast.makeText(requireContext(), "Lỗi kết nối khi upload ảnh: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ProfileFragment", "Upload network failure: " + t.getMessage(), t);
            }
        });
    }

    private void updateUserInfo(String name, String phone, String imageUrl, Dialog dialog) {
        UserUpdateRequest req = new UserUpdateRequest(userId, name, phone, imageUrl);

        RetrofitClient.getInstance().create(ApiService.class).updateUser(req).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!isAdded()) return; // Check if fragment is still attached

                // No need to re-enable button here, as it was handled in uploadAvatarAndUpdateUser
                // or if it was a direct update (no image upload), it can be enabled here.
                // However, since we're chaining calls, it's safer to enable at the end of the chain.
                // Let's re-enable here as a fallback or for non-image updates.
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body();

                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE).edit();
                    editor.putString("fullname", userResponse.getUser().getFullname());
                    editor.putString("numberphone", userResponse.getUser().getNumberphone());
                    editor.putString("image", userResponse.getUser().getImage()); // Update with potentially new image URL
                    editor.apply();

                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Recreate activity to reflect changes in main UI
                    requireActivity().recreate();
                } else {
                    String errorMsg = "Lỗi cập nhật thông tin: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("ProfileFragment", "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("ProfileFragment", "Update user info failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return; // Check if fragment is still attached

                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true); // Always re-enable button

                Toast.makeText(requireContext(), "Lỗi kết nối khi cập nhật thông tin: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ProfileFragment", "Update user info network failure: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Log.d("ProfileFragment", "Image selected: " + selectedImageUri.toString());

            // Get a reference to the ImageView in the currently open dialog
            // ✅ This is the key fix: use the global `updateProfileDialog` reference
            if (updateProfileDialog != null && updateProfileDialog.isShowing()) {
                ImageView imgAvatarInDialog = updateProfileDialog.findViewById(R.id.imgAvatar);
                if (imgAvatarInDialog != null) {
                    Glide.with(this).load(selectedImageUri).into(imgAvatarInDialog);
                } else {
                    Log.e("ProfileFragment", "Dialog ImageView not found!");
                }
            } else {
                Log.e("ProfileFragment", "Update dialog is not active or found.");
            }
        }
    }
}