package dashboard_fragment.create_examination_form;

import android.content.Context;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import java.util.List;
import dashboard_fragment.create_examination_form.create_ex_form_logic.CreateExFormRequest;
import dashboard_fragment.create_examination_form.create_ex_form_logic.ExFormApiCreateService;
import dashboard_fragment.create_examination_form.get_ex_form_logic.ExFormApiGetByDateService;
import dashboard_fragment.manage_examination_form.get_all_ex_form_logic.ExFormApiGetFormsWithPatient;
import dashboard_fragment.manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ExaminationFormRepository {
    private final ExFormApiCreateService createService;
    private final ExFormApiGetByDateService getByDateService;
    private final ExFormApiGetFormsWithPatient getFormByPatientCCCDOrIDService;
    private final Context context;

    public ExaminationFormRepository(Context context){
        this.context = context;

        Retrofit client = SupabaseClientProvider.getClient(context);

        this.createService = client.create(ExFormApiCreateService.class);
        this.getByDateService = client.create(ExFormApiGetByDateService.class);
        this.getFormByPatientCCCDOrIDService = client.create(ExFormApiGetFormsWithPatient.class);
    }

    public Call<List<ExaminationForm>> createForm(CreateExFormRequest newForm) {
        return createService.createForm("return=representation", newForm);
    }

    public Call<List<ExaminationForm>> getFormsByDate(String ngayKham) {
        return getByDateService.getFormsByDate(
                "eq." + ngayKham,
                "so_tiep_nhan",
                "so_tiep_nhan.desc"
        );
    }

    public Call<List<ExaminationFormWithPatientDto>> getAllFormsWithPatient() {
        return getFormByPatientCCCDOrIDService.getAllFormsWithPatientAndDoctor(
                "*,patient:patient!examination_form_patient_id_fkey(id,cccd,ho_ten,ngay_sinh,dia_chi),doctor:profiles!examination_form_doctor_id_fkey(id,ho_ten,chuc_vu)",
                "gio_du_kien.asc",
                "not.in.(Vắng,Đã hủy,Đã khám)"
        );
    }
}