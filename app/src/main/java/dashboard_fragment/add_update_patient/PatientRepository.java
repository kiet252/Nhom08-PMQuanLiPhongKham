package dashboard_fragment.add_update_patient;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.add_update_patient.add_patient_logic.CreatePatientRequest;
import dashboard_fragment.add_update_patient.add_patient_logic.PatientApiCreateService;
import dashboard_fragment.add_update_patient.update_patient_logic.UpdatePatientFragment;
import dashboard_fragment.add_update_patient.update_patient_logic.find_patient_logic.PatientApiGetService;
import dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic.PatientApiUpdateService;
import dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic.UpdatePatientRequest;
import retrofit2.Call;

public class PatientRepository {
    private static PatientApiGetService getService;
    private static PatientApiCreateService createService;

    private static PatientApiUpdateService updateService;
    private final String apiKey;

    public PatientRepository(String apiKey) {
        this.apiKey = apiKey;

        if (getService == null) {
            getService = SupabaseClientProvider.getClient().create(PatientApiGetService.class);
        }
        if (createService == null) {
            createService = SupabaseClientProvider.getClient().create(PatientApiCreateService.class);
        }
        if (updateService == null) {
            updateService = SupabaseClientProvider.getClient().create(PatientApiUpdateService.class);
        }
    }

    public Call<List<PatientProfile>> getProfileByIdOrCccd(String accessToken, String input) {
        String orFilter = "(id.eq." + input + ",cccd.eq." + input + ")";

        return getService.getProfileByIdOrCccd(
                apiKey,
                "Bearer " + accessToken,
                orFilter,
                "*"
        );
    }

    public Call<List<PatientProfile>> createProfile(String accessToken, CreatePatientRequest newProfile) {
        return createService.createProfile(
                apiKey,
                "Bearer " + accessToken,
                "return=representation",
                newProfile
        );
    }

    public Call<Void> updateProfile(String accessToken, String patientId, UpdatePatientRequest updatedProfile) {
        String idFilter = "eq." + patientId;

        return updateService.updatePatient(
                apiKey,
                "Bearer " + accessToken,
                idFilter,
                updatedProfile
        );
    }
}
