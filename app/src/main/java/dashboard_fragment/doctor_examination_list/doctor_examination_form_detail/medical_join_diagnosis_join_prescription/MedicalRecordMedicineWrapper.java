package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;

public class MedicalRecordMedicineWrapper {
    private int so_luong;
    private String lieu_dung;
    private MedicineItem medicine;

    public int getSoLuong() { return so_luong; }
    public String getLieuDung() { return lieu_dung; }
    public MedicineItem getMedicine() { return medicine; }
}
