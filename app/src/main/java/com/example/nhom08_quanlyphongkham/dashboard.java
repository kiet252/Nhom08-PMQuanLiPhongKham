package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.uilogin.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dashboard_fragment.HomeFragment_admin;

public class dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getIntent().getStringExtra("accessToken");
        if(token == null || token.isEmpty()){
            goToLogin();
            return;
        }

        setContentView(R.layout.dashboard);
        HomeFragment_admin homeFragmentAdmin = new HomeFragment_admin();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragmentAdmin).commit();
        UserProfile profile = (UserProfile) getIntent().getSerializableExtra("Userprofile");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // Đây là cách xác định nút nào đang được chạm vào

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment_admin();
            } else if (itemId == R.id.nav_notifications) { // Đổi từ doctor sang notification ở đây
                selectedFragment = new NotificationFragment();
            } else if (itemId == R.id.nav_account) {
                selectedFragment = AccountFragment.newInstance(profile);
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
