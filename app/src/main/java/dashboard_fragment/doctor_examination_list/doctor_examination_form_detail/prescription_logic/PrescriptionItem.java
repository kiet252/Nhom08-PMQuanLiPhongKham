package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic;

public class PrescriptionItem {
    private final MedicineItem medicine;
    private String lieuDung = "1 viên";
    private String ghiChu = "";
    private String tanSuat = "2 lần/ngày";
    private String thoiGian = "7 ngày";
    public PrescriptionItem(MedicineItem medicine) {
        this.medicine = medicine;
    }

    public MedicineItem getMedicine() {
        return medicine;
    }

    public String getLieuDung() {
        return lieuDung;
    }

    public void setLieuDung(String lieuDung) {
        this.lieuDung = lieuDung;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTanSuat() {
        return tanSuat;
    }

    public void setTanSuat(String tanSuat) {
        this.tanSuat = tanSuat;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }
}
