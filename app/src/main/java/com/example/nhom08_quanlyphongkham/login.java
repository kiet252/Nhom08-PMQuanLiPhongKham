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
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
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
        // here
        currentToken = SharedPrefManager.getInstance(this).getToken();
        if (currentToken != null && !currentToken.isEmpty() && SharedPrefManager.getInstance(this).getProfile() != null && !SharedPrefManager.getInstance(this).getProfile().getID().isEmpty())
        {
            // Nếu có, tạo Intent để đi thẳng tới màn hình Dashboard
            Intent intent = new Intent(this, dashboard.class);
            intent.putExtra("accessToken", currentToken);
            intent.putExtra("Userprofile", SharedPrefManager.getInstance(this).getProfile());

            startActivity(intent);

            // Quan trọng: Kết thúc Activity Login để người dùng không quay lại được bằng nút Back
            finish();
            return; // Dừng hàm onCreate tại đây, không chạy code bên dưới nữa
        }
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
                    Toast.makeText(login.this,"Đăng nhập thất bại! Sai mật khẩu hoặc tên người dùng", Toast.LENGTH_SHORT).show();
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
            editTextEmail.setError("Xin hãy nhập email!");
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Xin hãy nhập mật khẩu!");
            return false;
        }

        return true;
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        currentToken = loginResponse.getAccess_token();
        if (currentToken == null) {
            Toast.makeText(this, "Lỗi đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = loginResponse.getUser().getId();
        fetchUserProfile(userId);
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

    }

    private void fetchUserProfile(String userId) {
        profileRepository.getProfile(currentToken, userId).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfile>> call, @NonNull Response<List<UserProfile>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(login.this, "Không tìm được người dùng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserProfile profile = response.body().get(0);
                Intent logined = new Intent(login.this, dashboard.class);

                logined.putExtra("accessToken", currentToken);
                logined.putExtra("Userprofile", profile);
                SharedPrefManager.getInstance(login.this).saveToken(currentToken);
                SharedPrefManager.getInstance(login.this).saveProfile(profile);
                startActivity(logined);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfile>> call, @NonNull Throwable t) {
                Toast.makeText(login.this, "Lỗi tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }
}
