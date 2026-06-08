package dashboard_fragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.Map;

public interface TimekeepingApiService {
    @POST("rest/v1/timekeeping_auth_request")
    Call<ResponseBody> createAuthRequest(
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );
}

