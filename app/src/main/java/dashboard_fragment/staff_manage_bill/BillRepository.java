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
    private static final String BILL_SELECT = "id,ngay_kham,"
            + "patient:patient!examination_form_patient_id_fkey(id,ho_ten,so_dien_thoai,dia_chi),"
            + "medical_record!inner("
            + "  bill!inner(id,created_at,phuong_thuc_thanh_toan,trang_thai_thanh_toan,tong_thanh_toan),"
            + "  medical_record_clinical(id, clinical:clinical(id, ten_dich_vu, don_gia)),"
            + "  medical_record_medicine(id, so_luong, medicine:medicine(id, ten_thuoc, don_vi, don_gia))"
            + ")";

    public BillRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);
        this.examFormWithBillDtoApiService = client.create(ExamFormWithBillDtoApiService.class);
        this.billUpdatePayloadApiService = client.create(BillUpdatePayloadApiRequest.class);
    }

    public Call<List<ExamFormWithBillDto>> getAllBills() {
        return examFormWithBillDtoApiService.getAllBills(BILL_SELECT, "ngay_kham.desc");
    }

    public Call<List<ExamFormWithBillDto>> getBillsByPatientId(String patientId) {
        return examFormWithBillDtoApiService.getBillsByPatientId("eq." + patientId, BILL_SELECT, "ngay_kham.desc");
    }

    public Call<List<ExamFormWithBillDto>> getBillsByDateRange(String fromDateIso, String toDateIso) {
        String dateRangeFilter = "(ngay_kham.gte." + fromDateIso + ",ngay_kham.lte." + toDateIso + ")";
        return examFormWithBillDtoApiService.getBillsByDateRange(dateRangeFilter, BILL_SELECT, "ngay_kham.desc");
    }

    public Call<Void> updateBill(long billId, String paymentMethod, String invoiceStatus, double totalAmount) {
        String idFilter = "eq." + billId;
        BillUpdatePayload payload = new BillUpdatePayload(paymentMethod, invoiceStatus, totalAmount);

        return billUpdatePayloadApiService.updateBillStatus(idFilter, payload, "return=minimal");
    }
}
