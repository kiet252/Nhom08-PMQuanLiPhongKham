package dashboard_fragment.admin_manage_medicine_clinical;

import java.util.List;
import java.util.Map;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AdminMedClsApiService {

    // --- MEDICINE ---
    @GET("rest/v1/medicine")
    Call<List<MedicineItem>> getMedicines(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/medicine")
    Call<List<MedicineItem>> getMedicineById(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @POST("rest/v1/medicine")
    @Headers({
            "Prefer: return=representation"
    })
    Call<List<MedicineItem>> createMedicine(
            @Body Map<String, Object> body
    );

    @PATCH("rest/v1/medicine")
    Call<ResponseBody> updateMedicine(
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );

    // --- CLINICAL ---
    @GET("rest/v1/clinical")
    Call<List<ClinicalItem>> getClinicalItems(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/clinical")
    Call<List<ClinicalItem>> getClinicalItemById(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @POST("rest/v1/clinical")
    @Headers({
            "Prefer: return=representation"
    })
    Call<List<ClinicalItem>> createClinicalItem(
            @Body Map<String, Object> body
    );

    @PATCH("rest/v1/clinical")
    Call<ResponseBody> updateClinicalItem(
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );
}
