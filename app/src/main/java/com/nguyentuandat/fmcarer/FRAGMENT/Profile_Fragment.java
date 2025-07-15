package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyentuandat.fmcarer.ADAPTER.Frofile_ADAPTER;
import com.nguyentuandat.fmcarer.CHANGE.FileUtils;
import com.nguyentuandat.fmcarer.MODEL.User;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PasswordVerificationRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserRequest; // <-- Import SubUserRequest
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.ApiResponse;
import com.nguyentuandat.fmcarer.RESPONSE.UserListResponse;
import com.nguyentuandat.fmcarer.RESPONSE.UserResponse;
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_Fragment extends Fragment implements Frofile_ADAPTER.OnSubUserActionListener {

    private static final String TAG = "Profile_Fragment";
    private TextView btnLogout, btnEditProfile, textUserName, textEmail, textPhone;
    private ImageView imageAvatar;
    private Uri selectedImageUri; // For main user
    private Uri selectedSubuserImageUri; // For subuser
    private static final int REQUEST_PICK_IMAGE = 101; // For main user
    private static final int REQUEST_PICK_IMAGE_SUBUSER = 102; // For subuser
    private String userId;
    private Dialog updateProfileDialog;
    private Dialog subuserFormDialogInstance; // Dialog for add/edit subuser
    private RecyclerView recyclerSubuser;
    private Frofile_ADAPTER subuserAdapter;
    private MaterialButton btnAddSubuser; // Nút thêm tài khoản phụ

    private String currentFullname;
    private String currentEmail;
    private String currentPhone;
    private String currentImage;
    private String currentToken;

    private SharedPreferences loginPrefs;

    // Biến tạm để lưu trữ subuser đang được chỉnh sửa/xóa
    private User currentSubuserForAction;
    private String currentActionType; // "edit" or "delete"

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

        recyclerSubuser = view.findViewById(R.id.recyclerSubAccounts);
        recyclerSubuser.setLayoutManager(new LinearLayoutManager(getContext()));
        subuserAdapter = new Frofile_ADAPTER(requireContext(), new ArrayList<>(), this);
        recyclerSubuser.setAdapter(subuserAdapter);
        loginPrefs = requireActivity().getSharedPreferences("login_credentials", Context.MODE_PRIVATE);



        loadUserDataFromSharedPreferences();
        updateUIWithUserData();

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(requireActivity(), Login_Activity.class));
            requireActivity().finish();
        });

        btnEditProfile.setOnClickListener(v -> showUpdate_UserDialog());

        fetchSubusers();

        return view;
    }

    private void loadUserDataFromSharedPreferences() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        currentFullname = prefs.getString("fullname", "Tên chưa cập nhật");
        currentEmail = prefs.getString("email", "Email chưa cập nhật");
        currentPhone = prefs.getString("numberphone", "SĐT chưa cập nhật");
        currentImage = prefs.getString("image", "");
        currentToken = prefs.getString("token", "");
        Log.d(TAG, "Loaded UserId: " + userId);
        Log.d(TAG, "Loaded Token (first 10 chars): " + (currentToken != null ? currentToken.substring(0, Math.min(currentToken.length(), 10)) + "..." : "null"));
    }

    private void updateUIWithUserData() {
        textUserName.setText(currentFullname);
        textEmail.setText("Email: " + currentEmail);
        textPhone.setText("SĐT: " + currentPhone);

        if (currentImage != null && !currentImage.isEmpty()) {
            Glide.with(this).load(currentImage).placeholder(R.drawable.taikhoan).error(R.drawable.taikhoan).into(imageAvatar);
        } else {
            imageAvatar.setImageResource(R.drawable.taikhoan);
        }
    }

    private void showUpdate_UserDialog() {
        updateProfileDialog = new Dialog(requireContext());
        updateProfileDialog.setContentView(R.layout.diglog_infomation_update);

        TextInputEditText edtFullname = updateProfileDialog.findViewById(R.id.edtFullname);
        TextInputEditText edtPhone = updateProfileDialog.findViewById(R.id.edtPhone);
        ImageView imgAvatarInDialog = updateProfileDialog.findViewById(R.id.imgAvatar);
        MaterialButton btnUpdate = updateProfileDialog.findViewById(R.id.btnUpdate);
        MaterialButton btnChangeAvatar = updateProfileDialog.findViewById(R.id.btnChangeAvatar);
        MaterialButton btnBack = updateProfileDialog.findViewById(R.id.btnBack);

        if (updateProfileDialog.getWindow() != null) {
            updateProfileDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background));
            updateProfileDialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        edtFullname.setText(currentFullname);
        edtPhone.setText(currentPhone);
        if (currentImage != null && !currentImage.isEmpty()) {
            Glide.with(this).load(currentImage).placeholder(R.drawable.taikhoan).error(R.drawable.taikhoan).into(imgAvatarInDialog);
        } else {
            imgAvatarInDialog.setImageResource(R.drawable.taikhoan);
        }

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
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "ID người dùng không khả dụng.", Toast.LENGTH_SHORT).show();
            dialog.findViewById(R.id.btnUpdate).setEnabled(true);
            return;
        }

        File file = new File(FileUtils.getPath(requireContext(), imageUri));
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Không tìm thấy file ảnh", Toast.LENGTH_SHORT).show();
            dialog.findViewById(R.id.btnUpdate).setEnabled(true);
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
                    String errorMsg = "Lỗi upload ảnh";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing errorBody: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Upload Image Error: " + response.code() + " " + response.message() + (response.errorBody() != null ? " - " + errorMsg : ""));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi mạng khi upload ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Upload Image Network Failure: " + t.getMessage(), t);
            }
        });
    }

    private void updateUserInfo(String name, String phone, String imageUrl, Dialog dialog) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "ID người dùng không khả dụng.", Toast.LENGTH_LONG).show();
            dialog.findViewById(R.id.btnUpdate).setEnabled(true);
            return;
        }

        UserUpdateRequest req = new UserUpdateRequest(userId, name, phone, imageUrl);

        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        apiService.updateUser(req).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.UserData user = response.body().getUser();

                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE).edit();
                    editor.putString("fullname", user.getFullname());
                    editor.putString("numberphone", user.getNumberphone());
                    editor.putString("image", user.getImage());
                    editor.apply();

                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadUserDataFromSharedPreferences();
                    updateUIWithUserData();
                    fetchSubusers();
                } else {
                    String errorMsg = "Lỗi cập nhật";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing errorBody: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
                if (btnUpdate != null) btnUpdate.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Update User Network Failure: " + t.getMessage(), t);
            }
        });
    }

    public void fetchSubusers() {
        if (currentToken == null || currentToken.isEmpty() || userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Không có thông tin đăng nhập. Không thể tải tài khoản phụ.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        apiService.getAllSubusersByParentId("Bearer " + currentToken, userId).enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserListResponse> call, @NonNull Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Chắc chắn gọi getUsers() vì UserListResponse có thể trả về users, không phải subusers
                    List<User> subusers = response.body().getSubusers(); // <-- Đã sửa
                    Log.d(TAG, "Fetched " + (subusers != null ? subusers.size() : 0) + " subusers successfully.");
                    subuserAdapter.updateData(subusers);
                } else {
                    String errorMsg = "Lỗi khi lấy danh sách tài khoản phụ";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            JSONObject jsonError = new JSONObject(errorBodyString);
                            if (jsonError.has("message")) {
                                errorMsg = jsonError.getString("message");
                            } else {
                                errorMsg += ": " + errorBodyString;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing errorBody for fetchSubusers: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fetch Subusers Error: " + response.code() + " " + response.message() + " - " + errorMsg);
                    subuserAdapter.updateData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserListResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi lấy tài khoản phụ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Fetch Subusers Network Failure: " + t.getMessage(), t);
                subuserAdapter.updateData(new ArrayList<>());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                selectedImageUri = data.getData();
                if (updateProfileDialog != null && updateProfileDialog.isShowing()) {
                    ImageView imgAvatarInDialog = updateProfileDialog.findViewById(R.id.imgAvatar);
                    if (imgAvatarInDialog != null) {
                        Glide.with(this).load(selectedImageUri).placeholder(R.drawable.taikhoan).error(R.drawable.taikhoan).into(imgAvatarInDialog);
                    }
                }
            } else if (requestCode == REQUEST_PICK_IMAGE_SUBUSER) {
                selectedSubuserImageUri = data.getData();
                if (subuserFormDialogInstance != null && subuserFormDialogInstance.isShowing()) {
                    ImageView imgSubuserAvatar = subuserFormDialogInstance.findViewById(R.id.imgSubuserAvatar);
                    if (imgSubuserAvatar != null) {
                        Glide.with(this).load(selectedSubuserImageUri).placeholder(R.drawable.taikhoan).error(R.drawable.taikhoan).into(imgSubuserAvatar);
                    }
                }
            }
        }
    }

    // --- OnSubUserActionListener implementation ---
    @Override
    public void onActionWithPasswordConfirmation(User subuser, String actionType, String password, AlertDialog passwordDialog) {
        this.currentSubuserForAction = subuser;
        this.currentActionType = actionType;
        verifyUserPassword(userId, password, subuser, actionType, passwordDialog); // <-- Đổi tên hàm gọi API
    }
    // --- End OnSubUserActionListener methods ---

    private void verifyUserPassword(String parentId, String password, User subuser, String actionType, AlertDialog passwordDialog) { // <-- Đổi tên hàm
        if (currentToken == null || currentToken.isEmpty()) {
            Toast.makeText(requireContext(), "Token không khả dụng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            if (passwordDialog != null && passwordDialog.isShowing()) {
                passwordDialog.dismiss();
            }
            return;
        }

        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        PasswordVerificationRequest request = new PasswordVerificationRequest(parentId, password);

        apiService.verifyUserPassword("Bearer " + currentToken, request).enqueue(new Callback<ApiResponse>() { // <-- Đổi tên hàm gọi API
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Xác nhận mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        passwordDialog.dismiss();
                    }

                    if ("edit".equals(actionType)) {
                        showSubuserFormDialog(subuser);
                    } else if ("delete".equals(actionType)) {
                        deleteSubuser(subuser);
                    }
                } else {
                    String errorMsg = "Xác nhận mật khẩu thất bại. Vui lòng kiểm tra lại.";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            JSONObject jsonError = new JSONObject(errorBodyString);
                            if (jsonError.has("message")) {
                                errorMsg = jsonError.getString("message");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing errorBody for password verification: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Password Verification Failed: " + response.code() + " " + response.message() + " - " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi xác nhận mật khẩu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Password Verification Network Failure: " + t.getMessage(), t);
            }
        });
    }

    private void showSubuserFormDialog(@NonNull User subuserToEdit) { // Đổi từ @Nullable thành @NonNull
        // Đảm bảo subuserToEdit không bao giờ là null khi gọi hàm này
        // Bạn nên kiểm tra trước khi gọi hàm này hoặc đảm bảo luồng code không cho phép null.
        // Ví dụ, chỉ gọi từ sự kiện click vào item subuser đã có.

        subuserFormDialogInstance = new Dialog(requireContext());
        subuserFormDialogInstance.setContentView(R.layout.diglog_subuser_form); // Đảm bảo đúng layout name

        if (subuserFormDialogInstance.getWindow() != null) {
            subuserFormDialogInstance.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background));
            subuserFormDialogInstance.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDialogTitle = subuserFormDialogInstance.findViewById(R.id.tvDialogTitle);
        ImageView imgSubuserAvatar = subuserFormDialogInstance.findViewById(R.id.imgSubuserAvatar);
        MaterialButton btnChangeSubuserAvatar = subuserFormDialogInstance.findViewById(R.id.btnChangeSubuserAvatar);
        TextInputEditText editFullName = subuserFormDialogInstance.findViewById(R.id.editFullName);
        TextInputEditText editSubPhone = subuserFormDialogInstance.findViewById(R.id.editSubPhone);
        TextInputEditText editSubPassword = subuserFormDialogInstance.findViewById(R.id.editSubPassword);
        TextInputEditText editConfirmSubPassword = subuserFormDialogInstance.findViewById(R.id.editConfirmSubPassword);
        TextInputLayout layoutSubPassword = subuserFormDialogInstance.findViewById(R.id.layoutSubPassword);
        TextInputLayout layoutConfirmSubPassword = subuserFormDialogInstance.findViewById(R.id.layoutConfirmSubPassword);
        MaterialButton btnCancel = subuserFormDialogInstance.findViewById(R.id.btnCancel);
        MaterialButton btnSaveSubAccount = subuserFormDialogInstance.findViewById(R.id.btnSaveSubAccount);

        selectedSubuserImageUri = null;

        // --- Bắt đầu phần chỉ dành cho CHỈNH SỬA ---

        tvDialogTitle.setText("Chỉnh Sửa Tài Khoản Phụ");
        editFullName.setText(subuserToEdit.getFullname());
        editSubPhone.setText(subuserToEdit.getNumberphone());

        // Luôn hiển thị trường mật khẩu và xác nhận mật khẩu để người dùng có thể đổi
        layoutSubPassword.setVisibility(View.VISIBLE);
        layoutConfirmSubPassword.setVisibility(View.VISIBLE);
        editSubPassword.setText(""); // KHÔNG set text cho password để tránh lộ thông tin
        editConfirmSubPassword.setText(""); // KHÔNG set text cho password để tránh lộ thông tin

        btnSaveSubAccount.setText("Cập nhật");

        if (subuserToEdit.getImage() != null && !subuserToEdit.getImage().isEmpty()) {
            Glide.with(this).load(subuserToEdit.getImage()).placeholder(R.drawable.taikhoan).error(R.drawable.taikhoan).into(imgSubuserAvatar);
        } else {
            imgSubuserAvatar.setImageResource(R.drawable.taikhoan);
        }

        // --- Kết thúc phần chỉ dành cho CHỈNH SỬA ---


        btnChangeSubuserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_IMAGE_SUBUSER);
        });

        btnCancel.setOnClickListener(v -> subuserFormDialogInstance.dismiss());

        btnSaveSubAccount.setOnClickListener(v -> {
            String fullName = editFullName.getText().toString().trim();
            String phone = editSubPhone.getText().toString().trim();
            String password = editSubPassword.getText().toString().trim();
            String confirmPassword = editConfirmSubPassword.getText().toString().trim();

            if (fullName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ họ tên và số điện thoại.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Logic kiểm tra mật khẩu chỉ khi người dùng nhập vào các trường mật khẩu
            if (!password.isEmpty() || !confirmPassword.isEmpty()) {
                if (password.isEmpty()) { // Nếu một trong hai trường có dữ liệu nhưng trường này rỗng
                    Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Nếu cả password và confirmPassword đều rỗng, chúng ta sẽ không gửi mật khẩu lên backend,
            // backend sẽ hiểu là không thay đổi mật khẩu.

            btnSaveSubAccount.setEnabled(false);

            SubUserRequest subUserRequest = new SubUserRequest();
            subUserRequest.setId(subuserToEdit.getId()); // ID của subuser cần cập nhật LUÔN CÓ
            subUserRequest.setFullname(fullName);
            subUserRequest.setNumberphone(phone);
            subUserRequest.setCreatedBy(userId); // Giữ parentId (main user's ID)
            subUserRequest.setRole("subuser"); // Đảm bảo role là 'subuser'

            // Giữ ảnh cũ nếu không đổi, nếu có ảnh mới sẽ được cập nhật sau
            subUserRequest.setImage(subuserToEdit.getImage());

            // Chỉ đặt mật khẩu nếu người dùng đã nhập
            if (!password.isEmpty()) {
                subUserRequest.setPassword(password);
            }

            // Gọi hàm lưu/cập nhật, xử lý upload ảnh nếu có
            if (selectedSubuserImageUri != null) {
                uploadSubuserAvatarAndSave(selectedSubuserImageUri, subUserRequest, subuserFormDialogInstance);
            } else {
                saveSubuser(subUserRequest, subuserFormDialogInstance);
            }
        });

        subuserFormDialogInstance.show();
    }

    private void uploadSubuserAvatarAndSave(Uri imageUri, SubUserRequest subUserRequest, Dialog dialog) {
        // Nếu là thêm mới và chưa có ID, thì upload ảnh phải là một phần của tạo subuser
        // hoặc bạn phải tạo subuser trước để có ID rồi mới upload ảnh.
        // Hiện tại, API uploadImage yêu cầu userId.
        // Nếu subUserRequest.getId() == null thì phải là API tạo mới có upload ảnh hoặc upload sau khi tạo.
        // Cần xem xét lại luồng nếu subUserRequest.getId() luôn null khi thêm mới.
        // Tạm thời, tôi sẽ giữ nguyên logic này, giả định rằng khi upload ảnh, subUserRequest đã có ID nếu là edit.
        // Nếu là thêm mới, có thể cần một API khác hoặc upload ảnh sau khi tạo xong subuser.

        if (subUserRequest.getId() == null || subUserRequest.getId().isEmpty()) {
            Toast.makeText(requireContext(), "ID tài khoản phụ không khả dụng. Không thể upload ảnh.", Toast.LENGTH_SHORT).show();
            dialog.findViewById(R.id.btnSaveSubAccount).setEnabled(true);
            return;
        }


        File file = new File(FileUtils.getPath(requireContext(), imageUri));
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Không tìm thấy file ảnh", Toast.LENGTH_SHORT).show();
            dialog.findViewById(R.id.btnSaveSubAccount).setEnabled(true);
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
        RequestBody subuserIdPart = RequestBody.create(MediaType.parse("text/plain"), subUserRequest.getId());

        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        apiService.uploadImage(subuserIdPart, avatarPart).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                MaterialButton btnSaveSubAccount = dialog.findViewById(R.id.btnSaveSubAccount);
                if (btnSaveSubAccount != null) btnSaveSubAccount.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String imageUrl = response.body().getImageUrl();
                    subUserRequest.setImage(imageUrl); // Cập nhật URL ảnh cho subuser
                    saveSubuser(subUserRequest, dialog); // Sau khi upload ảnh, tiến hành lưu thông tin
                } else {
                    String errorMsg = "Lỗi upload ảnh tài khoản phụ";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing errorBody: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Upload Subuser Image Error: " + response.code() + " " + response.message() + (response.errorBody() != null ? " - " + errorMsg : ""));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                MaterialButton btnSaveSubAccount = dialog.findViewById(R.id.btnSaveSubAccount);
                if (btnSaveSubAccount != null) btnSaveSubAccount.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi mạng khi upload ảnh tài khoản phụ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Upload Subuser Image Network Failure: " + t.getMessage(), t);
            }
        });
    }


    private void saveSubuser(SubUserRequest subUserRequest, Dialog dialog) {
        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        if (subUserRequest.getId() == null || subUserRequest.getId().isEmpty()) { // Thêm mới
            // API: POST /api/users/subuser/create-or-update
            apiService.createOrUpdateSubUser(
                    "Bearer " + currentToken,
                    subUserRequest
            ).enqueue(new Callback<ApiResponse>() { // ApiResponse vì API trả về success/message
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                    MaterialButton btnSaveSubAccount = dialog.findViewById(R.id.btnSaveSubAccount);
                    if (btnSaveSubAccount != null) btnSaveSubAccount.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(requireContext(), "Thêm tài khoản phụ thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchSubusers();
                    } else {
                        String errorMsg = "Lỗi khi thêm tài khoản phụ";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ": " + response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Error parsing errorBody: " + e.getMessage());
                            }
                        }
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Add Subuser Error: " + response.code() + " " + response.message() + " - " + errorMsg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    MaterialButton btnSaveSubAccount = dialog.findViewById(R.id.btnSaveSubAccount);
                    if (btnSaveSubAccount != null) btnSaveSubAccount.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi mạng khi thêm tài khoản phụ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Add Subuser Network Failure: " + t.getMessage(), t);
                }
            });
        } else { // Chỉnh sửa
            // API: PUT /api/users/subuser/{subuserId}
            apiService.updateSubUser(
                    "Bearer " + currentToken,
                    subUserRequest.getId(),
                    subUserRequest
            ).enqueue(new Callback<ApiResponse>() { // ApiResponse vì API trả về success/message
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                    MaterialButton btnSaveSubAccount = dialog.findViewById(R.id.btnSaveSubAccount);
                    if (btnSaveSubAccount != null) btnSaveSubAccount.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(requireContext(), "Cập nhật tài khoản phụ thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchSubusers();
                    } else {
                        String errorMsg = "Lỗi khi cập nhật tài khoản phụ";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ": " + response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Error parsing errorBody: " + e.getMessage());
                            }
                        }
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Update Subuser Error: " + response.code() + " " + response.message() + " - " + errorMsg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    MaterialButton btnSaveSubAccount = dialog.findViewById(R.id.btnSaveSubAccount);
                    if (btnSaveSubAccount != null) btnSaveSubAccount.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi mạng khi cập nhật tài khoản phụ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update Subuser Network Failure: " + t.getMessage(), t);
                }
            });
        }
    }


    private void deleteSubuser(User subuser) {
        if (currentToken == null || currentToken.isEmpty()) {
            Toast.makeText(requireContext(), "Token không khả dụng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (subuser.getId() == null || subuser.getId().isEmpty()) {
            Toast.makeText(requireContext(), "ID tài khoản phụ không khả dụng để xóa.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        apiService.deleteSubuser(
                "Bearer " + currentToken,
                subuser.getId()
        ).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Xóa tài khoản phụ thành công!", Toast.LENGTH_SHORT).show();
                    fetchSubusers();
                } else {
                    String errorMsg = "Lỗi khi xóa tài khoản phụ";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            JSONObject jsonError = new JSONObject(errorBodyString);
                            if (jsonError.has("message")) {
                                errorMsg = jsonError.getString("message");
                            } else {
                                errorMsg += ": " + errorBodyString;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing errorBody for deleteSubuser: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Delete Subuser Error: " + response.code() + " " + response.message() + " - " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi xóa tài khoản phụ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Delete Subuser Network Failure: " + t.getMessage(), t);
            }
        });
    }
}