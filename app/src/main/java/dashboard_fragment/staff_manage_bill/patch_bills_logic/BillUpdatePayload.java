package dashboard_fragment.staff_manage_bill.patch_bills_logic;

public class BillUpdatePayload {
    private String phuong_thuc_thanh_toan;
    private String trang_thai_thanh_toan;

    public BillUpdatePayload(String phuong_thuc_thanh_toan, String trang_thai_thanh_toan) {
        this.phuong_thuc_thanh_toan = phuong_thuc_thanh_toan;
        this.trang_thai_thanh_toan = trang_thai_thanh_toan;
    }
}
