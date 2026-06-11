package dashboard_fragment.admin_timekeeping_schedule;

import com.google.gson.annotations.SerializedName;

public class TimekeepingItem {
    private long id;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("user_id")
    private String userId;

    private String status;

    private StaffProfile profiles;

    /**
     * Tạo TimekeepingItem tạm (chưa có id từ server) để update UI ngay sau khi tạo ca thành công.
     */
    public static TimekeepingItem createLocal(String userId, String userName, String userRole,
                                              String startTime, String endTime) {
        TimekeepingItem item = new TimekeepingItem();
        item.id = -1L; // chưa có id thật
        item.userId = userId;
        item.startTime = startTime;
        item.endTime = endTime;
        item.status = "Chưa checkin";
        StaffProfile profile = new StaffProfile();
        profile.id = userId;
        profile.hoTen = userName;
        profile.chucVu = userRole;
        item.profiles = profile;
        return item;
    }

    public long getId() {
        return id;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getUserId() {
        if (userId != null && !userId.isEmpty()) return userId;
        if (profiles != null && profiles.getId() != null) return profiles.getId();
        return null;
    }

    public String getStatus() {
        return status != null ? status : "Chưa checkin";
    }

    public StaffProfile getProfiles() {
        return profiles;
    }

    public String getStaffName() {
        if (profiles != null && profiles.getHoTen() != null && !profiles.getHoTen().trim().isEmpty()) {
            return profiles.getHoTen();
        }
        return "Nhân viên";
    }

    public String getStaffRole() {
        if (profiles != null && profiles.getChucVu() != null) {
            return profiles.getChucVu();
        }
        return "";
    }

    public static class StaffProfile {
        private String id;

        @SerializedName("ho_ten")
        private String hoTen;

        @SerializedName("chuc_vu")
        private String chucVu;

        @SerializedName("anh_dai_dien")
        private String anhDaiDien;

        public String getId() {
            return id;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getChucVu() {
            return chucVu;
        }

        public String getAnhDaiDien() {
            return anhDaiDien;
        }
    }
}
