package dashboard_fragment.staff_add_update_patient.update_patient_logic.update_patient_logic;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface PatientApiUpdateService {
    @PATCH("rest/v1/patient")
    Call<Void> updatePatient(
            @Query("id") String idFilter,
            @Body UpdatePatientRequest body
    );
}
