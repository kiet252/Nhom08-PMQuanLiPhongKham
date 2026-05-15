package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;

public class MedicalRecordMedicineWrapper {
    private int so_luong;
    private String lieu_dung;
    private String tan_suat;
    private String thoi_gian;
    private MedicineItem medicine;

    public int getSoLuong() { return so_luong; }
    public String getLieuDung() { return lieu_dung; }
    public String getTanSuat() { return tan_suat; }
    public String getThoiGian() { return thoi_gian; }
    public MedicineItem getMedicine() { return medicine; }
}
