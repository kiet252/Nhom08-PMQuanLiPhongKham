package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.LoginResponse;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.example.nhom08_quanlyphongkham.uilogin.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class login extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;

    private AuthRepository authRepository;
    private ProfileRepository profileRepository;

    private String currentToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(getString(R.string.abAIkey));
        profileRepository = new ProfileRepository(getString(R.string.abAIkey));

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.edtEmail);
        editTextPassword = findViewById(R.id.edtPassword);
        buttonLogin = findViewById(R.id.btnLogin);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

        if (!isValidLoginDetails(email, password)) {
            return;
        }

        authRepository.login(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(login.this, "Login failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                handleLoginSuccess(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(login.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidLoginDetails(String email, String password) {
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            return false;
        }

        return true;
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        String accessToken = loginResponse.getAccess_token();
        this.currentToken = accessToken;
        String userId = loginResponse.getUser().getId();

        fetchUserProfile(accessToken, userId);
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
    }

    private void fetchUserProfile(String accessToken, String userId) {
        profileRepository.getProfile(accessToken, userId).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfile>> call, @NonNull Response<List<UserProfile>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(login.this, "Could not load user profile", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserProfile profile = response.body().get(0);
                showWelcomeMessage(profile);
                Intent logined = new Intent(login.this, dashboard.class);
                logined.putExtra("accessToken", profile.getId());
                startActivity(logined);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfile>> call, @NonNull Throwable t) {
                Toast.makeText(login.this, "Profile error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void showWelcomeMessage(UserProfile profile) {
        Toast.makeText(this, "Welcome " + profile.getHo_ten(), Toast.LENGTH_SHORT).show();
    }
}
