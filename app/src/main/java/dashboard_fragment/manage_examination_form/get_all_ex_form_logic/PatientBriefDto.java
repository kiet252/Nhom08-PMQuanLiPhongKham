package dashboard_fragment.manage_examination_form.get_all_ex_form_logic;

import java.util.Date;

public class PatientBriefDto {
    private String id;
    private String ho_ten;
    private Date ngay_sinh;
    private String dia_chi;
    private String cccd;

    public String getId() {
        return id;
    }
    public String getHo_ten() {
        return ho_ten;
    }
    public Date getNgay_sinh() {
        return ngay_sinh;
    }
    public String getDia_chi() {
        return dia_chi;
    }

    public String getCccd() {
        return cccd;
    }
}
