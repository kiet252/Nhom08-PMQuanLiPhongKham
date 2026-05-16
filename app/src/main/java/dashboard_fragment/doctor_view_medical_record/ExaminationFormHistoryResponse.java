package dashboard_fragment.doctor_view_medical_record;

import java.util.List;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordClinicalWrapper;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordMedicineWrapper;

public class ExaminationFormHistoryResponse {
    private Long id;
    private String ngay_kham;
    private String trang_thai;
    private String trieu_chung_ban_dau;
    private DoctorProfile profiles;
    private List<MedicalRecordInner> medical_record;

    public Long getId() { return id; }
    public String getNgay_kham() { return ngay_kham; }
    public String getTrang_thai() { return trang_thai; }
    public String getTrieu_chung_ban_dau() { return trieu_chung_ban_dau; }
    public DoctorProfile getProfiles() { return profiles; }
    public List<MedicalRecordInner> getMedical_record() { return medical_record; }

    public static class DoctorProfile {
        private String ho_ten;
        public String getHo_ten() { return ho_ten; }
    }

    public static class MedicalRecordInner {
        private Long id;
        private String chan_doan_chinh;
        private String chan_doan_bo_sung;
        private String ghi_chu_lam_sang;
        private List<MedicalRecordClinicalWrapper> medical_record_clinical;
        private List<MedicalRecordMedicineWrapper> medical_record_medicine;

        public Long getId() { return id; }
        public String getChanDoanChinh() { return chan_doan_chinh; }
        public String getChanDoanBoSung() { return chan_doan_bo_sung; }
        public String getGhiChuLamSang() { return ghi_chu_lam_sang; }
        public List<MedicalRecordClinicalWrapper> getClinicalData() { return medical_record_clinical; }
        public List<MedicalRecordMedicineWrapper> getMedicineData() { return medical_record_medicine; }
    }
}
