package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.R;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context;
        this.authApiService = SupabaseClientProvider
                .getClient(context)
                .create(AuthApiService.class);
    }

    public Call<LoginResponse> login(String email, String password) {
        return authApiService.login(new LoginRequest(email, password));
    }

    public Call<LoginResponse> refreshToken(String refreshToken) {
        return authApiService.refreshToken(
                context.getString(R.string.abAIkey),
                new RefreshTokenRequest(refreshToken)
        );
    }

    public Call<LoginResponse> updatePassword(String newPassword) {
        return authApiService.updatePassword(new UpdatePasswordRequest(newPassword));
    }
}
