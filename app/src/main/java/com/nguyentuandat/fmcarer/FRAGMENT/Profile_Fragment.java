package com.nguyentuandat.fmcarer.FRAGMENT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;

public class Profile_Fragment extends Fragment {

    private TextView btnLogout, btnEditProfile;
    private TextView textUserName, textEmail, textPhone, textSubEmail, textSubPhone;
    private ImageView imageAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        // 🌟 Ánh xạ view
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        imageAvatar = view.findViewById(R.id.imageAvatar);
        textUserName = view.findViewById(R.id.textUserName);
        textEmail = view.findViewById(R.id.textEmail);
        textPhone = view.findViewById(R.id.textPhone);
        textSubEmail = view.findViewById(R.id.textSubEmail);
        textSubPhone = view.findViewById(R.id.textSubPhone);

        // 🧠 Lấy dữ liệu từ SharedPreferences (đã lưu sau khi đăng nhập hoặc cập nhật)
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", getContext().MODE_PRIVATE);
        String fullname = prefs.getString("fullname", "Tên chưa cập nhật");
        String email = prefs.getString("email", "Email chưa cập nhật");
        String phone = prefs.getString("numberphone", "SĐT chưa cập nhật");
        String image = prefs.getString("image", "");

        // 🖼️ Gán avatar (nếu có)

        Glide.with(this).load(Uri.parse(image)).into(imageAvatar);

        if (image != null && !image.isEmpty()) {
            try {
                Uri uri = Uri.parse(image);
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.taikhoan)
                        .error(R.drawable.taikhoan)
                        .into(imageAvatar);
            } catch (Exception e) {
                imageAvatar.setImageResource(R.drawable.taikhoan);
            }
        } else {
            imageAvatar.setImageResource(R.drawable.taikhoan);
        }


        // 📝 Gán dữ liệu vào TextView
        textUserName.setText(fullname);
        textEmail.setText("Email: " + email);
        textPhone.setText("SĐT: " + phone);

        // ⚠️ Nếu có tài khoản phụ, bạn tự động thêm ở đây. Nếu chưa thì gán mặc định:
        textSubEmail.setText("Email phụ: phu@example.com");
        textSubPhone.setText("SĐT phụ: 0123456789");

        // 👋 Xử lý logout
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Xóa toàn bộ thông tin người dùng khi logout
            editor.apply();

            Intent intent = new Intent(requireActivity(), Login_Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // 🛠️ Sự kiện chỉnh sửa (nếu cần)
        btnEditProfile.setOnClickListener(v -> {
            // Có thể show dialog hoặc chuyển sang Activity chỉnh sửa
            // TODO: Tùy bạn xử lý
        });

        return view;
    }
}
