package dashboard_fragment.create_examination_form;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;


import java.util.List;


import dashboard_fragment.add_update_patient.PatientDatabaseConstraintsChecker;
import dashboard_fragment.create_examination_form.create_ex_form_logic.CreateExFormRequest;
import dashboard_fragment.create_examination_form.create_ex_form_logic.ExFormApiCreateService;
import dashboard_fragment.create_examination_form.get_ex_form_logic.ExFormApiGetByDateService;
import dashboard_fragment.manage_examination_form.get_all_ex_form_logic.ExFormApiGetFormsByPatientCCCDOrIDService;
import retrofit2.Call;

public class ExaminationFormRepository {
    private static ExFormApiCreateService createService;
    private static ExFormApiGetByDateService getByDateService;
    private static ExFormApiGetFormsByPatientCCCDOrIDService getFormByPatientCCCDOrIDService;
    private final String apiKey;
    public ExaminationFormRepository(String apiKey){
        this.apiKey = apiKey;
        if (createService == null) {
            createService = SupabaseClientProvider.getClient().create(ExFormApiCreateService.class);
        }

        if (getByDateService == null) {
            getByDateService = SupabaseClientProvider.getClient().create(ExFormApiGetByDateService.class);
        }

        if (getFormByPatientCCCDOrIDService == null) {
            getFormByPatientCCCDOrIDService = SupabaseClientProvider.getClient().create(ExFormApiGetFormsByPatientCCCDOrIDService.class);
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
    public Call<List<ExaminationForm>> getFormsByDate(String accessToken, String ngayKham) {
        return getByDateService.getFormsByDate(
                apiKey,
                "Bearer " + accessToken,
                "eq." + ngayKham,
                "so_tiep_nhan",
                "so_tiep_nhan.desc"
        );
    }

    public Call<List<ExaminationForm>> getAllFormsByPatientCCCDOrID(String accessToken, String searchValue) {
        boolean isNumeric = searchValue != null && searchValue.matches("^\\d+$");
        boolean isCccd = PatientDatabaseConstraintsChecker.isValidCCCDInDB(searchValue);
        boolean isId = isNumeric && !isCccd;

        return getFormByPatientCCCDOrIDService.getFormsByCCCDOrID(
                apiKey,
                "Bearer " + accessToken,
                "*,patient:patient_id!inner(id,cccd,ho_ten)", // change patient_id to your real FK column
                isCccd ? "eq." + searchValue : null,
                isId ? "eq." + searchValue : null,
                "gio_du_kien.asc"
        );
    }
}
