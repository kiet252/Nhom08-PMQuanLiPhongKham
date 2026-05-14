package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.FullMedicalRecordResponse;

public class DoctorExDetailViewModel extends ViewModel {
    private final MutableLiveData<FullMedicalRecordResponse> medicalRecord = new MutableLiveData<>();

    public LiveData<FullMedicalRecordResponse> getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(FullMedicalRecordResponse record) {
        medicalRecord.setValue(record);
    }
}
