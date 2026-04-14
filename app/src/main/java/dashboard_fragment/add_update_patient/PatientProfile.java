package dashboard_fragment.add_update_patient;

import java.util.Date;

public class PatientProfile {
    private String id;
    private Date created_at;
    private String ho_ten;
    private Date ngay_sinh;
    private String gioi_tinh;
    private String dia_chi;
    private String cccd;
    private String so_dien_thoai;

    public PatientProfile(String id, Date createdAt, String hoTen, Date ngaySinh, String gioiTinh, String diaChi) {
        this.id = id;
        created_at = createdAt;
        ho_ten = hoTen;
        ngay_sinh = ngaySinh;
        gioi_tinh = gioiTinh;
        dia_chi = diaChi;
    }

    public String getId() {
        return id;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public String getHo_ten() {
        return ho_ten;
    }

    public Date getNgay_sinh() {
        return ngay_sinh;
    }

    public String getGioi_tinh() {
        return gioi_tinh;
    }

    public String getDia_chi() {
        return dia_chi;
    }

    public String getCccd() {
        return cccd;
    }
}
