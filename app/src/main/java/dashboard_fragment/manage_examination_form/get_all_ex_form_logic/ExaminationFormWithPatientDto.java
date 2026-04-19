package dashboard_fragment.manage_examination_form.get_all_ex_form_logic;

import java.util.Date;

public class ExaminationFormWithPatientDto {
    // examination_form fields
    private String id;
    private String patient_id;
    private String doctor_id;
    private Date ngay_kham;
    private String gio_du_kien;
    private int so_tiep_nhan;
    private String trieu_chung_ban_dau;
    private int phi_kham;
    private String trang_thai;
    private String phuong_thuc_thanh_toan;
    public PatientBriefDto patient;

    public String getId() { return id; }
    public String getPatient_id(){ return patient_id; }
    public String getDoctor_id(){ return doctor_id; }
    public Date getNgay_kham() { return ngay_kham; }
    public String getGio_du_kien() { return gio_du_kien; }
    public int getSo_tiep_nhan() { return so_tiep_nhan; }
    public String getTrieu_chung_ban_dau() { return trieu_chung_ban_dau; }
    public int getPhi_kham() { return phi_kham; }
    public String getTrang_thai() { return trang_thai; }
    public String getPhuong_thuc_thanh_toan() { return phuong_thuc_thanh_toan; }
}