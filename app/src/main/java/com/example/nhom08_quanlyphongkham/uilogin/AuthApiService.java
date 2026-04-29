package com.example.nhom08_quanlyphongkham.uilogin;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApiService {
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(@Body LoginRequest request);

    @PUT("auth/v1/user")
    Call<LoginResponse> updatePassword(@Body UpdatePasswordRequest request);
    
    @POST("auth/v1/token?grant_type=refresh_token")
    Call<LoginResponse> refreshToken(
            @Header("apikey") String apiKey,
            @Body RefreshTokenRequest request
    );

}