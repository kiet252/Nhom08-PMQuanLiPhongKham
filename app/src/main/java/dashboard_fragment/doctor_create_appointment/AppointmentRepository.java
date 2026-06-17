package dashboard_fragment.doctor_create_appointment;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_create_appointment.create_appointment_logic.AppointmentApiService;
import dashboard_fragment.doctor_create_appointment.create_appointment_logic.AppointmentItem;
import dashboard_fragment.doctor_create_appointment.create_appointment_logic.CreateAppointmentRequest;
import retrofit2.Call;

public class AppointmentRepository {
    private final AppointmentApiService apiService;
    public AppointmentRepository(Context context) {
        apiService = SupabaseClientProvider.getClient(context).create(AppointmentApiService.class);
    }
    public Call<Void> createAppointment(CreateAppointmentRequest request) {
        return apiService.createAppointment(request);
    }
    public Call<List<AppointmentItem>> getAppointmentsByPatientId(String patientId) {
        return apiService.getAppointmentsByPatientId(
                "eq." + patientId,
                "id,patient_id,doctor_id,ngay_hen,ghi_chu",
                "ngay_hen.desc"
        );
    }
    public Call<List<AppointmentItem>> getAllAppointments() {
        return apiService.getAppointmentsByPatientId(
                null,
                "id,patient_id,doctor_id,ngay_hen,ghi_chu",
                "ngay_hen.desc"
        );
    }

    public Call<List<AppointmentItem>> getAppointmentsByDateRange(String startDate, String endDate) {
        String filter = "(ngay_hen.gte." + startDate + ",ngay_hen.lte." + endDate + ")";

        return apiService.getAppointmentsByDateRange(
                filter,
                "id,patient_id,doctor_id,ngay_hen,ghi_chu",
                "ngay_hen.desc"
        );
    }

    public Call<List<AppointmentItem>> getAppointmentsByDate(
            String date
    ) {
        return apiService.getAppointmentsByDate(
                "eq." + date,
                "id,patient_id,doctor_id,ngay_hen,ghi_chu",
                "ngay_hen.desc"
        );
    }
}
