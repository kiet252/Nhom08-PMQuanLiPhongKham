package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dashboard_fragment.AccountFragment;
import dashboard_fragment.HomeFragment_admin;
import dashboard_fragment.HomeFragment_doctor;
import dashboard_fragment.HomeFragment_staff;
import dashboard_fragment.NotificationFragment;

public class dashboard extends BaseActivity {

    private static final String KEY_SELECTED_NAV = "key_selected_nav";
    private static final String TAG_HOME = "tag_home";
    private static final String TAG_NOTIFICATIONS = "tag_notifications";
    private static final String TAG_ACCOUNT = "tag_account";

    private UserProfile profile;
    private Fragment homeFragment;
    private Fragment notificationFragment;
    private Fragment accountFragment;
    private Fragment activeFragment;
    private int selectedNavId = R.id.nav_home;

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

        if (savedInstanceState != null) {
            selectedNavId = savedInstanceState.getInt(KEY_SELECTED_NAV, R.id.nav_home);
        }

        setContentView(R.layout.user_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        initFragments();
        restoreOrAddFragments(savedInstanceState);

        bottomNav.setSelectedItemId(selectedNavId);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_account) {
                UserProfile latestProfile = SharedPrefManager.getInstance(this).getProfile();
                if (latestProfile != null) {
                    profile = latestProfile;
                }
            }

            if (itemId != selectedNavId) {
                showFragment(itemId);
                selectedNavId = itemId;
            }

            return true;
        });
    }

    private void initFragments() {
        homeFragment = getHomeFragmentByRole(profile.getChuc_vu());
        notificationFragment = new NotificationFragment();
        accountFragment = AccountFragment.newInstance(profile);
    }

    private void restoreOrAddFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.fragment_container, homeFragment, TAG_HOME)
                    .add(R.id.fragment_container, notificationFragment, TAG_NOTIFICATIONS).hide(notificationFragment)
                    .add(R.id.fragment_container, accountFragment, TAG_ACCOUNT).hide(accountFragment)
                    .commit();

            activeFragment = homeFragment;
            return;
        }

        homeFragment = findFragmentOrFallback(TAG_HOME, homeFragment);
        notificationFragment = findFragmentOrFallback(TAG_NOTIFICATIONS, notificationFragment);
        accountFragment = findFragmentOrFallback(TAG_ACCOUNT, accountFragment);

        activeFragment = getFragmentByNavId(selectedNavId);
        if (activeFragment == null) {
            activeFragment = homeFragment;
            selectedNavId = R.id.nav_home;
        }
    }

    private Fragment findFragmentOrFallback(String tag, Fragment fallback) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null ? fragment : fallback;
    }

    private Fragment getFragmentByNavId(int navId) {
        if (navId == R.id.nav_home) return homeFragment;
        if (navId == R.id.nav_notifications) return notificationFragment;
        if (navId == R.id.nav_account) return accountFragment;
        return null;
    }

    private void showFragment(int navId) {
        Fragment target = getFragmentByNavId(navId);
        if (target == null || target == activeFragment) {
            return;
        }

        if (!target.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, target)
                    .hide(activeFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
        }

        activeFragment = target;
    }

    private Fragment getHomeFragmentByRole(String role) {
        if ("Quản trị viên".equals(role)) return new HomeFragment_admin();
        if ("Bác sĩ".equals(role)) return new HomeFragment_doctor();
        return new HomeFragment_staff();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_NAV, selectedNavId);
        super.onSaveInstanceState(outState);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}