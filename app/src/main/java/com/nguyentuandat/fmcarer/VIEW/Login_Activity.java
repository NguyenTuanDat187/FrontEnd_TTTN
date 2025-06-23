package com.nguyentuandat.fmcarer.VIEW;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login_Activity extends AppCompatActivity {

    private TextInputEditText edtLoginEmail, edtLoginPassword;
    private Button btnLogin;
    private TextView txtGoToRegister, txtForgotPassword;
    private CheckBox checkboxRemember;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "login_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoToRegister = findViewById(R.id.txtGoToRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        checkboxRemember = findViewById(R.id.checkboxRemember);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadSavedCredentials();

        btnLogin.setOnClickListener(view -> {
            String input = edtLoginEmail.getText().toString().trim();
            String password = edtLoginPassword.getText().toString().trim();

            if (input.isEmpty()) {
                edtLoginEmail.setError("Vui lòng nhập email hoặc số điện thoại");
                return;
            }

            if (password.length() < 6) {
                edtLoginPassword.setError("Mật khẩu phải từ 6 ký tự");
                return;
            }

            if (checkboxRemember.isChecked()) {
                saveCredentials(input, password);
            } else {
                clearCredentials();
            }

            loginUser(input, password);
        });

        txtGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(Login_Activity.this, Signin_Activity.class));
            finish();
        });

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Activity.this, Forgot_password_Activity.class);
            startActivity(intent);
        });

        String receivedEmail = getIntent().getStringExtra("email");
        if (receivedEmail != null) {
            edtLoginEmail.setText(receivedEmail);
            edtLoginPassword.requestFocus();
        }
    }

    private void loginUser(String input, String password) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        UserRequest request = new UserRequest(null, password);

        if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            request.setEmail(input);
        } else {
            request.setNumberphone(input);
        }

        apiService.loginUser(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.UserData user = response.body().getUser();

                    if (user != null && user.getId() != null) {
                        if ("parent".equals(user.getRole()) && (user.getEmail() == null || user.getEmail().isEmpty())) {
                            Toast.makeText(Login_Activity.this, "Tài khoản phụ huynh phải đăng nhập bằng email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ✅ Lưu vào USER (để dùng chung)
                        SharedPreferences.Editor editor = getSharedPreferences("USER", MODE_PRIVATE).edit();
                        editor.putString("_id", user.getId());
                        editor.putString("fullname", user.getFullname());
                        editor.putString("numberphone", user.getNumberphone());
                        editor.putString("image", user.getImage());
                        editor.putString("email", user.getEmail());
                        editor.putString("role", user.getRole());
                        editor.apply();

                        // ✅ Lưu riêng parentId để sử dụng cho subuser
                        SharedPreferences pref = getSharedPreferences("USER_PREF", MODE_PRIVATE);
                        pref.edit().putString("userId", user.getId()).apply();

                        Log.d("LOGIN_SUCCESS", "User ID: " + user.getId());

                        boolean isInfoComplete = user.getFullname() != null && !user.getFullname().isEmpty()
                                && user.getNumberphone() != null && !user.getNumberphone().isEmpty()
                                && user.getImage() != null && !user.getImage().isEmpty();

                        Intent intent = new Intent(Login_Activity.this, Dashboar_Activity.class);
                        intent.putExtra("showDialog", !isInfoComplete);
                        startActivity(intent);
                        finish();

                        Toast.makeText(Login_Activity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login_Activity.this, "Lỗi dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login_Activity.this, "Tài khoản hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("API_LOGIN", "Login failed", t);
                Toast.makeText(Login_Activity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadSavedCredentials() {
        boolean remember = sharedPreferences.getBoolean("remember", false);
        if (remember) {
            edtLoginEmail.setText(sharedPreferences.getString("email", ""));
            edtLoginPassword.setText(sharedPreferences.getString("password", ""));
            checkboxRemember.setChecked(true);
        }
    }

    private void clearCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
