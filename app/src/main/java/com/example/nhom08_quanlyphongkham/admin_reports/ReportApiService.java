package com.example.nhom08_quanlyphongkham.admin_reports;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ReportApiService
{
    /**
     * Lấy danh sách phiếu khám từ Supabase.
     * Join bảng patient để lấy id, cccd và ho_ten phục vụ tìm kiếm.
     */
    @GET("rest/v1/examination_form?select=id,ngay_kham,trang_thai,phi_kham,patient_name:patient(id,cccd,ho_ten),medical_record(bill(tong_thanh_toan))&order=ngay_kham.desc")
    Call<List<ReportItem>> getDanhSachDonKham();

    @GET("rest/v1/examination_form?select=id,ngay_kham,patient_name:patient(id,cccd,ho_ten),medical_record!inner(bill!inner(id,created_at,phuong_thuc_thanh_toan,trang_thai_thanh_toan,tong_thanh_toan))&order=ngay_kham.desc")
    Call<List<ReportItem>> getDanhSachBill();
}
