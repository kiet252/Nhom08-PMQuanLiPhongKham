package dashboard_fragment.create_examination_form;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;


import java.util.List;


import dashboard_fragment.create_examination_form.create_ex_form_logic.CreateExFormRequest;
import dashboard_fragment.create_examination_form.create_ex_form_logic.ExFormApiCreateService;
import retrofit2.Call;

public class ExaminationFormRepository {
    private static ExFormApiCreateService createService;
    private final String apiKey;
    public ExaminationFormRepository(String apiKey){
        this.apiKey = apiKey;
        if (createService == null) {
            createService = SupabaseClientProvider.getClient().create(ExFormApiCreateService.class);
        }
    }
    public Call<List<ExaminationForm>> createForm(String accessToken, CreateExFormRequest newForm) {
        return createService.createForm(
                apiKey,
                "Bearer " + accessToken,
                "return=representation",
                newForm
        );
    }
}
