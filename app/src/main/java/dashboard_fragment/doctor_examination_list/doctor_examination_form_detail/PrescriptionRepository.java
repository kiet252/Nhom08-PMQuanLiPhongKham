package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineApiService;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;
import retrofit2.Call;
import retrofit2.Retrofit;

public class PrescriptionRepository {

    private final MedicineApiService medicineApiService;

    public PrescriptionRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);
        this.medicineApiService = client.create(MedicineApiService.class);
    }

    public Call<List<MedicineItem>> getAllMedicines() {
        return medicineApiService.getAllMedicines(
                "id,ten_thuoc,hoat_chat,ham_luong,don_vi,ton_kho,chuc_nang,don_gia",
                "ten_thuoc.asc"
        );
    }
}
