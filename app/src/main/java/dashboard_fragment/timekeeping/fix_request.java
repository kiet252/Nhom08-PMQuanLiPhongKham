package dashboard_fragment.timekeeping;

import java.util.List;

public class fix_request {

    // Các thuộc tính
    private String staff_id;           // Kiểu uuid thường map với String
    private String id_ca_lam;            // Kiểu bigint map với Long
    private String li_do;              // Kiểu text map với String
    private String thoi_gian_vao_ca;   // Timestamp có thể dùng String (hoặc java.util.Date)
    private String thoi_gian_ra_ca;    // Timestamp có thể dùng String (hoặc java.util.Date)
    private List<String> minh_chung;         // Kiểu jsonb có thể để Object, String, hoặc 1 class Model tuỳ cách bạn parse

    public fix_request() {
    }

    // Getter và Setter
    public String getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(String staff_id) {
        this.staff_id = staff_id;
    }

    public String getId_ca_lam() {
        return id_ca_lam;
    }

    public void setId_ca_lam(String id_ca_lam) {
        this.id_ca_lam = id_ca_lam;
    }

    public String getLi_do() {
        return li_do;
    }

    public void setLi_do(String li_do) {
        this.li_do = li_do;
    }

    public String getThoi_gian_vao_ca() {
        return thoi_gian_vao_ca;
    }

    public void setThoi_gian_vao_ca(String thoi_gian_vao_ca) {
        this.thoi_gian_vao_ca = thoi_gian_vao_ca;
    }

    public String getThoi_gian_ra_ca() {
        return thoi_gian_ra_ca;
    }

    public void setThoi_gian_ra_ca(String thoi_gian_ra_ca) {
        this.thoi_gian_ra_ca = thoi_gian_ra_ca;
    }

    public Object getMinh_chung() {
        return minh_chung;
    }

    public void setMinh_chung(List<String> minh_chung) {
        this.minh_chung = minh_chung;
    }
}