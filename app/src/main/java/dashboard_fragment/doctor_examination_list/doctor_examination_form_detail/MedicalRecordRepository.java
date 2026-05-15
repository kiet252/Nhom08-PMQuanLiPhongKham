package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.FullMedicalRecordResponse;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordJoinDiagnosisJoinPrescriptionApiService;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db.SaveFullMedicalRecordApiService;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db.SaveFullMedicalRecordRequest;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MedicalRecordRepository {
    private final MedicalRecordJoinDiagnosisJoinPrescriptionApiService MedicalJoinDiagnosisJoinPrescriptionApiService;
    private final SaveFullMedicalRecordApiService saveFullMedicalRecordApiService;

    public MedicalRecordRepository(Context context) {
        Retrofit client = SupabaseClientProvider.getClient(context);
        this.MedicalJoinDiagnosisJoinPrescriptionApiService = client.create(MedicalRecordJoinDiagnosisJoinPrescriptionApiService.class);
        this.saveFullMedicalRecordApiService = client.create(SaveFullMedicalRecordApiService.class);
    }

    public Call<List<FullMedicalRecordResponse>> getMedicalRecordJoinDiagnosisJoinPrescription(String exFormId) {
        String filter = "eq." + exFormId;

        String selectFields = "id,chan_doan_chinh,chan_doan_bo_sung,ghi_chu_lam_sang,medical_record_clinical(clinical(id,ten_dich_vu,don_gia)),medical_record_medicine(so_luong,lieu_dung,tan_suat,thoi_gian,medicine(id,ten_thuoc,don_vi))";

        return MedicalJoinDiagnosisJoinPrescriptionApiService.getFullMedicalRecord(
                filter,
                selectFields
        );
    }

    public Call<Void> saveFullMedicalRecord(SaveFullMedicalRecordRequest request) {
        return saveFullMedicalRecordApiService.saveFullMedicalRecord(request);
    }
}
