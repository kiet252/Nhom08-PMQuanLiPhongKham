package dashboard_fragment.manage_examination_form.get_all_ex_form_logic;

import java.util.List;

import dashboard_fragment.create_examination_form.ExaminationForm;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ExFormApiGetFormsByPatientCCCDOrIDService {
    @GET("rest/v1/examination_form")
    Call<List<ExaminationFormWithPatientDto>> searchFormsByCccdOrPatientId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query(value = "select", encoded = true) String select,
            @Query(value = "or", encoded = true) String orFilter,
            @Query("order") String order
    );
}
