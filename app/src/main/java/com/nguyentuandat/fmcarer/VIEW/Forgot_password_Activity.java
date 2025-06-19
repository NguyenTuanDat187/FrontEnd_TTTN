package com.nguyentuandat.fmcarer.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nguyentuandat.fmcarer.R;

public class Forgot_password_Activity extends AppCompatActivity {
    private TextView txtBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_password_activity);
        // ánh xạ
        txtBackToLogin = findViewById(R.id.txtBackToLogin);

        // xử lý sự kiện click
        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Forgot_password_Activity.this, Login_Activity.class);
            startActivity(intent);
            finish(); // Kết thúc Forgot_password_Activity
        });

    }
}