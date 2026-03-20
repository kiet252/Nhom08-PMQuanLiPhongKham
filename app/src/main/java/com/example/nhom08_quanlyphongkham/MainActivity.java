package com.example.nhom08_quanlyphongkham;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.uilogin.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    MaterialButton buttonLogin;

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

        InitializeComponents();
        setListenersToComponents();
    }

    protected void InitializeComponents() {
        editTextEmail = findViewById(R.id.edtEmail);
        editTextPassword = findViewById(R.id.edtPassword);
        buttonLogin = findViewById(R.id.btnLogin);
    }

    protected void setListenersToComponents() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
                String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

                if (IsValidLoginDetails(email, password))
                    loginInitialize(email, password);
            }
        });
    }

    protected boolean IsValidLoginDetails(String email, String password) {
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

    protected void loginInitialize(String username, String password) {
        AuthRepository authRepository = new AuthRepository();

        authRepository.login(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}