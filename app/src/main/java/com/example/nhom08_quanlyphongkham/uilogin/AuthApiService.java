package com.example.nhom08_quanlyphongkham.uilogin;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApiService {
    @Headers({
            "apikey: sb_publishable_-nIp-wgA-gM7IPTL6WOViw_LGXW4ZAa",
            "Content-Type: application/json"
    })
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(@Body LoginRequest request);
}
