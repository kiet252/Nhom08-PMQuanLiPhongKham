package doctor_patient_list;

public class doctor_patient_record {
    private String patientName;
    private String patientPhone;

    public doctor_patient_record(String patientName, String patientId, String status) {
        this.patientName = patientName;
        this.patientPhone = patientId;
    }

    // Getters
    public String getPatientName() { return patientName; }
    public String getPatientId() { return patientPhone; }
}