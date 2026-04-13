package dashboard_fragment.add_update_patient;

import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileApiService;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import retrofit2.Call;

public class PatientRepository {
    private final PatientApiGetService PatientApiGetService;
    private final String apiKey;

    public PatientRepository(String apiKey) {
        this.apiKey = apiKey;
        PatientApiGetService = SupabaseClientProvider
                .getClient()
                .create(PatientApiGetService.class);
    }

    public Call<List<PatientProfile>> getProfileByName(String accessToken, String hoTen) {
        return PatientApiGetService.getProfileByName(
                apiKey,
                "Bearer " + accessToken,
                "eq." + hoTen,
                "*"
        );
    }
}
