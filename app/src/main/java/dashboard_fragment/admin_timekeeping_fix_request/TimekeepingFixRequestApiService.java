package dashboard_fragment.admin_timekeeping_fix_request;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface TimekeepingFixRequestApiService {
    @GET("rest/v1/timekeeping_fix_request")
    Call<List<TimekeepingFixRequestItem>> getRequests(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/timekeeping_fix_request")
    Call<List<TimekeepingFixRequestItem>> getRequestsByStatus(
            @Query("trang_thai") String statusFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @PATCH("rest/v1/timekeeping_fix_request")
    Call<ResponseBody> updateRequest(
            @Query("id") String idFilter,
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );

    @PATCH("rest/v1/timekeeping")
    Call<ResponseBody> updateShift(
            @Query("id") String idFilter,
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );
}
