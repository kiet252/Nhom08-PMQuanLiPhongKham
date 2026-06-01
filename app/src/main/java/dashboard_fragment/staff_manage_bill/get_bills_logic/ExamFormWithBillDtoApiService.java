package dashboard_fragment.staff_manage_bill.get_bills_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExamFormWithBillDtoApiService {
  @GET("rest/v1/examination_form")
  Call<List<ExamFormWithBillDto>> getAllBills(
      @Query(value = "select", encoded = true) String select,
      @Query("order") String order);

  @GET("rest/v1/examination_form")
  Call<List<ExamFormWithBillDto>> getBillsByPatientId(
      @Query("patient_id") String patientIdFilter,
      @Query(value = "select", encoded = true) String select,
      @Query("order") String order);

  @GET("rest/v1/examination_form")
  Call<List<ExamFormWithBillDto>> getBillsByDateRange(
      @Query("and") String dateRangeFilter,
      @Query(value = "select", encoded = true) String select,
      @Query("order") String order);
}
