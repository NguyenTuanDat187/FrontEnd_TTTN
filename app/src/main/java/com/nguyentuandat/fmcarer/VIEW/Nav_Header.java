package com.nguyentuandat.fmcarer.VIEW;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.nguyentuandat.fmcarer.R;

public class Nav_Header extends Fragment {
    private ImageView imgAvatar;
    private TextView txtUsername, txtEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nav_header, container, false);

        // Ánh xạ
        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtEmail = view.findViewById(R.id.txtEmail);

        // Lấy dữ liệu từ SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String fullname = sharedPreferences.getString("fullname", "Tên chưa cập nhật");
        String email = sharedPreferences.getString("email", "Email chưa cập nhật");
        String avatarUrl = sharedPreferences.getString("image", ""); // link ảnh nếu có

        // Set dữ liệu lên View
        txtUsername.setText(fullname);
        txtEmail.setText(email);

        // Load ảnh bằng Glide (hoặc Picasso)
        if (!avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.taikhoan)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imgAvatar);
        }

        return view;
    }
}
