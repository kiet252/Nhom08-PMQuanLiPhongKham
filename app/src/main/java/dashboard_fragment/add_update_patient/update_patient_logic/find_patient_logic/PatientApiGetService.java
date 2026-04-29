package dashboard_fragment.add_update_patient.update_patient_logic.find_patient_logic;

import java.util.List;

import dashboard_fragment.add_update_patient.PatientProfile;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface PatientApiGetService {
    @GET("rest/v1/patient")
    Call<List<PatientProfile>> getProfileByIdOrCccd(
            @Query("or") String orFilter,
            @Query("select") String select
    );
}
