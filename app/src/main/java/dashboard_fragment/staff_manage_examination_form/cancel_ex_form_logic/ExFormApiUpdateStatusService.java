package dashboard_fragment.staff_manage_examination_form.cancel_ex_form_logic;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ExFormApiUpdateStatusService {
    @PATCH("rest/v1/examination_form")
    Call<Void> updateExaminationFormStatus(
            @Query("id") String idFilter,
            @Body CancelExaminationFormRequest body
    );
}
