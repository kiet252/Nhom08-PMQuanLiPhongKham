package dashboard_fragment.account_edit_profile;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import dashboard_fragment.account_edit_profile.update_profile_logic.ProfileApiPatchService;
import dashboard_fragment.account_edit_profile.update_profile_logic.UpdateProfileRequest;
import retrofit2.Call;

import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_ANON_KEY;

public class EditProfileRepository {

    private final ProfileApiPatchService profileApiPatchService;
    private final Context context;

    public EditProfileRepository(Context context) {
        this.context = context;
        this.profileApiPatchService = SupabaseClientProvider
                .getClient(context)
                .create(ProfileApiPatchService.class);
    }

    public Call<java.util.List<com.example.nhom08_quanlyphongkham.UserProfile>> updateProfile(
            String accessToken,
            String userId,
            UpdateProfileRequest request
    ) {
        return profileApiPatchService.updateProfile(
                SUPABASE_ANON_KEY,
                "Bearer " + accessToken,
                "return=representation",
                "eq." + userId,
                request
        );
    }

}
