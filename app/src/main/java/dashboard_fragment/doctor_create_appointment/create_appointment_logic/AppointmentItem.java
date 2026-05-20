package dashboard_fragment.doctor_create_appointment.create_appointment_logic;

public class AppointmentItem {
    private Long id;
    private String patient_id;
    private String doctor_id;
    private String ngay_hen;
    private String ghi_chu;

    public Long getId() {
        return id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public String getNgay_hen() {
        return ngay_hen;
    }

    public String getGhi_chu() {
        return ghi_chu;
    }
}
