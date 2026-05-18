package dashboard_fragment.doctor_create_appointment.create_appointment_logic;

public class CreateAppointmentRequest {
    private String patient_id;
    private String doctor_id;
    private String ngay_hen;
    private String ghi_chu;
    public CreateAppointmentRequest(String patient_id, String doctor_id, String ngay_hen, String ghi_chu) {
        this.patient_id = patient_id;
        this.doctor_id = doctor_id;
        this.ngay_hen = ngay_hen;
        this.ghi_chu = ghi_chu;
    }
}