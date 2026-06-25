package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.FullMedicalRecordResponse;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem;

public class DoctorExDetailViewModel extends ViewModel {
    private final MutableLiveData<FullMedicalRecordResponse> medicalRecord = new MutableLiveData<>();
    private final MutableLiveData<Integer> diagnosisInputVersion = new MutableLiveData<>(0);
    private final LinkedHashSet<Integer> selectedClinicalIds = new LinkedHashSet<>();
    private final LinkedHashSet<String> selectedDiagnoses = new LinkedHashSet<>();
    private final ArrayList<PrescriptionItem> selectedMedicines = new ArrayList<>();
    private boolean clinicalSelectionInitialized;
    private boolean diagnosisSelectionInitialized;
    private boolean prescriptionSelectionInitialized;
    private String additionalDiagnosis = "";
    private String clinicalNote = "";

    public LiveData<FullMedicalRecordResponse> getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(FullMedicalRecordResponse record) {
        medicalRecord.setValue(record);
    }

    public FullMedicalRecordResponse getMedicalRecordValue() {
        return medicalRecord.getValue();
    }

    public LiveData<Integer> getDiagnosisInputVersion() {
        return diagnosisInputVersion;
    }

    private void notifyDiagnosisInputChanged() {
        Integer current = diagnosisInputVersion.getValue();
        diagnosisInputVersion.setValue(current == null ? 1 : current + 1);
    }

    public boolean hasClinicalSelectionInitialized() {
        return clinicalSelectionInitialized;
    }

    public void initializeSelectedClinicalIds(Collection<Integer> ids) {
        if (clinicalSelectionInitialized) {
            return;
        }

        selectedClinicalIds.clear();
        if (ids != null) {
            selectedClinicalIds.addAll(ids);
        }
        clinicalSelectionInitialized = true;
    }

    public boolean isClinicalSelected(int clinicalId) {
        return selectedClinicalIds.contains(clinicalId);
    }

    public void setClinicalSelected(int clinicalId, boolean selected) {
        clinicalSelectionInitialized = true;
        if (selected) {
            selectedClinicalIds.add(clinicalId);
        } else {
            selectedClinicalIds.remove(clinicalId);
        }
    }

    public Set<Integer> getSelectedClinicalIds() {
        return new LinkedHashSet<>(selectedClinicalIds);
    }

    public boolean hasDiagnosisSelectionInitialized() {
        return diagnosisSelectionInitialized;
    }

    public void initializeSelectedDiagnoses(Collection<String> diagnoses) {
        if (diagnosisSelectionInitialized) {
            return;
        }

        selectedDiagnoses.clear();
        if (diagnoses != null) {
            for (String diagnosis : diagnoses) {
                if (diagnosis != null && !diagnosis.trim().isEmpty()) {
                    selectedDiagnoses.add(diagnosis.trim());
                }
            }
        }
        diagnosisSelectionInitialized = true;
        notifyDiagnosisInputChanged();
    }

    public void setSelectedDiagnoses(Collection<String> diagnoses) {
        selectedDiagnoses.clear();
        if (diagnoses != null) {
            for (String diagnosis : diagnoses) {
                if (diagnosis != null && !diagnosis.trim().isEmpty()) {
                    selectedDiagnoses.add(diagnosis.trim());
                }
            }
        }
        diagnosisSelectionInitialized = true;
        notifyDiagnosisInputChanged();
    }

    public Set<String> getSelectedDiagnoses() {
        return new LinkedHashSet<>(selectedDiagnoses);
    }

    public String getAdditionalDiagnosis() {
        return additionalDiagnosis;
    }

    public void setAdditionalDiagnosis(String additionalDiagnosis) {
        this.additionalDiagnosis = additionalDiagnosis;
        notifyDiagnosisInputChanged();
    }

    public String getClinicalNote() {
        return clinicalNote;
    }

    public void setClinicalNote(String clinicalNote) {
        this.clinicalNote = clinicalNote;
        notifyDiagnosisInputChanged();
    }

    public boolean hasPrescriptionSelectionInitialized() {
        return prescriptionSelectionInitialized;
    }

    public void initializeSelectedMedicines(Collection<PrescriptionItem> medicines) {
        if (prescriptionSelectionInitialized) {
            return;
        }
        replaceSelectedMedicines(medicines);
    }

    public void replaceSelectedMedicines(Collection<PrescriptionItem> medicines) {
        selectedMedicines.clear();
        if (medicines != null) {
            selectedMedicines.addAll(medicines);
        }
        prescriptionSelectionInitialized = true;
    }

    public List<PrescriptionItem> getSelectedMedicines() {
        return selectedMedicines;
    }
}
