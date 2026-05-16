package dashboard_fragment.doctor_view_medical_record;

import java.util.List;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordClinicalWrapper;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordMedicineWrapper;

public class MedicalRecordByPatientResponse {
    private Long id;
    private String chan_doan_chinh;
    private String chan_doan_bo_sung;
    private String ghi_chu_lam_sang;
    private String doctor_id;
    private ExaminationFormWrapper examination_form;
    private List<MedicalRecordClinicalWrapper> medical_record_clinical;
    private List<MedicalRecordMedicineWrapper> medical_record_medicine;

    public Long getId() { return id; }
    public String getChanDoanChinh() { return chan_doan_chinh; }
    public String getChanDoanBoSung() { return chan_doan_bo_sung; }
    public String getGhiChuLamSang() { return ghi_chu_lam_sang; }
    public String getDoctorId() { return doctor_id; }
    public ExaminationFormWrapper getExaminationForm() { return examination_form; }
    public List<MedicalRecordClinicalWrapper> getClinicalData() { return medical_record_clinical; }
    public List<MedicalRecordMedicineWrapper> getMedicineData() { return medical_record_medicine; }

    public static class ExaminationFormWrapper {
        private Long id;
        private Long patient_id;
        private String ngay_kham;
        private String trang_thai;

        public Long getId() { return id; }
        public Long getPatientId() { return patient_id; }
        public String getNgayKham() { return ngay_kham; }
        public String getTrangThai() { return trang_thai; }
    }
}
