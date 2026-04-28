package dashboard_fragment.edit_profile;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import dashboard_fragment.edit_profile.update_profile_logic.ProfileApiPatchService;
import dashboard_fragment.edit_profile.update_profile_logic.UpdateProfileRequest;
import retrofit2.Call;

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
                context.getString(R.string.abAIkey),
                "Bearer " + accessToken,
                "return=representation",
                "eq." + userId,
                request
        );
    }

}
