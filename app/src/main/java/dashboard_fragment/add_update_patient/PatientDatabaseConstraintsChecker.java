package dashboard_fragment.add_update_patient;

public final class PatientDatabaseConstraintsChecker {
    private PatientDatabaseConstraintsChecker() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isValidCCCDInDB(String cccd) {
        return cccd != null && cccd.matches("^\\d{12}$");
    }

    public static boolean isValidPhoneNumInDB(String phoneNum) {
        return phoneNum != null && phoneNum.matches("^0\\d{9}$");
    }
}