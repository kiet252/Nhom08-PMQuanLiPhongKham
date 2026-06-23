package dashboard_fragment.admin_manage_device;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface DeviceApprovalApiService {
    @GET("rest/v1/timekeeping_auth_request?type_of_request=in.(DeviceID,Face)&order=created_at.desc")
    Call<List<DeviceApprovalRequest>> getPendingDeviceRequests(
            @Query("select") String select
    );

    @PATCH("rest/v1/profiles")
    Call<ResponseBody> updateProfileDevice(
            @Header("Prefer") String prefer,
            @Query("id") String profileId,
            @Body Map<String, Object> updates
    );

    @PATCH("rest/v1/timekeeping_auth_request")
    Call<ResponseBody> updateRequestStatus(
            @Header("Prefer") String prefer,
            @Query("id") String requestId,
            @Body Map<String, Object> updates
    );
}
