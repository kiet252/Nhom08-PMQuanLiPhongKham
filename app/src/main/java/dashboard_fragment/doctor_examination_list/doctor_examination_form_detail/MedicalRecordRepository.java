package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalApiService;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.FullMedicalRecordResponse;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordJoinDiagnosisJoinPrescriptionApiService;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MedicalRecordRepository {
    private final MedicalRecordJoinDiagnosisJoinPrescriptionApiService MedicalJoinDiagnosisJoinPrescriptionApiService;

    public MedicalRecordRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);
        this.MedicalJoinDiagnosisJoinPrescriptionApiService = client.create(MedicalRecordJoinDiagnosisJoinPrescriptionApiService.class);
    }

    public Call<List<FullMedicalRecordResponse>> getMedicalRecordJoinDiagnosisJoinPrescription(String exFormId) {
        String filter = "eq." + exFormId;

        String selectFields = "id,chan_doan_chinh,chan_doan_bo_sung,ghi_chu_lam_sang,medical_record_clinical(clinical(id,ten_dich_vu,don_gia)),medical_record_medicine(so_luong,lieu_dung,medicine(id,ten_thuoc,don_vi))";

        return MedicalJoinDiagnosisJoinPrescriptionApiService.getFullMedicalRecord(
                filter,
                selectFields
        );
    }
}
