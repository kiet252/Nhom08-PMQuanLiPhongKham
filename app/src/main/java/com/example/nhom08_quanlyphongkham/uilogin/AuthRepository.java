package com.example.nhom08_quanlyphongkham.uilogin;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;
    private final String apiKey;

    public AuthRepository(String apiKey) {
        this.apiKey = apiKey;
        authApiService = SupabaseClientProvider
                .getClient()
                .create(AuthApiService.class);
    }

    public Call<LoginResponse> login(String email, String password) {
        return authApiService.login(apiKey, new LoginRequest(email, password));
    }

    public Call<LoginResponse> updatePassword(String accessToken, String newPassword) {
        return authApiService.updatePassword(
                apiKey,
                "Bearer " + accessToken,
                new UpdatePasswordRequest(newPassword)
        );
    }
}
