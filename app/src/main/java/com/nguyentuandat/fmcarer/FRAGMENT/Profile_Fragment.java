package com.nguyentuandat.fmcarer.FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;

public class Profile_Fragment extends Fragment {
    private TextView btnLogout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
    // ánh  xạ
        btnLogout = view.findViewById(R.id.btnLogout);
        // Thiết lập sự kiện click cho nút đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển về Login_Activity khi nhấn nút Đăng xuất
                Intent intent = new Intent(Profile_Fragment.this.getActivity(), Login_Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });






        return view;


    }
}
