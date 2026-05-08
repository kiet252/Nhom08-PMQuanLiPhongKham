package com.example.nhom08_quanlyphongkham;

import com.example.nhom08_quanlyphongkham.uilogin.LoginRequest;
import com.example.nhom08_quanlyphongkham.uilogin.LoginResponse;
import com.example.nhom08_quanlyphongkham.uilogin.RefreshTokenRequest;

import java.util.List;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface PatientApiService {
        @GET("rest/v1/examination_form")
        Call<List<CountResponse>> getTodayCount(
                @Query("ngay_kham") String dateFilter, // Dùng gte.YYYY-MM-DD
                @Query("select") String select         // Truyền vào "count"
        );
    @GET("rest/v1/examination_form")
    Call<List<CountResponse>> getTodayWaitCount(
            @Query("ngay_kham") String dateFilter, // Dùng gte.YYYY-MM-DD
            @Query("trang_thai") String trangThai,
            @Query("select") String select         // Truyền vào "count"
    );


    }