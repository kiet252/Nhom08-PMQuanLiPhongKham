package dashboard_fragment.doctor_view_medical_record;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MedicalRecordByPatientApiService {
    @GET("rest/v1/medical_record")
    Call<List<dashboard_fragment.doctor_view_medical_record.MedicalRecordByPatientResponse>> getMedicalRecordsByPatientId(
            @Query("examination_form.patient_id") String patientIdFilter,
            @Query("select") String selectFields
    );

    @GET("rest/v1/examination_form")
    Call<List<ExaminationFormHistoryResponse>> getExaminationFormHistoryByPatientId(
            @Query("patient_id") String patientIdFilter,
            @Query("trang_thai") String trangThaiFilter,
            @Query("select") String selectFields,
            @Query("order") String order
    );
}
