package dashboard_fragment.add_update_patient.add_patient_logic;

import java.util.Date;

public class CreatePatientRequest {
    private String ho_ten;
    private Date ngay_sinh;
    private String gioi_tinh;
    private String dia_chi;
    private String cccd;
    private String so_dien_thoai;

    public CreatePatientRequest(String hoTen, Date ngaySinh, String gioiTinh, String diaChi, String cccd, String sdt) {
        this.ho_ten = hoTen;
        this.ngay_sinh = ngaySinh;
        this.gioi_tinh = gioiTinh;
        this.dia_chi = diaChi;
        this.cccd = cccd;
        this.so_dien_thoai = sdt;
    }
}