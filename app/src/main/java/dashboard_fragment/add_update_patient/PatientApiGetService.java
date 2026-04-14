package dashboard_fragment.add_update_patient;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface PatientApiGetService {
    @GET("rest/v1/patient")
    Call<List<PatientProfile>> getProfileByName(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("ho_ten") String hoTen,
            @Query("select") String select
    );
}
