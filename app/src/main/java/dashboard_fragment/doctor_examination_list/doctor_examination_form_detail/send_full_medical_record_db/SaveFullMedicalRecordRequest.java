package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db;

import java.util.List;

public class SaveFullMedicalRecordRequest {
    private final long p_ex_form_id;
    private final String p_doctor_id;
    private final String p_chan_doan_chinh;
    private final String p_chan_doan_bo_sung;
    private final String p_ghi_chu_lam_sang;
    private final List<Long> p_clinical_ids;
    private final List<SaveFullMedicalRecordMedicinePayload> p_medicines;

    public SaveFullMedicalRecordRequest(long exFormId, String doctorId,
                                        String chanDoanChinh, String chanDoanBoSung, String ghiChuLamSang,
                                        List<Long> clinicalIds,
                                        List<SaveFullMedicalRecordMedicinePayload> medicines) {
        this.p_ex_form_id = exFormId;
        this.p_doctor_id = doctorId;
        this.p_chan_doan_chinh = chanDoanChinh;
        this.p_chan_doan_bo_sung = chanDoanBoSung;
        this.p_ghi_chu_lam_sang = ghiChuLamSang;
        this.p_clinical_ids = clinicalIds;
        this.p_medicines = medicines;
    }
}
