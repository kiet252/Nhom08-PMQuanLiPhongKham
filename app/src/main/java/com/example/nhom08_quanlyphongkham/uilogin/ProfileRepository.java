package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.List;

import retrofit2.Call;

public class ProfileRepository {

    private final ProfileApiService profileApiService;

    public ProfileRepository(Context context) {
        this.profileApiService = SupabaseClientProvider
                .getClient(context)
                .create(ProfileApiService.class);
    }

    public Call<List<UserProfile>> getProfile(String userId) {
        return profileApiService.getProfile(
                "eq." + userId,
                "*"
        );
    }
    public Call<List<UserProfile>> getListProfile(String role) {
        return profileApiService.getListProfiles(
                role,
                "id,ho_ten,chuc_vu"
        );
    }

    public Call<List<UserProfile>> getDoctors() {
        return profileApiService.getDoctors(
                "eq.Bác sĩ",
                "*"
        );
    }
}
