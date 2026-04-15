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

    public String getSo_dien_thoai() {
        return so_dien_thoai;
    }
}
