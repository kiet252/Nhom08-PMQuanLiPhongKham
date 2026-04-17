package dashboard_fragment.create_examination_form.create_ex_form_logic;

import java.util.List;

import dashboard_fragment.create_examination_form.ExaminationForm;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExFormApiCreateService {
    @POST("rest/v1/examination_form")
    Call<List<ExaminationForm>> createForm(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Body CreateExFormRequest newForm
    );
    @GET("rest/v1/examination_form")
    Call<List<ExaminationForm>> getFormsByDate(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("ngay_kham") String ngayKham,
            @Query("select") String select,
            @Query("order") String order
    );
}
