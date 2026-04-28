package dashboard_fragment.edit_profile.update_profile_logic;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ProfileApiPatchService {
    @PATCH("rest/v1/profiles")
    Call<java.util.List<com.example.nhom08_quanlyphongkham.UserProfile>> updateProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Query("id") String id,
            @Body UpdateProfileRequest request
    );


}
