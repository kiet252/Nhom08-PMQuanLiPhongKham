package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription;

import java.util.List;

public class FullMedicalRecordResponse {
    private Long id;
    private String chan_doan_chinh;
    private String chan_doan_bo_sung;
    private String ghi_chu_lam_sang;
    private List<MedicalRecordClinicalWrapper> medical_record_clinical;
    private List<MedicalRecordMedicineWrapper> medical_record_medicine;

    public MedicalRecordDiagnosisWrapper getDiagnosisNotes() {
        return new MedicalRecordDiagnosisWrapper(chan_doan_chinh, chan_doan_bo_sung, ghi_chu_lam_sang);
    }
    // Getters
    public Long getId() { return id; }
    public List<MedicalRecordClinicalWrapper> getClinicalData() { return medical_record_clinical; }
    public List<MedicalRecordMedicineWrapper> getMedicineData() { return medical_record_medicine; }
}
