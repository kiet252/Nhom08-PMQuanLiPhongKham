package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.diagnosis_logic;

public class DiagnosisOption {
    private final String groupName;
    private final String diagnosisName;

    public DiagnosisOption(String groupName, String diagnosisName) {
        this.groupName = groupName;
        this.diagnosisName = diagnosisName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDiagnosisName() {
        return diagnosisName;
    }
}
