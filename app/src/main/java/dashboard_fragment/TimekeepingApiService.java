package dashboard_fragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface TimekeepingApiService {
    @POST("rest/v1/timekeeping_auth_request")
    Call<ResponseBody> createAuthRequest(
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );

    @GET("rest/v1/timekeeping_auth_request")
    Call<java.util.List<java.util.Map<String, Object>>> getAuthRequests(
            @QueryMap Map<String, String> queries
            );
    @GET("rest/v1/timekeeping")
    Call<java.util.List<java.util.Map<String, Object>>> getTimekeepingEntries(
            @QueryMap Map<String, String> queries

    );
}

