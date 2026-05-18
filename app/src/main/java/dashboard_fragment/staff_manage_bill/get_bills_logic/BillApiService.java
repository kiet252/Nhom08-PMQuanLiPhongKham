package dashboard_fragment.staff_manage_bill.get_bills_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BillApiService {
  /**
   * Loads bills for a patient via examination_form.patient_id (direct column filter).
   * Chain: examination_form → medical_record → bill
   */
  @GET("rest/v1/examination_form")
  Call<List<ExamFormWithBillDto>> getBillsByPatientId(
      @Query("patient_id") String patientIdFilter,
      @Query(value = "select", encoded = true) String select,
      @Query("order") String order);
}
