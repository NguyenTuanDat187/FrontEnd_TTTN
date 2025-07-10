package com.nguyentuandat.fmcarer.VIEW;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SubUserLoginRequest;
import com.nguyentuandat.fmcarer.RESPONSE.UserResponse;
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

    private SharedPreferences loginPrefs;
    private static final String PREF_LOGIN_CREDS = "login_credentials";
    private static final String PREF_USER_SESSION = "user_session";
    private static final String KEY_AUTH_TOKEN = "token"; // Hằng số cho key của token

    private ApiService apiService;
    private static final String TAG = "Login_Activity"; // Tag cho Logcat

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

        loginPrefs = getSharedPreferences(PREF_LOGIN_CREDS, MODE_PRIVATE);
        loadSavedCredentials();

        // ✅ Khởi tạo ApiService ngay sau khi UI được thiết lập
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        btnLogin.setOnClickListener(view -> attemptLogin());

        txtGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, Signin_Activity.class));
            finish();
        });

        txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, Forgot_password_Activity.class));
        });

        // Nếu có email gửi sang từ đăng ký
        String receivedEmail = getIntent().getStringExtra("email");
        String receivedPass = getIntent().getStringExtra("password");
        if (receivedEmail != null) {
            edtLoginEmail.setText(receivedEmail);
            edtLoginPassword.setText(receivedPass != null ? receivedPass : "");
        }

        // Nếu có token đã lưu → vào thẳng Dashboard
        String savedToken = getSharedPreferences(PREF_USER_SESSION, MODE_PRIVATE).getString(KEY_AUTH_TOKEN, null);
        // ✅ Log token được tìm thấy khi khởi tạo Activity
        if (savedToken != null && !savedToken.isEmpty()) {
            Log.d(TAG, "onCreate: Found saved token, navigating to Dashboard. Token (first 10 chars): " + savedToken.substring(0, Math.min(savedToken.length(), 10)));
            startActivity(new Intent(this, Dashboar_Activity.class));
            finish();
        } else {
            Log.d(TAG, "onCreate: No saved token found or token is empty.");
        }
    }

    private void attemptLogin() {
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
    }

    private void loginUser(String input, String password) {
        Call<UserResponse> call;

        if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            call = apiService.loginUser(new UserRequest(input, password));
        } else {
            call = apiService.loginSubUser(new SubUserLoginRequest(input, password));
        }

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.UserData user = response.body().getUser();
                    String token = response.body().getAccessToken();

                    // ✅ Log token nhận được từ API
                    Log.d(TAG, "onResponse: Login successful. Received Token (first 10 chars): " + (token != null ? token.substring(0, Math.min(token.length(), 10)) : "null"));

                    if (user != null && token != null && !token.isEmpty()) {
                        saveUserSession(user, token);

                        boolean isInfoComplete = user.getFullname() != null && !user.getFullname().isEmpty()
                                && user.getNumberphone() != null && !user.getNumberphone().isEmpty()
                                && user.getImage() != null && !user.getImage().isEmpty();

                        Intent intent = new Intent(Login_Activity.this, Dashboar_Activity.class);
                        intent.putExtra("showDialog", !isInfoComplete); // mở dialog nếu thiếu info
                        startActivity(intent);
                        finish();

                        Toast.makeText(Login_Activity.this, "✅ Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login_Activity.this, "Dữ liệu đăng nhập không hợp lệ!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onResponse: User data or token is null/empty after successful response.");
                    }
                } else {
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "API_LOGIN: Lỗi kết nối", t);
                Toast.makeText(Login_Activity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(UserResponse.UserData user, String token) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_USER_SESSION, MODE_PRIVATE).edit();
        editor.putString("_id", user.getId());
        editor.putString("fullname", user.getFullname());
        editor.putString("numberphone", user.getNumberphone());
        editor.putString("image", user.getImage());
        editor.putString("email", user.getEmail());
        editor.putString("role", user.getRole());
        editor.putString(KEY_AUTH_TOKEN, token); // Sử dụng hằng số cho key của token

        // ✅ Log token ngay trước khi apply()
        Log.d(TAG, "saveUserSession: Attempting to save token (first 10 chars): " + (token != null ? token.substring(0, Math.min(token.length(), 10)) : "null"));
        editor.apply();
        Log.d(TAG, "saveUserSession: Token and user data applied to SharedPreferences.");
    }

    private void handleLoginError(Response<UserResponse> response) {
        String errorMessage = "Đăng nhập thất bại!";
        try {
            if (response.errorBody() != null) {
                String errorBodyString = response.errorBody().string();
                // ✅ Log errorBody để xem chi tiết lỗi từ server
                Log.e(TAG, "handleLoginError: Error Body: " + errorBodyString);
                errorMessage = errorBodyString; // Có thể server trả về message lỗi trực tiếp trong errorBody
            } else if (response.body() != null && response.body().getMessage() != null) {
                errorMessage = response.body().getMessage();
            }
        } catch (Exception e) {
            Log.e(TAG, "LOGIN_ERROR: Lỗi đọc message từ errorBody", e);
        }
        Toast.makeText(Login_Activity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void saveCredentials(String input, String password) {
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.putString("input_credential", input);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadSavedCredentials() {
        boolean remember = loginPrefs.getBoolean("remember", false);
        if (remember) {
            edtLoginEmail.setText(loginPrefs.getString("input_credential", ""));
            edtLoginPassword.setText(loginPrefs.getString("password", ""));
            checkboxRemember.setChecked(true);
        }
    }

    private void clearCredentials() {
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.clear();
        editor.apply();
    }
}