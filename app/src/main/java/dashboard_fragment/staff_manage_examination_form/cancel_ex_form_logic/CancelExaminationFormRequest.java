package dashboard_fragment.staff_manage_examination_form.cancel_ex_form_logic;

import com.google.gson.annotations.SerializedName;

public class CancelExaminationFormRequest {
    @SerializedName("trang_thai")
    private final String trangThai;

    public CancelExaminationFormRequest(String trangThai) {
        this.trangThai = trangThai;
    }
}
