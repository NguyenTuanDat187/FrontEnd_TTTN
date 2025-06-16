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
import com.nguyentuandat.fmcarer.R;

public class Signin_Activity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword;
    private TextInputLayout layoutEmail;
    private Button btnRegister;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Ánh xạ view
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        layoutEmail = findViewById(R.id.layoutEmail);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        // Bấm vào icon để gửi OTP
        layoutEmail.setEndIconOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.setError("Email không hợp lệ");
                return;
            }

            layoutEmail.setError(null);
            showOtpDialog(email); // Hiển thị OTP dialog
        });

        // Xoá lỗi nếu người dùng nhập lại đúng email
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    layoutEmail.setError(null);
                }
            }
        });

        // Chuyển màn login
        btnRegister.setOnClickListener(view -> {
            Intent    intent = new Intent(Signin_Activity.this, Login_Activity.class);

            startActivity(intent);
            Toast.makeText(this, "Đăng ký thành công (mock)", Toast.LENGTH_SHORT).show();
        });

        txtLogin.setOnClickListener(view -> {
            Toast.makeText(this, "Bạn đã có tài khoản? Đăng nhập", Toast.LENGTH_SHORT).show();
        });
    }

    // Dialog nhập mã OTP
    private void showOtpDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.diglog_verification_otp_email, null);
        builder.setView(view);
        builder.setCancelable(false);

        TextView txtTitle = view.findViewById(R.id.tvTitle);
        TextInputEditText edtOtp = view.findViewById(R.id.edtOtp);
        Button btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);
        TextView txtResendOtp = view.findViewById(R.id.txtResendOtp);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnConfirm = view.findViewById(R.id.btnConfirm);

        txtTitle.setText("Mã OTP đã được gửi tới email:\n" + email);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // bo viền
        dialog.show();

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                edtOtp.setError("Vui lòng nhập mã OTP");
                return;
            }

            // Xử lý xác thực OTP ở đây
            Toast.makeText(this, "Xác minh OTP: " + otp, Toast.LENGTH_SHORT).show();
        });

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Xác nhận", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        txtResendOtp.setOnClickListener(v -> {
            Toast.makeText(this, "Gửi lại mã OTP (demo)", Toast.LENGTH_SHORT).show();
        });
    }
}
