package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ClinicalApiService {
    @GET("rest/v1/clinical")
    Call<List<ClinicalItem>> getAllClinicalItems(
            @Query("select") String select,
            @Query("order") String order
    );
}
