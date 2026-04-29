package com.example.nhom08_quanlyphongkham.uilogin;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ProfileApiService {
    @GET("rest/v1/profiles")
    Call<List<UserProfile>> getProfile(
            @Query("id") String id,
            @Query("select") String select
    );

    @GET("rest/v1/profiles")
    Call<List<UserProfile>> getListProfiles(
            @Query("chuc_vu") String role, // Tham số này sẽ truyền "neq.Quản trị viên"
            @Query("select") String select
    );

    @PATCH("rest/v1/profiles")
    Call<ResponseBody> updateProfile(
            @Header("Authorization") String token,
            @Header("Prefer") String prefer,
            @Query("id") String filter,
            @Body Map<String, Object> updates
    );
    @GET("rest/v1/profiles")
    Call<List<UserProfile>> getDoctors(
            @Query("chuc_vu") String chucVu,
            @Query("select") String select
    );
}
