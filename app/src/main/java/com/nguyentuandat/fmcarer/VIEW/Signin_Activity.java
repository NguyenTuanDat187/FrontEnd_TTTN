package com.nguyentuandat.fmcarer.VIEW;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.OtpResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.UserResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.REPOSITORY.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Signin_Activity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword;
    private TextInputLayout layoutEmail;
    private Button btnRegister;
    private TextView txtLogin;

    private boolean isOtpVerified = false;
    private String verifiedEmail = "";
    private String currentOtpCode = "";

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        layoutEmail = findViewById(R.id.layoutEmail);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:6000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        layoutEmail.setEndIconOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.setError("Email không hợp lệ");
                return;
            }
            layoutEmail.setError(null);
            sendOtpToEmail(email);
        });

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().equals(verifiedEmail)) {
                    isOtpVerified = false;
                }
            }
        });

        btnRegister.setOnClickListener(view -> {
            if (!isOtpVerified || !edtEmail.getText().toString().trim().equals(verifiedEmail)) {
                Toast.makeText(this, "Vui lòng xác minh email bằng mã OTP!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (validateInputs()) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                AuthRepository repository = new AuthRepository();
                repository.registerUser(email, password).enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(Signin_Activity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Signin_Activity.this, Login_Activity.class));
                            finish();
                        } else {
                            Toast.makeText(Signin_Activity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Toast.makeText(Signin_Activity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        txtLogin.setOnClickListener(view -> {
            startActivity(new Intent(this, Login_Activity.class));
            finish();
        });
    }

    private boolean validateInputs() {
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải từ 6 ký tự");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không trùng khớp");
            return false;
        }

        return true;
    }

    private void sendOtpToEmail(String email) {
        apiService.sendOtp(new OtpRequest(email)).enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentOtpCode = response.body().getOtp();
                    Toast.makeText(Signin_Activity.this, "OTP đã gửi về email!", Toast.LENGTH_SHORT).show();
                    showOtpDialog(email);
                } else {
                    Toast.makeText(Signin_Activity.this, "Không thể gửi OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                Toast.makeText(Signin_Activity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.diglog_verification_otp_email, null);
        builder.setView(view);
        builder.setCancelable(false);

        TextView txtTitle = view.findViewById(R.id.tvTitle);
        TextInputEditText edtOtp = view.findViewById(R.id.edtOtp);
        Button btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);
        TextView txtResendOtp = view.findViewById(R.id.txtResendOtp);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnConfirm = view.findViewById(R.id.btnConfirm);

        txtTitle.setText("Mã OTP đã gửi tới email:\n" + email);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                edtOtp.setError("Vui lòng nhập mã OTP");
            } else if (otp.equals(currentOtpCode)) {
                isOtpVerified = true;
                verifiedEmail = email;
                Toast.makeText(this, "Xác minh OTP thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                edtOtp.setError("Mã OTP không đúng");
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (isOtpVerified && email.equals(verifiedEmail)) {
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Vui lòng nhập đúng mã OTP", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        txtResendOtp.setOnClickListener(v -> {
            sendOtpToEmail(email);
            Toast.makeText(this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
        });
    }

    private void registerUserToServer(String email, String password) {
        apiService.registerUser(new UserRequest(email, password)).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(Signin_Activity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Signin_Activity.this, Login_Activity.class));
                    finish();
                } else {
                    Toast.makeText(Signin_Activity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(Signin_Activity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}