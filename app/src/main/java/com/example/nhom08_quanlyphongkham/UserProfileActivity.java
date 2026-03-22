package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.uilogin.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    View rootView, scrollView, bottomNavigation;
    TextView textviewProfile, textviewEmail, textviewPhone, textviewBirthday, textviewGender, textviewAddress, textviewJobTitle;
    static String EXTRA_USER_PROFILE;
    UserProfile userprofile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_profile);

        initializeViews();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            scrollView.setPadding(0, 0, 0, 0);
            bottomNavigation.setPadding(
                    bottomNavigation.getPaddingLeft(),
                    bottomNavigation.getPaddingTop(),
                    bottomNavigation.getPaddingRight(),
                    systemBars.bottom
            );

            return insets;
        });

        decomposeUserdata();
        setViewProfile();
    }
    private void initializeViews() {
        rootView = findViewById(R.id.user_profile);
        scrollView = findViewById(R.id.profile_scroll);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        textviewProfile = findViewById(R.id.txtProfileName);
        textviewEmail = findViewById(R.id.txtEmail);
        textviewPhone = findViewById(R.id.txtPhone);
        textviewBirthday = findViewById(R.id.txtBirthday);
        textviewGender = findViewById(R.id.txtGender);
        textviewAddress = findViewById(R.id.txtAddress);
        textviewJobTitle = findViewById(R.id.txtJobTitle);
    }

    private void decomposeUserdata() {
        userprofile = (UserProfile) getIntent().getSerializableExtra(EXTRA_USER_PROFILE);
    }

    private void setViewProfile() {
        String GreetingText = "Xin chào, " + userprofile.getHo_ten();

        textviewProfile.setText(GreetingText);

        textviewEmail.setText(userprofile.getEmail());
        textviewPhone.setText(userprofile.getSo_dien_thoai());

        Date NgaySinhUser = userprofile.getNgay_sinh();

        if (NgaySinhUser != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            textviewBirthday.setText(sdf.format(NgaySinhUser));
        }

        textviewGender.setText(userprofile.getGioitinh());
        textviewAddress.setText(userprofile.getDia_chi());
        textviewJobTitle.setText(userprofile.getChuc_vu());
    }
}
