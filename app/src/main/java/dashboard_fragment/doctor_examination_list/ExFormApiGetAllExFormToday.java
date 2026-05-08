package dashboard_fragment.doctor_examination_list;

import java.util.List;

import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExFormApiGetAllExFormToday {
    @GET("rest/v1/examination_form")
    Call<List<ExaminationFormWithPatientDto>> getFormsByDate(
            @Query("ngay_kham") String ngayKham,
            @Query(value = "select", encoded = true) String select,
            @Query("order") String order,
            @Query("trang_thai") String trangThai
    );
}
