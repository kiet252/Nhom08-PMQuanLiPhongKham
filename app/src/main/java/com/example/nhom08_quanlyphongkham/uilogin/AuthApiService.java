package com.example.nhom08_quanlyphongkham.uilogin;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApiService {
    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(
            @Header("apikey") String apiKey,
            @Body LoginRequest request
    );

    @PUT("auth/v1/user")
    Call<LoginResponse> updatePassword(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Body UpdatePasswordRequest request
    );
}