package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MedicalRecordJoinDiagnosisJoinPrescriptionApiService {
    @GET("rest/v1/medical_record")
    Call<List<FullMedicalRecordResponse>> getFullMedicalRecord(
            @Query("ex_form_id") String exFormIdFilter,
            @Query("select") String selectFields
    );
}
