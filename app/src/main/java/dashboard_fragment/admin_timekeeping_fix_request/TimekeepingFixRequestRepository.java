package dashboard_fragment.admin_timekeeping_fix_request;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class TimekeepingFixRequestRepository {
    private static final String SELECT =
            "*,profiles(id,ho_ten,chuc_vu,anh_dai_dien),timekeeping(id,start_time,end_time,status)";

    private final TimekeepingFixRequestApiService apiService;

    public TimekeepingFixRequestRepository(Context context) {
        apiService = SupabaseClientProvider.getClient(context).create(TimekeepingFixRequestApiService.class);
    }

    public Call<List<TimekeepingFixRequestItem>> getAllRequests() {
        return apiService.getRequests(SELECT, "id.desc");
    }

    public Call<List<TimekeepingFixRequestItem>> getPendingRequests() {
        return apiService.getRequestsByStatus("eq.Chờ duyệt", SELECT, "id.desc");
    }

    public Call<ResponseBody> approveRequest(TimekeepingFixRequestItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("trang_thai", "Đã duyệt");
        return apiService.updateRequest("eq." + item.getId(), "return=minimal", body);
    }

    public Call<ResponseBody> rejectRequest(long id) {
        Map<String, Object> body = new HashMap<>();
        body.put("trang_thai", "Từ chối");
        return apiService.updateRequest("eq." + id, "return=minimal", body);
    }

    public Call<ResponseBody> updateShift(TimekeepingFixRequestItem item) {
        Map<String, Object> body = new HashMap<>();
        if (item.getRequestedCheckIn() != null && !item.getRequestedCheckIn().trim().isEmpty()) {
            body.put("start_time", item.getRequestedCheckIn());
        }
        if (item.getRequestedCheckOut() != null && !item.getRequestedCheckOut().trim().isEmpty()) {
            body.put("end_time", item.getRequestedCheckOut());
        }
        return apiService.updateShift("eq." + item.getShiftId(), "return=minimal", body);
    }
}
