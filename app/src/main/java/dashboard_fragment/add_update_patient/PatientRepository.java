package dashboard_fragment.add_update_patient;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import retrofit2.Call;

public class PatientRepository {
    private static PatientApiGetService getService;
    private static PatientApiCreateService createService;
    private final String apiKey;

    public PatientRepository(String apiKey) {
        this.apiKey = apiKey;

        if (getService == null) {
            getService = SupabaseClientProvider.getClient().create(PatientApiGetService.class);
        }
        if (createService == null) {
            createService = SupabaseClientProvider.getClient().create(PatientApiCreateService.class);
        }
    }

    public Call<List<PatientProfile>> getProfileByName(String accessToken, String hoTen) {
        return getService.getProfileByName(
                apiKey,
                "Bearer " + accessToken,
                "ilike." + hoTen,
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
}
