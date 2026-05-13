package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import java.util.Locale;

public enum DoctorExaminationStatus {
    WAITING("Chờ khám"),
    IN_PROGRESS("Đang khám"),
    DONE("Đã khám");

    private final String displayName;

    DoctorExaminationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DoctorExaminationStatus fromValue(String value) {
        String normalized = normalize(value);
        for (DoctorExaminationStatus status : values()) {
            if (normalize(status.displayName).equals(normalized)) {
                return status;
            }
        }
        return WAITING;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
