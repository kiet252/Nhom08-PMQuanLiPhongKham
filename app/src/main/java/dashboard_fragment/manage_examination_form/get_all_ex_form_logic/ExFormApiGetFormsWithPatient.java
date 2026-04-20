package dashboard_fragment.manage_examination_form.get_all_ex_form_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ExFormApiGetFormsWithPatient {
    @GET("rest/v1/examination_form")
    Call<List<ExaminationFormWithPatientDto>> getAllFormsWithPatientAndDoctor(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query(value = "select", encoded = true) String select,
            @Query("order") String order,
            @Query("trang_thai") String trangThai
    );
}
