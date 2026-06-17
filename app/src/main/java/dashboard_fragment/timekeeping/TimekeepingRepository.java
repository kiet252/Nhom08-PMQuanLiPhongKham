package dashboard_fragment.timekeeping;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.HashMap;
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

    public Call<java.util.List<java.util.Map<String, Object>>> getRequestsByStaffId(String staffId) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();
        queries.put("select", "*");
        queries.put("staff_id", "eq." + staffId);

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

    public Call<java.util.List<ca_lam_viec>> getCaLamViecList(String staffId) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();

        String today = java.time.LocalDate.now().toString();
        String currentTime = java.time.LocalDateTime.now().toString();

        queries.put("select", "id,start_time,end_time, status");
        queries.put("user_id", "eq." + staffId);
        queries.put("status", "neq.Hoàn thành");

        queries.put("start_time", "gte." + today);

        queries.put("end_time", "gte." + currentTime);

        return apiService.getCaLamViec(queries);
    }

    // New method for timekeeping_request to get shifts by staff_id with proper filter
    public Call<java.util.List<ca_lam_viec>> getCaLamViecListByStaffId(String staffId) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();

        queries.put("select", "id,start_time,end_time,status");
        queries.put("user_id", "eq." + staffId);

        Log.e("getCaLamViecListByStaffId", "Query: select=" + queries.get("select") +
                ", user_id=" + queries.get("user_id"));

        return apiService.getCaLamViec(queries);
    }

    public Call<java.util.List<java.util.Map<String, Object>>> getTimekeepingById(String id) {
        java.util.Map<String, String> queries = new java.util.HashMap<>();
        queries.put("select", "id,real_start_time,real_end_time,status");
        queries.put("id", "eq." + id);
        return apiService.getTimekeeping(queries);
    }

    public Call<ResponseBody> updateTimekeepingById(String id, java.util.Map<String, Object> updates) {
        java.util.Map<String, String> query = new java.util.HashMap<>();
        query.put("id", "eq." + id);
        return apiService.updateTimekeeping("return=minimal", query, updates);
    }
}
