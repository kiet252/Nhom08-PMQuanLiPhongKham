package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic;

public class ClinicalItem {
    private int id;
    private String ten_dich_vu;
    private String loai_dich_vu;
    private String mo_ta;
    private Double don_gia;

    public int getId() {
        return id;
    }

    public String getTen_dich_vu() {
        return ten_dich_vu;
    }

    public String getLoai_dich_vu() {
        return loai_dich_vu;
    }

    public String getMo_ta() {
        return mo_ta;
    }

    public Double getDon_gia() {
        return don_gia;
    }

    public void setDon_gia(Double don_gia) {
        this.don_gia = don_gia;
    }
}
