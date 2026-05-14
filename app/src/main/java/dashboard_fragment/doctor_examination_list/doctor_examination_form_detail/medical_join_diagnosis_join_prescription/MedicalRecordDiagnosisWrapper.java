package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription;

public class MedicalRecordDiagnosisWrapper {
    private String chanDoanChinh;
    private String chanDoanBoSung;
    private String ghiChuLamSang;

    public MedicalRecordDiagnosisWrapper(String chanDoanChinh, String chanDoanBoSung, String ghiChuLamSang) {
        this.chanDoanChinh = chanDoanChinh;
        this.chanDoanBoSung = chanDoanBoSung;
        this.ghiChuLamSang = ghiChuLamSang;
    }

    public String getChanDoanChinh() {
        return chanDoanChinh;
    }

    public String getChanDoanBoSung() {
        return chanDoanBoSung;
    }

    public String getGhiChuLamSang() {
        return ghiChuLamSang;
    }
}
