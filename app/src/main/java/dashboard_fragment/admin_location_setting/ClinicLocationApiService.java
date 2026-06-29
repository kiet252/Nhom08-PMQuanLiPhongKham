package dashboard_fragment.admin_location_setting;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ClinicLocationApiService {

    @GET("rest/v1/clinic_location")
    @Headers({"Prefer: return=representation"})
    Call<List<ClinicLocation>> getLocations();

    // Tạo mới vị trí
    @POST("rest/v1/clinic_location")
    @Headers({"Prefer: return=representation"})
    Call<List<ClinicLocation>> createLocation(@Body ClinicLocation location);

    // Cập nhật vị trí theo id
    @PATCH("rest/v1/clinic_location")
    @Headers({"Prefer: return=representation"})
    Call<List<ClinicLocation>> updateLocation(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );
}