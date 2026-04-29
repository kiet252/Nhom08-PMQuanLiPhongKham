package dashboard_fragment.add_update_patient;
import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.add_update_patient.add_patient_logic.CreatePatientRequest;
import dashboard_fragment.add_update_patient.add_patient_logic.PatientApiCreateService;
import dashboard_fragment.add_update_patient.update_patient_logic.UpdatePatientFragment;
import dashboard_fragment.add_update_patient.update_patient_logic.find_patient_logic.PatientApiGetService;
import dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic.PatientApiUpdateService;
import dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic.UpdatePatientRequest;
import retrofit2.Call;
import retrofit2.Retrofit;

public class PatientRepository {
    private final PatientApiGetService getService;
    private final PatientApiCreateService createService;
    private final PatientApiUpdateService updateService;
    private final Context context;

    public PatientRepository(Context context) {
        this.context = context;

        // Use the context to get the Retrofit client
        Retrofit client = SupabaseClientProvider.getClient(context);

        this.getService = client.create(PatientApiGetService.class);
        this.createService = client.create(PatientApiCreateService.class);
        this.updateService = client.create(PatientApiUpdateService.class);
    }

    public Call<List<PatientProfile>> getProfileByIdOrCccd(String input) {
        String orFilter = "(id.eq." + input + ",cccd.eq." + input + ")";
        return getService.getProfileByIdOrCccd(orFilter, "*");
    }

    public Call<List<PatientProfile>> createProfile(CreatePatientRequest newProfile) {
        return createService.createProfile("return=representation", newProfile);
    }

    public Call<Void> updateProfile(String patientId, UpdatePatientRequest updatedProfile) {
        String idFilter = "eq." + patientId;
        return updateService.updatePatient(idFilter, updatedProfile);
    }
}
