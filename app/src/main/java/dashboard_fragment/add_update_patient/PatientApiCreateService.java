package dashboard_fragment.add_update_patient;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
public interface PatientApiCreateService {
    @POST("rest/v1/profiles")
    Call<List<PatientProfile>> createProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Body PatientProfile newProfile
    );
}
