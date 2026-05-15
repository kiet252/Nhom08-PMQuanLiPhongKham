package dashboard_fragment.staff_create_examination_form;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_examination_list.ExFormApiGetAllExFormToday;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.info_logic.ExFormUpdateSpecificExFormApiService;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.info_logic.ExaminationFormSpecificFormUpdateBody;
import dashboard_fragment.staff_create_examination_form.create_ex_form_logic.CreateExFormRequest;
import dashboard_fragment.staff_create_examination_form.create_ex_form_logic.ExFormApiCreateService;
import dashboard_fragment.staff_create_examination_form.get_ex_form_logic.ExFormApiGetByDateService;
import dashboard_fragment.staff_manage_examination_form.cancel_ex_form_logic.CancelExaminationFormRequest;
import dashboard_fragment.staff_manage_examination_form.cancel_ex_form_logic.ExFormApiUpdateStatusService;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExFormApiGetFormsWithPatient;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ExaminationFormRepository {
    private final ExFormApiCreateService createService;
    private final ExFormApiGetByDateService getByDateService;
    private final ExFormApiGetFormsWithPatient getFormByPatientCCCDOrIDService;
    private final ExFormApiUpdateStatusService updateStatusService;
    private final ExFormApiGetAllExFormToday getAllExFormToday;
    private final ExFormUpdateSpecificExFormApiService updateSpecificExForm;

    public ExaminationFormRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);

        this.createService = client.create(ExFormApiCreateService.class);
        this.getByDateService = client.create(ExFormApiGetByDateService.class);
        this.getFormByPatientCCCDOrIDService = client.create(ExFormApiGetFormsWithPatient.class);
        this.updateStatusService = client.create(ExFormApiUpdateStatusService.class);
        this.getAllExFormToday = client.create(ExFormApiGetAllExFormToday.class);
        this.updateSpecificExForm = client.create(ExFormUpdateSpecificExFormApiService.class);
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
                "*,patient:patient!examination_form_patient_id_fkey(id,cccd,ho_ten,gioi_tinh,so_dien_thoai,ngay_sinh,dia_chi),doctor:profiles!examination_form_doctor_id_fkey(id,ho_ten,chuc_vu)",
                "gio_du_kien.asc",
                "not.in.(Vắng,Đã hủy,Đã khám)"
        );
    }

    public Call<Void> cancelForm(String formId) {
        return updateStatusService.updateExaminationFormStatus(
                "eq." + formId,
                new CancelExaminationFormRequest("Đã hủy")
        );
    }

    public Call<List<ExaminationFormWithPatientDto>> getAllFormsToday() {
        return getAllFormsToday(null);

    }

    public Call<List<ExaminationFormWithPatientDto>> getAllFormsToday(String currentProfileId) {
        String doctorFilter = (currentProfileId == null) ? null : "eq." + currentProfileId;

        return getAllExFormToday.getFormsByDate(
                null,
                "*,patient:patient!examination_form_patient_id_fkey(id,cccd,ho_ten,gioi_tinh,so_dien_thoai,ngay_sinh,dia_chi),doctor:profiles!examination_form_doctor_id_fkey(id,ho_ten,chuc_vu)",
                "ngay_kham.desc,gio_du_kien.asc",
                "not.in.(Vắng,Đã hủy)",
                doctorFilter
        );
    }

    public Call<Void> patchExaminationForm(String id, String newStatus, String newSymptoms) {
        String filter = "eq." + id;

        ExaminationFormSpecificFormUpdateBody updateData = new ExaminationFormSpecificFormUpdateBody(newStatus, newSymptoms);

        return updateSpecificExForm.updateExaminationStatus(filter, updateData);
    }
}
