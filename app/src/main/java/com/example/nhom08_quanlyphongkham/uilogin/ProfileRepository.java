package com.example.nhom08_quanlyphongkham.uilogin;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.List;

import retrofit2.Call;

public class ProfileRepository {

    private final ProfileApiService profileApiService;
    private final String apiKey;

    public ProfileRepository(String apiKey) {
        this.apiKey = apiKey;
        profileApiService = SupabaseClientProvider
                .getClient()
                .create(ProfileApiService.class);
    }

    public Call<List<UserProfile>> getProfile(String accessToken, String userId) {
        return profileApiService.getProfile(
                apiKey,
                "Bearer " + accessToken,
                "eq." + userId,
                "*"
        );
    }
}
