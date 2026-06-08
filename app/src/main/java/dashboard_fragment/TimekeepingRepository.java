package dashboard_fragment;

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
}

