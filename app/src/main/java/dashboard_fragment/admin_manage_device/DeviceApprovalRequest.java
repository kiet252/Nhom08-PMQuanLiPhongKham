package dashboard_fragment.admin_manage_device;

import com.google.gson.annotations.SerializedName;

public class DeviceApprovalRequest {
    private long id;
    private String created_at;
    private String type_of_request;
    private String android_id;
    private String status;
    private Object face;

    @SerializedName("face_image")
    private String faceImage;

    public Object getFace() {
        return face;
    }

    public void setFace(Object face) {
        this.face = face;
    }

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    // Biến này để hứng Object chứa thông tin nhân viên đã join từ profiles
    @SerializedName("staff_id")
    private StaffProfile staff;

    public long getId() {
        return id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getTypeOfRequest() {
        return type_of_request;
    }

    public String getAndroidId() {
        return android_id;
    }

    // Lấy Staff ID trực tiếp từ Object hồ sơ được join để an toàn tuyệt đối
    public String getStaffId() {
        if (staff != null && staff.getId() != null) {
            return staff.getId();
        }
        return "N/A";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public StaffProfile getStaff() {
        return staff;
    }

    public String getStaffName() {
        if (staff != null && staff.getHoTen() != null && !staff.getHoTen().trim().isEmpty()) {
            return staff.getHoTen();
        }
        return "Chưa có tên nhân viên";
    }

    public static class StaffProfile {
        @SerializedName("id")
        private String id;

        @SerializedName("ho_ten")
        private String hoTen;

        public String getId() {
            return id;
        }

        public String getHoTen() {
            return hoTen;
        }
    }
}