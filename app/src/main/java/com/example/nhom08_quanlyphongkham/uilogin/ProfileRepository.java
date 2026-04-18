package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.List;

import retrofit2.Call;

public class ProfileRepository {

    private final ProfileApiService profileApiService;
    private final String apiKey;

    public ProfileRepository(Context context, String apiKey) {
        this.apiKey = apiKey;
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
}
