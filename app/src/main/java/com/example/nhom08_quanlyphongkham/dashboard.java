package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dashboard_fragment.AccountFragment;
import dashboard_fragment.HomeFragment_admin;
import dashboard_fragment.HomeFragment_doctor;
import dashboard_fragment.HomeFragment_staff;
import dashboard_fragment.NotificationFragment;

public class dashboard extends AppCompatActivity {
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        profile = prefManager.getProfile();
        String token = prefManager.getToken();

        if (profile == null || token == null) {
            goToLogin();
            return;
        }

        setContentView(R.layout.dashboard);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        Fragment homeFragment = getHomeFragmentByRole(profile.getChuc_vu());

        // Initial fragment load
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new NotificationFragment();
            } else if (itemId == R.id.nav_account) {
                UserProfile latestProfile = SharedPrefManager.getInstance(this).getProfile();
                if (latestProfile != null) {
                    profile = latestProfile;
                }
                selectedFragment = AccountFragment.newInstance(profile);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    private Fragment getHomeFragmentByRole(String role) {
        if ("Quản trị viên".equals(role)) return new HomeFragment_admin();
        if ("Bác sĩ".equals(role)) return new HomeFragment_doctor();
        return new HomeFragment_staff();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
