package dashboard_fragment.create_examination_form;

import java.sql.Time;
import java.util.Date;
public class ExaminationForm {
    private String id;
    private Date created_at;
    private String patient_id;
    private String doctor_id;
    private Date ngay_kham;
    private Time gio_du_kien;
    private int so_thu_tu_trong_ngay;
    private String trieu_chung_ban_dau;
    private int phi_kham;
    private String trang_thai;
    private String phuong_thuc_thanh_toan;

    public String getId() { return id; }
    public Date getCreated_at() { return created_at; }
    public String getPatient_id(){ return patient_id; }
    public String getDoctor_id(){ return doctor_id; }
    public Date getNgay_kham() { return ngay_kham; }
    public Time getGio_du_kien() { return gio_du_kien; }
    public int getSo_thu_tu_trong_ngay() { return so_thu_tu_trong_ngay; }
    public String getTrieu_chung_ban_dau() { return trieu_chung_ban_dau; }
    public int getPhi_kham() { return phi_kham; }
    public String getTrang_thai() { return trang_thai; }
    public String getPhuong_thuc_thanh_toan() { return phuong_thuc_thanh_toan; }
}
