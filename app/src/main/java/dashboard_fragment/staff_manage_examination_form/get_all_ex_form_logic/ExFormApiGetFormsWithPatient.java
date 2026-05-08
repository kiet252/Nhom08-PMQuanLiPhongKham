package dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExFormApiGetFormsWithPatient {
    @GET("rest/v1/examination_form")
    Call<List<ExaminationFormWithPatientDto>> getAllFormsWithPatientAndDoctor(
            @Query(value = "select", encoded = true) String select,
            @Query("order") String order,
            @Query("trang_thai") String trangThai
    );
}
