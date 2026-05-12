package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalApiService;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ClinicalRepository {

    private final ClinicalApiService clinicalApiService;

    public ClinicalRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);
        this.clinicalApiService = client.create(ClinicalApiService.class);
    }

    public Call<List<ClinicalItem>> getAllClinicalItems() {
        return clinicalApiService.getAllClinicalItems(
                "id,ten_dich_vu,loai_dich_vu,mo_ta",
                "id.asc"
        );
    }
}
