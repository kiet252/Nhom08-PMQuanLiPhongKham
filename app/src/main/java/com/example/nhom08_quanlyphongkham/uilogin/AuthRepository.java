package com.example.nhom08_quanlyphongkham.uilogin;

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
}
