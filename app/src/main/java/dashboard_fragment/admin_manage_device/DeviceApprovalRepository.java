package dashboard_fragment.admin_manage_device;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class DeviceApprovalRepository {
    private final DeviceApprovalApiService apiService;

    public DeviceApprovalRepository(Context context) {
        apiService = SupabaseClientProvider.getClient(context).create(DeviceApprovalApiService.class);
    }

    public Call<List<DeviceApprovalRequest>> getPendingDeviceRequests() {
        return apiService.getPendingDeviceRequests(
                "id,created_at,type_of_request,android_id,status,face,face_image,staff_id:profiles(id,ho_ten)"
        );
    }

    public Call<ResponseBody> approveProfileDevice(String staffId, String androidId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("android_id", androidId);
        return apiService.updateProfileDevice("return=minimal", "eq." + staffId, updates);
    }

    public Call<ResponseBody> approveProfileFace(String staffId, String faceId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("face_id", faceId);
        return apiService.updateProfileDevice("return=minimal", "eq." + staffId, updates);
    }

    public Call<ResponseBody> updateRequestStatus(long requestId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        return apiService.updateRequestStatus("return=representation", "eq." + requestId, updates);
    }
}
