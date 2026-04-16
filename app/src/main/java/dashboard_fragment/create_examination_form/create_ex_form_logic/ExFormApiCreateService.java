package dashboard_fragment.create_examination_form.create_ex_form_logic;

import java.util.List;

import dashboard_fragment.create_examination_form.ExaminationForm;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ExFormApiCreateService {
    @POST("rest/v1/examination_form")
    Call<List<ExaminationForm>> createForm(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Body CreateExFormRequest newForm
    );
}
