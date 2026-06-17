package dashboard_fragment.doctor_create_appointment.create_appointment_logic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AppointmentApiService {
    @POST("rest/v1/appointment")
    Call<Void> createAppointment(@Body CreateAppointmentRequest request);

    @GET("rest/v1/appointment")
    Call<List<AppointmentItem>> getAppointmentsByPatientId(
            @Query("patient_id") String patientId,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/appointment")
    Call<List<AppointmentItem>> getAppointmentsByDateRange(
            @Query("and") String dateRange,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/appointment")
    Call<List<AppointmentItem>> getAppointmentsByDate(
            @Query("ngay_hen") String ngayHen,
            @Query("select") String select,
            @Query("order") String order
    );
}
