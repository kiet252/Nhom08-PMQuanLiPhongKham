package dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic;

import java.util.Date;

public class UpdatePatientRequest {
    private String ngay_sinh;
    private String dia_chi;
    private String cccd;

    public UpdatePatientRequest(String ngaySinh, String diaChi, String cccd) {
        this.ngay_sinh = ngaySinh;
        this.dia_chi = diaChi;
        this.cccd = cccd;
    }
}
