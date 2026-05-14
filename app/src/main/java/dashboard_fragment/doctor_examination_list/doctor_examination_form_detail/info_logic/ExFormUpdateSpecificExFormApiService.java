package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.info_logic;

import dashboard_fragment.staff_create_examination_form.ExaminationForm;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ExFormUpdateSpecificExFormApiService {
    @PATCH("rest/v1/examination_form")
    Call<Void> updateExaminationStatus(
            @Query("id") String idFilter,
            @Body ExaminationFormSpecificFormUpdateBody body
    );
}
