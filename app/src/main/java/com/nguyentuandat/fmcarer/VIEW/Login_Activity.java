package com.nguyentuandat.fmcarer.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login_Activity extends AppCompatActivity {

    private TextInputEditText edtLoginEmail, edtLoginPassword;
    private Button btnLogin;
    private TextView txtGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoToRegister = findViewById(R.id.txtGoToRegister);

        btnLogin.setOnClickListener(view -> {
            String email = edtLoginEmail.getText().toString().trim();
            String password = edtLoginPassword.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtLoginEmail.setError("Email không hợp lệ");
                return;
            }
            if (password.length() < 6) {
                edtLoginPassword.setError("Mật khẩu phải từ 6 ký tự");
                return;
            }

            loginUser(email, password);
        });

        txtGoToRegister.setOnClickListener(view -> {
            startActivity(new Intent(Login_Activity.this, Signin_Activity.class));
            finish();
        });
    }

    private void loginUser(String email, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:6000/") // đổi IP nếu chạy trên thiết bị thật
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        UserRequest request = new UserRequest(email, password);

        apiService.loginUser(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                Log.d("API_LOGIN", "Status code: " + response.code());

                if (response.body() != null) {
                    Log.d("API_LOGIN", "Success: " + response.body().isSuccess());
                    Log.d("API_LOGIN", "Message: " + response.body().getMessage());

                    if (response.body().getUser() != null) {
                        Log.d("API_LOGIN", "User Email: " + response.body().getUser().getEmail());
                        Log.d("API_LOGIN", "User Verified: " + response.body().getUser().isVerified());
                    }
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Login_Activity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login_Activity.this, Dashboar_Activity.class));
                    finish();
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
}
