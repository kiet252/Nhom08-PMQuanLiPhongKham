package dashboard_fragment.admin_timekeeping_fix_request;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TimekeepingFixRequestItem {
    private long id;

    @SerializedName("staff_id")
    private String staffId;

    @SerializedName("id_ca_lam")
    private long shiftId;

    @SerializedName("li_do")
    private String reason;

    @SerializedName("thoi_gian_vao_ca")
    private String requestedCheckIn;

    @SerializedName("thoi_gian_ra_ca")
    private String requestedCheckOut;

    @SerializedName("trang_thai")
    private String status;

    @SerializedName("minh_chung")
    private JsonElement evidence;

    @SerializedName("profiles")
    private StaffProfile staff;

    @SerializedName("timekeeping")
    private ShiftInfo shift;

    public long getId() {
        return id;
    }

    public String getStaffId() {
        return staffId;
    }

    public long getShiftId() {
        return shiftId;
    }

    public String getReason() {
        return reason != null ? reason : "";
    }

    public String getRequestedCheckIn() {
        return requestedCheckIn;
    }

    public String getRequestedCheckOut() {
        return requestedCheckOut;
    }

    public String getStatus() {
        return status != null && !status.trim().isEmpty() ? status : "Chờ duyệt";
    }

    public StaffProfile getStaff() {
        return staff;
    }

    public ShiftInfo getShift() {
        return shift;
    }

    public String getStaffName() {
        return staff != null && staff.hoTen != null && !staff.hoTen.trim().isEmpty()
                ? staff.hoTen : "Nhân viên";
    }

    public String getStaffRole() {
        return staff != null && staff.chucVu != null ? staff.chucVu : "";
    }

    public String getStaffAvatar() {
        return staff != null ? staff.anhDaiDien : null;
    }

    public String getOriginalStartTime() {
        return shift != null ? shift.startTime : null;
    }

    public String getOriginalEndTime() {
        return shift != null ? shift.endTime : null;
    }

    public String getOriginalStatus() {
        return shift != null && shift.status != null ? shift.status : "";
    }

    public List<String> getEvidenceUrls() {
        List<String> urls = new ArrayList<>();
        collectEvidenceUrls(evidence, urls);
        return urls;
    }

    private void collectEvidenceUrls(JsonElement element, List<String> urls) {
        if (element == null || element.isJsonNull()) return;
        if (element.isJsonPrimitive()) {
            String value = element.getAsString();
            if (looksLikeUrl(value)) urls.add(value);
            return;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                collectEvidenceUrls(child, urls);
            }
            return;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String[] keys = {"url", "image_url", "imageUrl", "path", "publicUrl", "src"};
            for (String key : keys) {
                if (object.has(key)) collectEvidenceUrls(object.get(key), urls);
            }
            String[] arrayKeys = {"urls", "images", "anh", "minh_chung", "files"};
            for (String key : arrayKeys) {
                if (object.has(key)) collectEvidenceUrls(object.get(key), urls);
            }
        }
    }

    private boolean looksLikeUrl(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }

    public static class StaffProfile {
        private String id;

        @SerializedName("ho_ten")
        private String hoTen;

        @SerializedName("chuc_vu")
        private String chucVu;

        @SerializedName("anh_dai_dien")
        private String anhDaiDien;
    }

    public static class ShiftInfo {
        private long id;

        @SerializedName("start_time")
        private String startTime;

        @SerializedName("end_time")
        private String endTime;

        private String status;
    }
}
