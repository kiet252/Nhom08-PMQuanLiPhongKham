package dashboard_fragment.edit_profile.update_profile_logic;

public class UpdateProfileRequest {
    private String ho_ten;
    private String so_dien_thoai;
    private String dia_chi;
    private String gioitinh;

    public UpdateProfileRequest(String hoTen, String soDienThoai, String diaChi, String gioiTinh) {
        this.ho_ten = hoTen;
        this.so_dien_thoai = soDienThoai;
        this.dia_chi = diaChi;
        this.gioitinh = gioiTinh;
    }
}
