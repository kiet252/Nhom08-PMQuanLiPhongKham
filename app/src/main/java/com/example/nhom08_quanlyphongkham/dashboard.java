package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dashboard_fragment.AccountFragment;
import dashboard_fragment.HomeFragment_admin;
import dashboard_fragment.HomeFragment_doctor;
import dashboard_fragment.HomeFragment_staff;
import dashboard_fragment.NotificationFragment;

public class dashboard extends AppCompatActivity {
    private String currentToken;
    UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentToken = getIntent().getStringExtra("accessToken");
        profile = (UserProfile) getIntent().getSerializableExtra("Userprofile");

        if(currentToken == null || currentToken.isEmpty()){
            goToLogin();
            return;
        }

        setContentView(R.layout.dashboard);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        Fragment userFragment;
        if ("Quản trị viên".equals(profile.getChuc_vu()))
            userFragment = new HomeFragment_admin();
        else if ("Bác sĩ".equals(profile.getChuc_vu()))
            userFragment = new HomeFragment_doctor();
        else
            userFragment = HomeFragment_staff.newInstance(currentToken);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, userFragment)
                .commit();
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // Đây là cách xác định nút nào đang được chạm vào

            if (itemId == R.id.nav_home) {
                selectedFragment = userFragment;
            } else if (itemId == R.id.nav_notifications) { // Đổi từ doctor sang notification ở đây
                selectedFragment = new NotificationFragment();
            } else if (itemId == R.id.nav_account) {
                selectedFragment = AccountFragment.newInstance(profile, currentToken);
            }

            // Thực hiện tráo đổi mảnh ghép vào FrameLayout
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true; // Trả về true để icon sáng lên
        });

    }
    private void goToLogin() {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        finish();
    }
}
