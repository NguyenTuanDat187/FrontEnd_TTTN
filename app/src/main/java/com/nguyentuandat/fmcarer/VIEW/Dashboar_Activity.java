package com.nguyentuandat.fmcarer.VIEW;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.nguyentuandat.fmcarer.FRAGMENT.Account_Sub_Create_Fragment;
import com.nguyentuandat.fmcarer.FRAGMENT.Care_schedule_Fragment;
import com.nguyentuandat.fmcarer.FRAGMENT.Home_Fragment;
import com.nguyentuandat.fmcarer.FRAGMENT.Profile_Fragment;
import com.nguyentuandat.fmcarer.FRAGMENT.Top_Up_Fragment;
import com.nguyentuandat.fmcarer.R;

public class Dashboar_Activity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    private FrameLayout fragmentContainer;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboar);

        // ánh xạ
        fragmentContainer = findViewById(R.id.fragmentContainer);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNav = findViewById(R.id.bottomNav);

        // Toolbar thay thế ActionBar
        setSupportActionBar(toolbar);

        // Tạo toggle (icon menu)
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        // Thiết lập BottomNavigationView
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Thay đổi fragment dựa trên menu được chọn
                // Trang chủ
                if (id == R.id.nav_home) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Home_Fragment())
                            .commit();
                }
                // Tạo tài khoản phụ
                else if (id == R.id.account_sub_create) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Account_Sub_Create_Fragment())
                            .commit();
                }
                // Lịch chăm sóc

                else if (id == R.id.nav_schedule) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Care_schedule_Fragment())
                            .commit();
                }
                // Hồ sơ
                else if (id == R.id.nav_profile) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Profile_Fragment())
                            .commit();
                }

                return true;
            }
        });
        // Xử lý khi chọn menu trong NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Thay đổi fragment dựa trên menu được chọn
                if (id == R.id.nav_top_up) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Top_Up_Fragment())
                            .commit();
                }
                // Fragment Lịch chăm sóc
                else if (id == R.id.nav_schedule) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Care_schedule_Fragment())
                            .commit();
                }
                // Fragment Trang chủ
                else if (id == R.id.nav_home) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Home_Fragment())
                            .commit();
                }
                // Fragment Tạo tài khoản phụ
                 else if (id == R.id.account_sub_create) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Account_Sub_Create_Fragment())
                            .commit();
                }
                 // Fragment Hồ sơ
                 else if (id == R.id.nav_account) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new Profile_Fragment())
                            .commit();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new Home_Fragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_home); // chọn mặc định trang chủ
        }

    }


    @Override
    public void onBackPressed() {
        // Đóng menu nếu đang mở
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
