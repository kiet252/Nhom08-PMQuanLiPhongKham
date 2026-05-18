package dashboard_fragment.staff_manage_bill;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.staff_manage_bill.get_bills_logic.BillApiService;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;
import retrofit2.Call;
import retrofit2.Retrofit;

public class BillRepository {
  private final BillApiService billApiService;

  public BillRepository(Context context) {
    Retrofit client = SupabaseClientProvider.getClient(context);
    this.billApiService = client.create(BillApiService.class);
  }

  public Call<List<ExamFormWithBillDto>> getBillsByPatientId(String patientId) {
    String select =
        "id,ngay_kham,"
            + "patient:patient!examination_form_patient_id_fkey(ho_ten),"
            + "medical_record!inner("
            + "bill!inner(id,created_at,phuong_thuc_thanh_toan,trang_thai_thanh_toan,tong_thanh_toan)"
            + ")";

    return billApiService.getBillsByPatientId("eq." + patientId, select, "ngay_kham.desc");
  }
}
