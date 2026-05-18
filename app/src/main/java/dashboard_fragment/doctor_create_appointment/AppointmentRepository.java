package dashboard_fragment.doctor_create_appointment;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import dashboard_fragment.doctor_create_appointment.create_appointment_logic.AppointmentApiService;
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
}
