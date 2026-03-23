package com.example.nhom08_quanlyphongkham;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.uilogin.UserProfile;

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
        UserProfile profile = (UserProfile) getIntent().getSerializableExtra("name");
        TextView txtWelcome = findViewById(R.id.txtWelcome);

        if(profile != null) {
            String GreetingText = "Chào mừng, " + profile.getHo_ten();
            txtWelcome.setText(GreetingText);
        }
        else
        {
            String GreetingText = "Chào mừng, Guest";
            txtWelcome.setText(GreetingText);
        }

    }
    private void goToLogin() {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        finish();
    }
}
