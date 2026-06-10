package dashboard_fragment;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.util.Log;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class TimekeepingRepository {
    private final TimekeepingApiService apiService;

    public TimekeepingRepository(Context context) {
        this.apiService = SupabaseClientProvider.getClient(context).create(TimekeepingApiService.class);
    }

    public Call<ResponseBody> sendAuthRequest(String androidId, String typeOfRequest, String staffId) {
        Map<String, Object> body = new HashMap<>();
        body.put("android_id", androidId);
        body.put("type_of_request", typeOfRequest);
        body.put("staff_id", staffId);

        // Prefer minimal to avoid returning representation
        return apiService.createAuthRequest("return=minimal", body);
    }

    public Call<ResponseBody> sendFaceAuthRequest(String staffId, java.util.List<Double> faceVector, String details) {
        Map<String, Object> body = new HashMap<>();
        body.put("type_of_request", "Face");
        body.put("face", faceVector);
        body.put("staff_id", staffId);

        // Log the outgoing body for debugging (helps diagnose 400 responses)
        try {
            Gson gson = new Gson();
            Log.e("TimekeepingRepo", "sendFaceAuthRequest body: " + gson.toJson(body));
        } catch (Exception e) {
            Log.e("TimekeepingRepo", "Could not serialize body for logging", e);
        }

        return apiService.createAuthRequest("return=minimal", body);
    }

    /**
     * Alternative: send face vector as JSON string (useful if the DB column expects a textual/vector literal)
     */
    public Call<ResponseBody> sendFaceAuthRequestAsString(String staffId, java.util.List<Double> faceVector, String details) {
        Map<String, Object> body = new HashMap<>();
        body.put("type_of_request", "Face");
        try {
            Gson gson = new Gson();
            String faceJson = gson.toJson(faceVector);
            body.put("face", faceJson);
        } catch (Exception e) {
            Log.e("TimekeepingRepo", "Could not serialize faceVector to JSON string", e);
            body.put("face", null);
        }
        body.put("staff_id", staffId);

        try {
            Gson gson = new Gson();
            Log.e("TimekeepingRepo", "sendFaceAuthRequestAsString body: " + gson.toJson(body));
        } catch (Exception e) {
            Log.e("TimekeepingRepo", "Could not serialize body for logging", e);
        }

        return apiService.createAuthRequest("return=minimal", body);
    }

    // Return any requests that match the given staff_id (no type filter)
    public Call<java.util.List<java.util.Map<String, Object>>> getRequestsByStaffId(String staffId) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();
        queries.put("select", "*");
        queries.put("staff_id", "eq." + staffId);

        return apiService.getAuthRequests(queries);
    }
    public Call<java.util.List<java.util.Map<String, Object>>> getShifts(String staffId) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();
        queries.put("select", "start_time,end_time");
        queries.put("user_id", "eq." + staffId);
        queries.put("start_time", "gte." + getTodayStart()); // >= 00:00:00
        queries.put("end_time", "lte." + getTodayEnd());   // <= 23:59:59
        return apiService.getTimekeepingEntries(queries);
    }
    private String getTodayStart() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00+07:00", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getTodayEnd() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59+07:00", Locale.getDefault());
        return sdf.format(new Date());
    }

    // New: get requests by staff_id and exact status (e.g. "Chưa duyệt")
    public Call<java.util.List<java.util.Map<String, Object>>> getRequestsByStaffIdAndStatus(String staffId, String status) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();
        queries.put("select", "*");
        queries.put("staff_id", "eq." + staffId);
        queries.put("status", "eq." + status);
        Log.e("CHECK_PENDING", "query staff_id: [" + staffId + "]");
        Log.e("CHECK_PENDING", "query status: [" + status + "]");
        return apiService.getAuthRequests(queries);
    }

    // New: get requests by staff_id, status and type_of_request (e.g. "Chưa duyệt", "Face" or "DeviceID")
    public Call<java.util.List<java.util.Map<String, Object>>> getRequestsByStaffIdStatusAndType(String staffId, String status, String typeOfRequest) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();
        queries.put("select", "*");
        queries.put("staff_id", "eq." + staffId);
        queries.put("status", "eq." + status);
        queries.put("type_of_request", "eq." + typeOfRequest);
        Log.e("CHECK_PENDING", "query staff_id: [" + staffId + "]");
        Log.e("CHECK_PENDING", "query status: [" + status + "]");
        Log.e("CHECK_PENDING", "query type_of_request: [" + typeOfRequest + "]");
        return apiService.getAuthRequests(queries);
    }
}
