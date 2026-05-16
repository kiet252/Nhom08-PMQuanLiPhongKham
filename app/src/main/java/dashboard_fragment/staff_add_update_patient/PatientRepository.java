package dashboard_fragment.staff_add_update_patient;
import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.List;

import dashboard_fragment.doctor_view_medical_record.ExaminationFormHistoryResponse;

import dashboard_fragment.staff_add_update_patient.add_patient_logic.CreatePatientRequest;
import dashboard_fragment.staff_add_update_patient.add_patient_logic.PatientApiCreateService;
import dashboard_fragment.staff_add_update_patient.update_patient_logic.find_patient_logic.PatientApiGetService;
import dashboard_fragment.staff_add_update_patient.update_patient_logic.update_patient_logic.PatientApiUpdateService;
import dashboard_fragment.staff_add_update_patient.update_patient_logic.update_patient_logic.UpdatePatientRequest;
import dashboard_fragment.doctor_view_medical_record.MedicalRecordByPatientApiService;
import dashboard_fragment.doctor_view_medical_record.MedicalRecordByPatientResponse;
import retrofit2.Call;
import retrofit2.Retrofit;

public class PatientRepository {
    private final PatientApiGetService getService;
    private final PatientApiCreateService createService;
    private final PatientApiUpdateService updateService;
    private final MedicalRecordByPatientApiService medicalRecordApiService;
    private final Context context;

    public PatientRepository(Context context) {
        this.context = context;

        // Use the context to get the Retrofit client
        Retrofit client = SupabaseClientProvider.getClient(context);

        this.getService = client.create(PatientApiGetService.class);
        this.createService = client.create(PatientApiCreateService.class);
        this.updateService = client.create(PatientApiUpdateService.class);
        this.medicalRecordApiService = client.create(MedicalRecordByPatientApiService.class);
    }

    public Call<List<PatientProfile>> getProfileByIdOrCccd(String input) {
        String orFilter = "(id.eq." + input + ",cccd.eq." + input + ")";
        return getService.getProfileByIdOrCccd(orFilter, "*");
    }

    public Call<List<PatientProfile>> createProfile(CreatePatientRequest newProfile) {
        return createService.createProfile("return=representation", newProfile);
    }

    public Call<Void> updateProfile(String patientId, UpdatePatientRequest updatedProfile) {
        String idFilter = "eq." + patientId;
        return updateService.updatePatient(idFilter, updatedProfile);
    }

    public Call<List<dashboard_fragment.doctor_view_medical_record.MedicalRecordByPatientResponse>> getMedicalRecordsByPatientId(String patientId) {
        String filter = "eq." + patientId;
        String selectFields = "id,chan_doan_chinh,chan_doan_bo_sung,ghi_chu_lam_sang,doctor_id," +
                "examination_form!inner(id,patient_id,ngay_kham,trang_thai)," +
                "medical_record_clinical(clinical(id,ten_dich_vu,don_gia))," +
                "medical_record_medicine(so_luong,lieu_dung,ghi_chu,tan_suat,thoi_gian,medicine(id,ten_thuoc,don_vi))";
        
        return medicalRecordApiService.getMedicalRecordsByPatientId(filter, selectFields);
    }

    public Call<List<ExaminationFormHistoryResponse>> getExaminationFormHistoryByPatientId(String patientId) {
        String filterPatient = "eq." + patientId;
        String filterStatus = "eq.Đã khám";
        String selectFields = "id,ngay_kham,trang_thai,trieu_chung_ban_dau,profiles!inner(ho_ten)," +
                "medical_record(id,chan_doan_chinh,chan_doan_bo_sung,ghi_chu_lam_sang," +
                "medical_record_clinical(clinical(id,ten_dich_vu,don_gia))," +
                "medical_record_medicine(so_luong,lieu_dung,ghi_chu,tan_suat,thoi_gian,medicine(id,ten_thuoc,don_vi)))";
        String order = "ngay_kham.desc";
        
        return medicalRecordApiService.getExaminationFormHistoryByPatientId(filterPatient, filterStatus, selectFields, order);
    }
}
