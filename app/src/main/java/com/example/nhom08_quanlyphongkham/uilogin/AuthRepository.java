package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;
    private final String apiKey;

    public AuthRepository(Context context, String apiKey) {
        this.apiKey = apiKey;
        authApiService = SupabaseClientProvider
                .getClient(context)
                .create(AuthApiService.class);
    }

    public Call<LoginResponse> login(String email, String password) {
        return authApiService.login(apiKey, new LoginRequest(email, password));
    }
    public Call<LoginResponse> refreshToken(String refreshToken) {
        return authApiService.refreshToken(apiKey, new RefreshTokenRequest(refreshToken));
    }
    public Call<LoginResponse> updatePassword(String newPassword) {
        return authApiService.updatePassword(
                apiKey,
                new UpdatePasswordRequest(newPassword)
        );
    }
}
