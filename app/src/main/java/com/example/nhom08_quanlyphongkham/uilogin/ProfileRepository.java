package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ProfileRepository {

    private final ProfileApiService profileApiService;
    private final String apiKey;
    private final Context context;

    public ProfileRepository(Context context, String apiKey) {
        this.apiKey = apiKey;
        this.context = context;
        this.profileApiService = SupabaseClientProvider
                .getClient(context)
                .create(ProfileApiService.class);
    }

    public Call<List<UserProfile>> getProfile(String userId) {
        return profileApiService.getProfile(
                apiKey,

                "eq." + userId,
                "*"
        );
    }
    public Call<List<UserProfile>> getListProfile(String role) {
        return profileApiService.getListProfiles(
                apiKey,
                role,
                "id,ho_ten,chuc_vu"
        );
    }

    public Call<ResponseBody> updateProfile(String userId, Map<String, Object> updates) {
        String token = SharedPrefManager.getInstance(context).getToken();
        return profileApiService.updateProfile(
                apiKey,
                "Bearer " + token,
                "return=minimal",
                "eq." + userId,
                updates
        );
    }
}
