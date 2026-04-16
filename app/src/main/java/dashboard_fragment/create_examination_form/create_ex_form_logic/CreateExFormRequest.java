package dashboard_fragment.create_examination_form.create_ex_form_logic;

import java.sql.Time;
import java.util.Date;

public class CreateExFormRequest {
    private Date ngay_kham;
    private Time gio_du_kien;
    private int so_thu_tu_trong_ngay;
    private String trieu_chung_ban_dau;
    private int phi_kham;
    private String trang_thai;
    private String phuong_thuc_thanh_toan;
    public CreateExFormRequest(Date ngayKham, Time gioDuKien, int soThuTuTrongNgay, String trieuChungBanDau, int phiKham, String trangThai, String phuongThucThanhToan) {
        this.ngay_kham = ngayKham;
        this.gio_du_kien = gioDuKien;
        this.so_thu_tu_trong_ngay = soThuTuTrongNgay;
        this.trieu_chung_ban_dau = trieuChungBanDau;
        this.phi_kham = phiKham;
        this.trang_thai = trangThai;
        this.phuong_thuc_thanh_toan = phuongThucThanhToan;
    }
}
