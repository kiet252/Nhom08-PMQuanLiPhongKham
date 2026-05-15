package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db;

public class SaveFullMedicalRecordMedicinePayload {
    private final long medicine_id;
    private final int so_luong;
    private final int lieu_dung;
    private final String tan_suat;
    private final String thoi_gian;
    private final String ghi_chu;

    public SaveFullMedicalRecordMedicinePayload(long medicineId, int soLuong, int lieuDung,
                                                String tanSuat, String thoiGian, String ghiChu) {
        this.medicine_id = medicineId;
        this.so_luong = soLuong;
        this.lieu_dung = lieuDung;
        this.tan_suat = tanSuat;
        this.thoi_gian = thoiGian;
        this.ghi_chu = ghiChu;
    }
}
