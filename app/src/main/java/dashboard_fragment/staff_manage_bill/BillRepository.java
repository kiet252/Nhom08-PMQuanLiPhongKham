package dashboard_fragment.staff_manage_bill;

import android.content.Context;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import java.util.List;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDtoApiService;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;
import dashboard_fragment.staff_manage_bill.patch_bills_logic.BillUpdatePayload;
import dashboard_fragment.staff_manage_bill.patch_bills_logic.BillUpdatePayloadApiRequest;
import retrofit2.Call;
import retrofit2.Retrofit;

public class BillRepository {
    private final ExamFormWithBillDtoApiService examFormWithBillDtoApiService;
    private final BillUpdatePayloadApiRequest billUpdatePayloadApiService;

    public BillRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);
        this.examFormWithBillDtoApiService = client.create(ExamFormWithBillDtoApiService.class);
        this.billUpdatePayloadApiService = client.create(BillUpdatePayloadApiRequest.class);
    }

    public Call<List<ExamFormWithBillDto>> getBillsByPatientId(String patientId) {
        String select = "id,ngay_kham,"
                + "patient:patient!examination_form_patient_id_fkey(id,ho_ten,so_dien_thoai,dia_chi),"
                + "medical_record!inner("
                + "  bill!inner(id,created_at,phuong_thuc_thanh_toan,trang_thai_thanh_toan,tong_thanh_toan),"
                + "  medical_record_clinical(id, clinical:clinical(id, ten_dich_vu, don_gia)),"
                + "  medical_record_medicine(id, so_luong, medicine:medicine(id, ten_thuoc, don_vi, don_gia))"
                + ")";

        return examFormWithBillDtoApiService.getBillsByPatientId("eq." + patientId, select, "ngay_kham.desc");
    }

    // --- ADDED REPOSITORY UPDATE WRAPPER METHOD ---
    public Call<Void> updateBill(long billId, String paymentMethod, String invoiceStatus, double totalAmount) {
        String idFilter = "eq." + billId;
        BillUpdatePayload payload = new BillUpdatePayload(paymentMethod, invoiceStatus, totalAmount);

        return billUpdatePayloadApiService.updateBillStatus(idFilter, payload, "return=minimal");
    }
}