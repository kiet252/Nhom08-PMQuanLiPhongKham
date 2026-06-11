package dashboard_fragment.admin_timekeeping_schedule;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TimekeepingApiService {

    @GET("rest/v1/timekeeping")
    Call<List<TimekeepingItem>> getShiftsInRange(
            @Query("and") String andFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @POST("rest/v1/timekeeping")
    Call<ResponseBody> createShift(
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );

    @DELETE("rest/v1/timekeeping")
    Call<ResponseBody> deleteShift(
            @Query("id") String idFilter
    );
}
