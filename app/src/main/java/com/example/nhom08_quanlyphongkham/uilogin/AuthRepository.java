package com.example.nhom08_quanlyphongkham.uilogin;

import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;

    public AuthRepository() {
        authApiService = SupabaseClientProvider
                .getClient()
                .create(AuthApiService.class);
    }

    public Call<LoginResponse> login(String email, String password) {
        return authApiService.login(new LoginRequest(email, password));
    }
}