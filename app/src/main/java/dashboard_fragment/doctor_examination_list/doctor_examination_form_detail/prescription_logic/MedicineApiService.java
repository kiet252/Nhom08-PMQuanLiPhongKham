package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MedicineApiService {
    @GET("rest/v1/medicine")
    Call<List<MedicineItem>> getAllMedicines(
            @Query("select") String select,
            @Query("order") String order
    );
}
