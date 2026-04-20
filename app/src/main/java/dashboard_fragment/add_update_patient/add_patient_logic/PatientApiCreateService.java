package dashboard_fragment.add_update_patient.add_patient_logic;

import java.util.List;

import dashboard_fragment.add_update_patient.PatientProfile;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
public interface PatientApiCreateService {
    @POST("rest/v1/patient")
    Call<List<PatientProfile>> createProfile(
            @Header("Prefer") String prefer,
            @Body CreatePatientRequest newProfile
    );
}
