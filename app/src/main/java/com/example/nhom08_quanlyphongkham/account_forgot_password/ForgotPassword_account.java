package com.example.nhom08_quanlyphongkham.account_forgot_password;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.LoginResponse;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword_account extends AppCompatActivity {

    private ImageButton btnBackForgotPassword;
    private MaterialButton btnSendOtp;
    private MaterialButton btnVerifyOtp;
    private MaterialButton btnResetPassword;
    private ProgressBar progressSendOtp;
    private LinearLayout layoutResetAction;
    private TextInputEditText edtEmail;
    private TextInputEditText edtOtp;
    private TextInputEditText edtNewPassword;
    private TextInputEditText edtConfirmNewPassword;

    private TextInputLayout tilEmail;
    private TextInputLayout tilOtp;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmNewPassword;

    private LinearLayout layoutOtpSection;
    private LinearLayout layoutResetPasswordSection;

    private AuthRepository authRepository;
    private ProfileRepository profileRepository;

    private boolean isOtpVerified = false;
    private String verifiedEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_forgot_password), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);
        profileRepository = new ProfileRepository(this);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnBackForgotPassword = findViewById(R.id.btnBackForgotPassword);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressSendOtp = findViewById(R.id.progressSendOtp);
        layoutResetAction = findViewById(R.id.layoutResetAction);

        edtEmail = findViewById(R.id.edtForgotPasswordEmail);
        edtOtp = findViewById(R.id.edtForgotPasswordOtp);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);

        tilEmail = findViewById(R.id.tilEmail);
        tilOtp = findViewById(R.id.tilOtp);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = findViewById(R.id.tilConfirmNewPassword);

        layoutOtpSection = findViewById(R.id.layoutOtpSection);
        layoutResetPasswordSection = findViewById(R.id.layoutResetPasswordSection);
    }

    private void setupListeners() {
        btnBackForgotPassword.setOnClickListener(v -> finish());
        btnSendOtp.setOnClickListener(v -> handleSendOtp());
        btnVerifyOtp.setOnClickListener(v -> handleVerifyOtp());
        btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    private void handleSendOtp() {
        clearAllErrors();

        String email = getText(edtEmail);

        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        setSendOtpLoading(true);

        profileRepository.getProfileByEmail(email).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfile>> call, @NonNull Response<List<UserProfile>> response) {
                if (!response.isSuccessful()) {
                    setSendOtpLoading(false);
                    Toast.makeText(ForgotPassword_account.this,
                            "Không kiểm tra được email trong dữ liệu",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {
                    setSendOtpLoading(false);
                    tilEmail.setError("Email không tồn tại trong dữ liệu");
                    return;
                }

                sendRecoveryOtp(email);
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfile>> call, @NonNull Throwable t) {
                setSendOtpLoading(false);
                Toast.makeText(ForgotPassword_account.this,
                        "Lỗi kết nối",
                        Toast.LENGTH_SHORT).show();
                Log.d("Connection Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void sendRecoveryOtp(String email) {
        authRepository.recoverPassword(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                setSendOtpLoading(false);

                if (!response.isSuccessful()) {
                    Toast.makeText(ForgotPassword_account.this,
                            "Không gửi được mã OTP tới email này",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                layoutOtpSection.setVisibility(View.VISIBLE);
                layoutResetPasswordSection.setVisibility(View.GONE);
                layoutResetAction.setVisibility(View.GONE);
                isOtpVerified = false;
                verifiedEmail = null;

                Toast.makeText(ForgotPassword_account.this,
                        "Đã gửi mã OTP đến email của bạn",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                setSendOtpLoading(false);
                Toast.makeText(ForgotPassword_account.this,
                        "Lỗi kết nối",
                        Toast.LENGTH_SHORT).show();
                Log.d("Connection Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void handleVerifyOtp() {
        clearAllErrors();

        String email = getText(edtEmail);
        String otp = getText(edtOtp);

        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        if (otp.isEmpty()) {
            tilOtp.setError("Vui lòng nhập mã OTP");
            return;
        }

        if (!otp.matches("\\d{6,8}")) {
            tilOtp.setError("Mã OTP không hợp lệ");
            return;
        }

        authRepository.verifyRecoveryOtp(email, otp).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tilOtp.setError("Mã OTP không đúng hoặc đã hết hạn");
                    return;
                }

                LoginResponse loginResponse = response.body();
                String accessToken = loginResponse.getAccess_token();
                String refreshToken = loginResponse.getRefresh_token();

                if (accessToken == null || accessToken.isEmpty()) {
                    Toast.makeText(ForgotPassword_account.this,
                            "Không lấy được phiên xác thực sau khi xác nhận OTP",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPrefManager.getInstance(ForgotPassword_account.this)
                        .saveTokens(accessToken, refreshToken);

                isOtpVerified = true;
                verifiedEmail = email;
                layoutResetPasswordSection.setVisibility(View.VISIBLE);
                layoutResetAction.setVisibility(View.VISIBLE);

                Toast.makeText(ForgotPassword_account.this,
                        "Xác nhận OTP thành công",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(ForgotPassword_account.this,
                        "Lỗi kết nối",
                        Toast.LENGTH_SHORT).show();
                Log.d("Connection Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void handleResetPassword() {
        clearAllErrors();

        if (!isOtpVerified || verifiedEmail == null) {
            Toast.makeText(this, "Bạn cần xác nhận OTP trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassword = getText(edtNewPassword);
        String confirmPassword = getText(edtConfirmNewPassword);

        if (newPassword.isEmpty()) {
            tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (newPassword.length() < 6) {
            tilNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu mới");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        authRepository.updatePassword(newPassword).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ForgotPassword_account.this,
                            "Đặt lại mật khẩu thất bại",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(ForgotPassword_account.this,
                        "Đặt lại mật khẩu thành công",
                        Toast.LENGTH_SHORT).show();

                SharedPrefManager.getInstance(ForgotPassword_account.this).clear();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(ForgotPassword_account.this,
                        "Lỗi kết nối",
                        Toast.LENGTH_SHORT).show();
                Log.d("Connection Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void setSendOtpLoading(boolean isLoading) {
        btnSendOtp.setEnabled(!isLoading);
        edtEmail.setEnabled(!isLoading);
        progressSendOtp.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendOtp.setText(isLoading ? "" : "Gửi mã");
    }

    private void clearAllErrors() {
        tilEmail.setError(null);
        tilOtp.setError(null);
        tilNewPassword.setError(null);
        tilConfirmNewPassword.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}