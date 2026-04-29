package dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class UpdatePatientRequest {
    @SerializedName("so_dien_thoai")
    private String so_dien_thoai;
    @SerializedName("dia_chi")
    private String dia_chi;
    @SerializedName("cccd")
    private String cccd;
    @SerializedName("gioi_tinh")
    private String gioi_tinh;

    public UpdatePatientRequest(String so_dien_thoai, String diaChi, String cccd, String gioi_tinh) {
        this.so_dien_thoai = so_dien_thoai;
        this.dia_chi = diaChi;
        this.cccd = cccd;
        this.gioi_tinh = gioi_tinh;
    }
}
