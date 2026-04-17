package com.example.nhom08_quanlyphongkham.uilogin;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ProfileApiService {
    @GET("rest/v1/profiles")
    Call<List<UserProfile>> getProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("id") String id,
            @Query("select") String select
    );
    @GET("rest/v1/profiles")
    Call<List<UserProfile>> getDoctors(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("chuc_vu") String chucVu,
            @Query("select") String select
    );
}
