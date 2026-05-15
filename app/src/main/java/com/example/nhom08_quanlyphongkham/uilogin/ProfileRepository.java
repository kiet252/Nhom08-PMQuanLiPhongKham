package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.UserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ProfileRepository {

    private final ProfileApiService profileApiService;
    private final Context context;

    public ProfileRepository(Context context) {
        this.context = context;
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
    public Call<List<UserProfile>> getListProfile(String role, String status) {
        return profileApiService.getListProfiles(
                role,
                status,
                "id,ho_ten,chuc_vu,anh_dai_dien"
        );
    }

    public Call<List<UserProfile>> getDoctors() {
        return profileApiService.getDoctors(
                "eq.Bác sĩ",
                "*"
        );
    }
    public Call<ResponseBody> updateProfile(String userId, Map<String, Object> updates) {
        String token = SharedPrefManager.getInstance(context).getToken();
        return profileApiService.updateProfile(
                "Bearer " + token,
                "return=minimal",
                "eq." + userId,
                updates);
        }
    public Call<ResponseBody> deactivateStaff(String staffId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("trang_thai_hoat_dong", "Vô hiệu hoá");
        return updateProfile(staffId, updates);
    }

}