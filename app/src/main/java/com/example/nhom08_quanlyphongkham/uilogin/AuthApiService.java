package com.example.nhom08_quanlyphongkham.uilogin;

import dashboard_fragment.account_change_password_request.UpdatePasswordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.GET;
import retrofit2.http.DELETE;
import retrofit2.http.Query;

public interface AuthApiService {
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(
            @Header("apikey") String apiKey,
            @Body LoginRequest request
    );
    Call<LoginResponse> login(@Body LoginRequest request);

    @PUT("auth/v1/user")
    Call<LoginResponse> updatePassword(@Body UpdatePasswordRequest request);
    
    @POST("auth/v1/token?grant_type=refresh_token")
    Call<LoginResponse> refreshToken(
            @Header("apikey") String apiKey,
            @Body RefreshTokenRequest request
    );
    @GET("rest/v1/thong_bao?select=*&order=id.asc")
    Call<java.util.List<ThongBao>> layDanhSachThongBao(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );
    @POST("rest/v1/thong_bao")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<java.util.List<ThongBao>> themThongBao(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Body ThongBao thongBao
    );
    @GET("rest/v1/thong_bao_fragment_admin?select=*&order=id.asc")
    Call<java.util.List<ThongBao>> layDanhSachThongBaoAdmin(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );
    @POST("rest/v1/thong_bao_fragment_admin")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<java.util.List<ThongBao>> themThongBaoAdmin(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Body ThongBao thongBao
    );
    @DELETE("rest/v1/thong_bao")
    Call<Void> xoaThongBaoAdmin(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("id") String queryId // id=eq.1
    );
    @DELETE("rest/v1/thong_bao_fragment_admin")
    Call<Void> xoaThongBao(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("id") String queryId // id=eq.1
    );

}