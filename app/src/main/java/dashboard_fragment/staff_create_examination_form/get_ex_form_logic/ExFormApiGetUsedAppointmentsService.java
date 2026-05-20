package dashboard_fragment.staff_create_examination_form.get_ex_form_logic;

import java.util.List;

import dashboard_fragment.staff_create_examination_form.ExaminationForm;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExFormApiGetUsedAppointmentsService {
    @GET("rest/v1/examination_form")
    Call<List<ExaminationForm>> getFormsByPatientId(
            @Query("patient_id") String patientId,
            @Query("appointment_id") String appointmentFilter,
            @Query("select") String select,
            @Query("order") String order
    );
}
