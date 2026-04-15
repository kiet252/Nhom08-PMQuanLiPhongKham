package dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic;

import java.util.List;

import dashboard_fragment.add_update_patient.PatientProfile;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface PatientApiUpdateService {
    @PATCH("rest/v1/patient")
    Call<Void> updatePatient(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("id") String idFilter,
            @Body UpdatePatientRequest body
    );
}
