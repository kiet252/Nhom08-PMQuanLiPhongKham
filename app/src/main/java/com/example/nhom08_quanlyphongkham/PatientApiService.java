package com.example.nhom08_quanlyphongkham;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
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