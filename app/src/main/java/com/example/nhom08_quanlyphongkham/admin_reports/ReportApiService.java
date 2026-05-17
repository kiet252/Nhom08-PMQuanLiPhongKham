package com.example.nhom08_quanlyphongkham.admin_reports;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ReportApiService {
    /**
     * Lấy danh sách phiếu khám từ Supabase.
     * select: Lấy các cột id, ngay_kham, trang_thai, phi_kham và join bảng patient để lấy ho_ten gán vào patient_name.
     * order: Sắp xếp theo ngày khám giảm dần (mới nhất lên đầu).
     */
    @GET("rest/v1/examination_form?select=id,ngay_kham,trang_thai,phi_kham,patient_name:patient(ho_ten)&order=ngay_kham.desc")
    Call<List<ReportItem>> getDanhSachDonKham();
}
