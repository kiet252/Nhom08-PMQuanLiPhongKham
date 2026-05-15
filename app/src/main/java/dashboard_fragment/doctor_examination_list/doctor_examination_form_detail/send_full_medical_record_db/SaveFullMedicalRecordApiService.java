package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SaveFullMedicalRecordApiService {
    @POST("rest/v1/rpc/save_full_medical_record")
    Call<Void> saveFullMedicalRecord(@Body SaveFullMedicalRecordRequest body);
}
