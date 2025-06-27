package com.nguyentuandat.fmcarer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.nguyentuandat.fmcarer.VIEW.Dashboar_Activity;
import com.nguyentuandat.fmcarer.VIEW.Login_Activity;
import com.nguyentuandat.fmcarer.VIEW.Signin_Activity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Login_Activity.class);
                startActivity(intent);
            }
        }, 1000);
    }

}