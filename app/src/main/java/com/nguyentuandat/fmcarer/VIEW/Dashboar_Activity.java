package com.nguyentuandat.fmcarer.VIEW;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.FRAGMENT.*;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserUpdateRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboar_Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;
    private ImageView imgAvatar;
    private TextView toolbarTitle;

    private ImageView navAvatar;
    private TextView navUsername, navEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle); // 📌 TextView trong Toolbar
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Gán view header
        View headerView = navigationView.getHeaderView(0);
        navAvatar = headerView.findViewById(R.id.imgAvatar);
        navUsername = headerView.findViewById(R.id.txtUsername);
        navEmail = headerView.findViewById(R.id.txtEmail);
        loadHeaderData();

        // Navigation bottom
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new Home_Fragment(), "Trang chủ");
            } else if (id == R.id.account_sub_create) {
                replaceFragment(new Account_Sub_Create_Fragment(), "Tạo tài khoản phụ");
            } else if (id == R.id.nav_schedule) {
                replaceFragment(new Care_schedule_Fragment(), "Lịch chăm sóc");
            } else if (id == R.id.nav_profile) {
                replaceFragment(new Profile_Fragment(), "Thông tin cá nhân");
            }
            return true;
        });

        // Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_top_up) {
                replaceFragment(new Top_Up_Fragment(), "Nạp tiền");
            } else if (id == R.id.nav_schedule) {
                replaceFragment(new Care_schedule_Fragment(), "Lịch chăm sóc");
            } else if (id == R.id.nav_home) {
                replaceFragment(new Home_Fragment(), "Trang chủ");
            } else if (id == R.id.account_sub_create) {
                replaceFragment(new Account_Sub_Create_Fragment(), "Tạo tài khoản phụ");
            } else if (id == R.id.nav_account) {
                replaceFragment(new Profile_Fragment(), "Thông tin cá nhân");
            } else if (id == R.id.nav_children) {
                replaceFragment(new Children_List_Fragment(), "Trẻ đang theo dõi");
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Load màn đầu
        if (savedInstanceState == null) {
            replaceFragment(new Home_Fragment(), "Trang chủ");
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Gợi ý cập nhật nếu thiếu info
        SharedPreferences prefs = getSharedPreferences("USER", MODE_PRIVATE);
        String fullname = prefs.getString("fullname", "");
        String phone = prefs.getString("numberphone", "");
        String image = prefs.getString("image", "");

        if (fullname.isEmpty() || phone.isEmpty() || image.isEmpty()) {
            new Handler().postDelayed(this::showUpdateDialog, 1000);
        }
    }

    private void loadHeaderData() {
        SharedPreferences prefs = getSharedPreferences("USER", MODE_PRIVATE);
        String name = prefs.getString("fullname", "Tên chưa cập nhật");
        String email = prefs.getString("email", "Email chưa cập nhật");
        String image = prefs.getString("image", "");

        navUsername.setText(name);
        navEmail.setText(email);
        if (!image.isEmpty()) {
            Glide.with(this).load(image).placeholder(R.drawable.taikhoan).error(R.drawable.taikhoan).into(navAvatar);
        }
    }

    // 📌 Thêm title fragment vào đây
    private void replaceFragment(androidx.fragment.app.Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }

    private void showUpdateDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.diglog_infomation_update);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dialog_background));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }

        MaterialButton btnUpdate = dialog.findViewById(R.id.btnUpdate);
        MaterialButton btnBack = dialog.findViewById(R.id.btnBack);
        MaterialButton btnChangeAvatar = dialog.findViewById(R.id.btnChangeAvatar);
        TextInputEditText edtFullname = dialog.findViewById(R.id.edtFullname);
        TextInputEditText edtPhone = dialog.findViewById(R.id.edtPhone);
        imgAvatar = dialog.findViewById(R.id.imgAvatar);

        SharedPreferences prefs = getSharedPreferences("USER", MODE_PRIVATE);
        String userId = prefs.getString("_id", null);
        String currentName = prefs.getString("fullname", "");
        String currentPhone = prefs.getString("numberphone", "");
        String currentImage = prefs.getString("image", "");

        edtFullname.setText(currentName);
        edtPhone.setText(currentPhone);
        if (!currentImage.isEmpty()) {
            Glide.with(this).load(currentImage).into(imgAvatar);
        }

        btnBack.setOnClickListener(v -> dialog.dismiss());

        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnUpdate.setOnClickListener(v -> {
            String name = edtFullname.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == null) {
                Toast.makeText(this, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : currentImage;
            UserUpdateRequest request = new UserUpdateRequest(userId, name, phone, imageUrl);

            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            Call<UserResponse> call = apiService.updateUser(request);

            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(Dashboar_Activity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                        prefs.edit()
                                .putString("fullname", name)
                                .putString("numberphone", phone)
                                .putString("image", imageUrl)
                                .apply();

                        loadHeaderData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(Dashboar_Activity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Toast.makeText(Dashboar_Activity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(imgAvatar);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
