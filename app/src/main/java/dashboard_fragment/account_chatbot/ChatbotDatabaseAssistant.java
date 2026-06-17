package dashboard_fragment.account_chatbot;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dashboard_fragment.UserRole;
import dashboard_fragment.doctor_create_appointment.AppointmentRepository;
import dashboard_fragment.doctor_create_appointment.create_appointment_logic.AppointmentItem;
import dashboard_fragment.doctor_examination_list.ExFormApiGetAllExFormToday;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.ClinicalRepository;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.PrescriptionRepository;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordClinicalWrapper;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordMedicineWrapper;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;
import dashboard_fragment.doctor_view_medical_record.MedicalRecordByPatientResponse;
import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import dashboard_fragment.staff_manage_bill.BillRepository;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatbotDatabaseAssistant {

    public interface AnswerCallback {
        void onAnswer(String json);
    }

    public enum IntentType {
        PATIENT_PROFILE,
        APPOINTMENT_BY_PATIENT,
        MEDICAL_RECORD_BY_PATIENT,
        DOCTOR_SCHEDULE,
        BILL_BY_PATIENT,
        BILL_BY_DATE,
        MEDICINE_LOOKUP,
        CLINICAL_LOOKUP,
        COUNT_PATIENTS,
        COUNT_EXAMINATIONS,
        COUNT_BILLS,
        COUNT_APPOINTMENTS,
        LIST_PATIENTS,
        LIST_APPOINTMENTS,
        LIST_EXAMINATIONS,
        LIST_BILLS,
        TIMEKEEPING,
        INVENTORY_STATS
    }

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    public static class QuerySpec {

        private IntentType intent;

        private String patientId;
        private String patientName;
        private String cccd;

        private String doctorId;

        private String medicineName;
        private String clinicalName;

        private String date;
        private String startDate;
        private String endDate;

        private String status;

        public IntentType getIntent() {
            return intent;
        }

        public void setIntent(IntentType intent) {
            this.intent = intent;
        }

        public String getPatientId() {
            return patientId;
        }

        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public String getPatientName() {
            return patientName;
        }

        public void setPatientName(String patientName) {
            this.patientName = patientName;
        }

        public String getCccd() {
            return cccd;
        }

        public void setCccd(String cccd) {
            this.cccd = cccd;
        }

        public String getDoctorId() {
            return doctorId;
        }

        public void setDoctorId(String doctorId) {
            this.doctorId = doctorId;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getClinicalName() {
            return clinicalName;
        }

        public void setClinicalName(String clinicalName) {
            this.clinicalName = clinicalName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public static QuerySpec fromJson(String json) {
            return gson.fromJson(json, QuerySpec.class);
        }

        public String toJson() {
            return gson.toJson(this);
        }
    }

    public static class DateParts {
        private final String isoDate;
        private final String displayDate;

        public DateParts(String isoDate, String displayDate) {
            this.isoDate = isoDate;
            this.displayDate = displayDate;
        }

        public String getIsoDate() {
            return isoDate;
        }

        public String getDisplayDate() {
            return displayDate;
        }
    }

    private static final String EXAM_SELECT =
            "*,patient:patient!examination_form_patient_id_fkey(id,cccd,ho_ten,gioi_tinh,so_dien_thoai,ngay_sinh,dia_chi)," +
                    "doctor:profiles!examination_form_doctor_id_fkey(id,ho_ten,chuc_vu)";

    private final String roleName;
    private final String currentProfileId;

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ClinicalRepository clinicalRepository;
    private final ExFormApiGetAllExFormToday exFormApi;

    public ChatbotDatabaseAssistant(Context context, String roleName) {
        Context appContext = context.getApplicationContext();
        this.roleName = roleName == null ? UserRole.BAC_SI.name() : roleName;

        this.patientRepository = new PatientRepository(appContext);
        this.appointmentRepository = new AppointmentRepository(appContext);
        this.billRepository = new BillRepository(appContext);
        this.prescriptionRepository = new PrescriptionRepository(appContext);
        this.clinicalRepository = new ClinicalRepository(appContext);

        Retrofit retrofit = SupabaseClientProvider.getClient(appContext);
        this.exFormApi = retrofit.create(ExFormApiGetAllExFormToday.class);

        String profileId = null;
        UserProfile profile = SharedPrefManager.getInstance(appContext).getProfile();
        if (profile != null) {
            profileId = profile.getID();
        }
        this.currentProfileId = profileId;
    }

    public boolean canAccess(IntentType intent) {
        if (intent == IntentType.TIMEKEEPING) return false;
        if (UserRole.ADMIN.name().equals(roleName)) return true;

        if (UserRole.BAC_SI.name().equals(roleName)) {
            return intent == IntentType.PATIENT_PROFILE
                    || intent == IntentType.APPOINTMENT_BY_PATIENT
                    || intent == IntentType.MEDICAL_RECORD_BY_PATIENT
                    || intent == IntentType.DOCTOR_SCHEDULE
                    || intent == IntentType.MEDICINE_LOOKUP
                    || intent == IntentType.CLINICAL_LOOKUP
                    || intent == IntentType.LIST_PATIENTS
                    || intent == IntentType.LIST_APPOINTMENTS
                    || intent == IntentType.LIST_EXAMINATIONS
                    || intent == IntentType.COUNT_APPOINTMENTS
                    || intent == IntentType.COUNT_EXAMINATIONS
                    || intent == IntentType.COUNT_PATIENTS
                    || intent == IntentType.INVENTORY_STATS;
        }

        if (UserRole.NHAN_VIEN.name().equals(roleName)) {
            return intent == IntentType.PATIENT_PROFILE
                    || intent == IntentType.APPOINTMENT_BY_PATIENT
                    || intent == IntentType.DOCTOR_SCHEDULE
                    || intent == IntentType.BILL_BY_PATIENT
                    || intent == IntentType.BILL_BY_DATE
                    || intent == IntentType.LIST_PATIENTS
                    || intent == IntentType.LIST_APPOINTMENTS
                    || intent == IntentType.LIST_EXAMINATIONS
                    || intent == IntentType.LIST_BILLS
                    || intent == IntentType.COUNT_APPOINTMENTS
                    || intent == IntentType.COUNT_EXAMINATIONS
                    || intent == IntentType.COUNT_BILLS;
        }
        return false;
    }

    public String getDeniedMessage(IntentType intent) {
        if (intent == IntentType.TIMEKEEPING) {
            return "Chatbot hiện chưa hỗ trợ tra cứu bảng timekeeping/chấm công.";
        }

        return "Vai trò hiện tại chưa được phép tra cứu kiểu dữ liệu này.";
    }

    public void processNaturalLanguage(String message, AnswerCallback callback) {
        if (TextUtils.isEmpty(message)) {
            callback.onAnswer(errorJson("Câu hỏi không được để trống."));
            return;
        }

        QuerySpec spec = buildQuerySpec(message);

        if (spec == null || spec.getIntent() == null) {
            callback.onAnswer(errorJson("Không xác định được loại truy vấn từ câu hỏi này."));
            return;
        }

        execute(spec, callback);
    }
    private QuerySpec buildQuerySpec(String rawMessage) {
        String q = normalizeNL(rawMessage);
        QuerySpec spec = new QuerySpec();

        if (containsAnyNL(q, "kho thuoc", "ton kho", "thong ke kho",
                "thuoc con lai", "thuoc sap het", "thuoc het hang", "so luong thuoc")) {
            spec.setIntent(IntentType.INVENTORY_STATS);
            return spec;
        }

        if (containsAnyNL(q, "bao nhieu benh nhan", "so luong benh nhan",
                "dem benh nhan", "tong so benh nhan")) {
            spec.setIntent(IntentType.COUNT_PATIENTS);
            return spec;
        }


        if (containsAnyNL(q, "bao nhieu lich hen", "so luong lich hen",
                "dem lich hen", "tong so lich hen")) {
            spec.setIntent(IntentType.COUNT_APPOINTMENTS);
            return spec;
        }

        if (containsAnyNL(q, "bao nhieu phieu kham", "so luong phieu kham",
                "dem phieu kham", "tong so phieu kham")) {

            spec.setIntent(IntentType.COUNT_EXAMINATIONS);

            extractDateRange(rawMessage, spec);

            if (spec.getStartDate() == null) {
                spec.setDate(extractDate(rawMessage));
            }
            return spec;
        }

        if (containsAnyNL(q, "bao nhieu hoa don", "so luong hoa don",
                "dem hoa don", "tong so hoa don")) {
            spec.setIntent(IntentType.COUNT_BILLS);
            return spec;
        }

        if (containsAnyNL(q, "danh sach benh nhan", "liet ke benh nhan",
                "tat ca benh nhan")) {
            spec.setIntent(IntentType.LIST_PATIENTS);

            extractDateRange(rawMessage, spec);

            return spec;
        }

        if (containsAnyNL(q, "danh sach lich hen", "liet ke lich hen",
                "tat ca lich hen")) {

            spec.setIntent(IntentType.LIST_APPOINTMENTS);

            extractDateRange(rawMessage, spec);

            if (spec.getStartDate() == null) {
                spec.setDate(extractDate(rawMessage));
            }

            return spec;
        }

        if (containsAnyNL(q, "danh sach phieu kham", "liet ke phieu kham",
                "tat ca phieu kham")) {

            spec.setIntent(IntentType.LIST_EXAMINATIONS);

            extractDateRange(rawMessage, spec);

            if (spec.getStartDate() == null) {
                spec.setDate(extractDate(rawMessage));
            }

            return spec;
        }

        if (containsAnyNL(q, "danh sach hoa don", "liet ke hoa don",
                "tat ca hoa don")) {

            spec.setIntent(IntentType.LIST_BILLS);

            extractDateRange(rawMessage, spec);

            if (spec.getStartDate() == null) {
                spec.setDate(extractDate(rawMessage));
            }

            return spec;
        }

        if (containsAnyNL(q, "hoa don hom nay", "hoa don ngay", "hoa don thang")) {
            spec.setIntent(IntentType.BILL_BY_DATE);
            extractDateRange(rawMessage, spec);

            if (spec.getStartDate() == null) {
                spec.setDate(extractDate(rawMessage));
            }
            return spec;
        }

        if (containsAnyNL(q, "hoa don", "xem hoa don",
                "tra cuu hoa don")) {
            spec.setIntent(IntentType.BILL_BY_PATIENT);
            String idOrCccd = extractPatientIdOrCccd(rawMessage);
            spec.setPatientId(idOrCccd);
            spec.setCccd(idOrCccd);
            return spec;
        }

        if (containsAnyNL(q, "lich kham hom nay", "lich kham ngay",
                "lich kham cua bac si", "lich kham", "phieu kham hom nay",
                "phieu kham ngay")) {
            spec.setIntent(IntentType.DOCTOR_SCHEDULE);
            extractDateRange(rawMessage, spec);

            if (spec.getStartDate() == null) {
                spec.setDate(extractDate(rawMessage));
            }
            return spec;
        }

        if (containsAnyNL(q, "tra cuu thuoc", "tim thuoc", "thong tin thuoc",
                "thuoc ten", "hoat chat")) {
            spec.setIntent(IntentType.MEDICINE_LOOKUP);
            spec.setMedicineName(extractNameAfterKeyword(rawMessage,
                    "thuốc", "hoạt chất", "tên thuốc", "tra cứu thuốc", "tìm thuốc"));
            return spec;
        }

        if (containsAnyNL(q, "tra cuu dich vu", "tim dich vu", "dich vu lam sang",
                "can lam sang ten", "xet nghiem ten")) {
            spec.setIntent(IntentType.CLINICAL_LOOKUP);
            spec.setClinicalName(extractNameAfterKeyword(rawMessage,
                    "dịch vụ", "xét nghiệm", "cận lâm sàng", "tra cứu dịch vụ"));
            return spec;
        }

        if (containsAnyNL(q, "benh an cua benh nhan", "benh an cua",
                "ho so benh an", "lich su kham", "lich su benh an")) {
            spec.setIntent(IntentType.MEDICAL_RECORD_BY_PATIENT);
            String idOrCccd = extractPatientIdOrCccd(rawMessage);
            spec.setPatientId(idOrCccd);
            spec.setCccd(idOrCccd);
            return spec;
        }

        if (containsAnyNL(q, "lich hen cua benh nhan", "lich hen benh nhan",
                "lich hen cua", "dat lich hen", "xem lich hen")) {
            spec.setIntent(IntentType.APPOINTMENT_BY_PATIENT);
            String idOrCccd = extractPatientIdOrCccd(rawMessage);
            spec.setPatientId(idOrCccd);
            spec.setCccd(idOrCccd);
            return spec;
        }

        if (containsAnyNL(q, "thong tin benh nhan", "ho so benh nhan",
                "tra cuu benh nhan", "tim benh nhan", "cccd",
                "can cuoc cong dan", "can cuoc", "ma benh nhan", "id benh nhan")) {
            spec.setIntent(IntentType.PATIENT_PROFILE);
            String idOrCccd = extractPatientIdOrCccd(rawMessage);
            if (!TextUtils.isEmpty(idOrCccd)) {

                if (idOrCccd.matches("\\d{9,12}")) {
                    spec.setCccd(idOrCccd);
                } else {
                    spec.setPatientId(idOrCccd);
                }
            }

            String name = extractPatientName(rawMessage);
            if (!TextUtils.isEmpty(name) && TextUtils.isEmpty(spec.getPatientId())
                    && TextUtils.isEmpty(spec.getCccd())) {
                spec.setPatientName(name);
                spec.setPatientId(name);
            }
            return spec;
        }

        return null;
    }

    private String normalizeNL(String input) {
        if (input == null) return "";
        String value = input.toLowerCase(Locale.getDefault()).trim();
        value = value.replace("đ", "d").replace("Đ", "D");
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAnyNL(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String extractPatientIdOrCccd(String message) {
        if (TextUtils.isEmpty(message)) return null;

        Pattern cccdPattern = Pattern.compile("\\b(\\d{9,12})\\b");
        Matcher m = cccdPattern.matcher(message);
        if (m.find()) return m.group(1);

        Pattern idPattern = Pattern.compile(
                "(?i)(?:ma benh nhan|mã bệnh nhân|id benh nhan|id)\\s*[:=]?\\s*(\\d+)"
        );
        Matcher idMatcher = idPattern.matcher(message);

        if (idMatcher.find()) {
            return idMatcher.group(1);
        }

        return null;
    }

    private String extractPatientName(String message) {
        if (TextUtils.isEmpty(message)) return null;
        String[] triggers = {
                "bệnh nhân", "benh nhan", "tên", "ten", "họ tên", "ho ten"
        };
        String lower = message.toLowerCase(Locale.getDefault());
        for (String trigger : triggers) {
            int idx = lower.lastIndexOf(trigger);
            if (idx >= 0) {
                String after = message.substring(idx + trigger.length()).trim();
                after = after.replaceFirst("(?i)^(la|là|ten|tên|:)\\s*", "").trim();
                if (!after.isEmpty() && after.length() <= 60) return after;
            }
        }
        return null;
    }

    private String extractNameAfterKeyword(String message, String... keywords) {
        if (TextUtils.isEmpty(message)) return null;
        String lower = message.toLowerCase(Locale.getDefault());
        for (String kw : keywords) {
            int idx = lower.indexOf(kw.toLowerCase(Locale.getDefault()));
            if (idx >= 0) {
                String after = message.substring(idx + kw.length()).trim();
                after = after.replaceFirst("(?i)^(la|là|tên|ten|:)\\s*", "").trim();
                if (!after.isEmpty() && after.length() <= 80) return after;
            }
        }
        return null;
    }

    private void extractDateRange(String message, QuerySpec spec) {

        String lower = normalizeNL(message);

        Calendar cal = Calendar.getInstance();

        if (lower.contains("tuan nay")) {

            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            Date start = cal.getTime();

            cal.add(Calendar.DAY_OF_MONTH, 6);

            Date end = cal.getTime();

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            spec.setStartDate(sdf.format(start));
            spec.setEndDate(sdf.format(end));
            return;
        }

        if (lower.contains("thang nay")) {

            cal.set(Calendar.DAY_OF_MONTH, 1);

            Date start = cal.getTime();

            cal.set(Calendar.DAY_OF_MONTH,
                    cal.getActualMaximum(Calendar.DAY_OF_MONTH));

            Date end = cal.getTime();

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            spec.setStartDate(sdf.format(start));
            spec.setEndDate(sdf.format(end));
            return;
        }

        Pattern p = Pattern.compile(
                "(\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}).*?(?:den|toi|->|đến).*?(\\d{1,2}[/-]\\d{1,2}[/-]\\d{4})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(message);

        if (m.find()) {

            spec.setStartDate(convertToIso(m.group(1)));
            spec.setEndDate(convertToIso(m.group(2)));
        }
    }

    private String convertToIso(String dateStr) {
        try {

            dateStr = dateStr.replace("-", "/");

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date date = inputFormat.parse(dateStr);

            return date != null
                    ? outputFormat.format(date)
                    : null;

        } catch (Exception e) {
            Log.e("Chatbot", "convertToIso error", e);
            return null;
        }
    }

    private String extractDate(String message) {
        if (TextUtils.isEmpty(message)) return null;

        String lower = message.toLowerCase(Locale.getDefault());
        if (lower.contains("hôm nay") || lower.contains("hom nay") || lower.contains("today")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date());
        }

        Pattern p1 = Pattern.compile("(\\d{1,2})[/\\-](\\d{1,2})[/\\-](\\d{4})");
        Matcher m1 = p1.matcher(message);
        if (m1.find()) {
            try {
                int day = Integer.parseInt(m1.group(1));
                int month = Integer.parseInt(m1.group(2));
                int year = Integer.parseInt(m1.group(3));
                return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
            } catch (Exception ignored) {}
        }

        Pattern p2 = Pattern.compile("(\\d{4})[/\\-](\\d{1,2})[/\\-](\\d{1,2})");
        Matcher m2 = p2.matcher(message);
        if (m2.find()) {
            try {
                int year = Integer.parseInt(m2.group(1));
                int month = Integer.parseInt(m2.group(2));
                int day = Integer.parseInt(m2.group(3));
                return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
            } catch (Exception ignored) {}
        }

        return null;
    }

    public void execute(QuerySpec spec, AnswerCallback callback) {

        if (spec == null || spec.getIntent() == null) {
            callback.onAnswer(errorJson("Không xác định được truy vấn."));
            return;
        }

        if (!canAccess(spec.getIntent())) {
            callback.onAnswer(errorJson(getDeniedMessage(spec.getIntent())));
            return;
        }

        switch (spec.getIntent()) {
            case PATIENT_PROFILE:
                queryPatientProfile(
                        !TextUtils.isEmpty(spec.getPatientId())
                                ? spec.getPatientId()
                                : spec.getCccd(),
                        callback
                );
                break;
            case APPOINTMENT_BY_PATIENT:
                queryAppointmentsByPatient(
                        !TextUtils.isEmpty(spec.getPatientId())
                                ? spec.getPatientId()
                                : spec.getCccd(),
                        callback
                );
                break;
            case MEDICAL_RECORD_BY_PATIENT:
                queryMedicalRecordByPatient(
                        !TextUtils.isEmpty(spec.getPatientId())
                                ? spec.getPatientId()
                                : spec.getCccd(),
                        callback
                );
                break;
            case DOCTOR_SCHEDULE:
                queryDoctorSchedule(spec.getDate(), callback);
                break;
            case BILL_BY_PATIENT:
                queryBillsByPatient(
                        !TextUtils.isEmpty(spec.getPatientId())
                                ? spec.getPatientId()
                                : spec.getCccd(),
                        callback
                );
                break;
            case BILL_BY_DATE:
                queryBillsByDate(spec.getDate(), callback);
                break;
            case MEDICINE_LOOKUP:
                queryMedicineLookup(spec.getMedicineName(), callback);
                break;
            case CLINICAL_LOOKUP:
                queryClinicalLookup(spec.getClinicalName(), callback);
                break;
            case COUNT_PATIENTS:
                queryCountPatients(callback);
                break;
            case LIST_PATIENTS:
                queryListPatients(callback);
                break;
            case COUNT_APPOINTMENTS:
                queryCountAppointments(
                        spec.getStartDate(),
                        spec.getEndDate(),
                        callback);
                break;
            case LIST_APPOINTMENTS:
                queryListAppointments(
                        spec.getDate(),
                        spec.getStartDate(),
                        spec.getEndDate(),
                        callback
                );
                break;
            case COUNT_EXAMINATIONS:
                queryCountExaminations(
                        spec.getStartDate(),
                        spec.getEndDate(),
                        callback
                );
                break;
            case LIST_EXAMINATIONS:
                queryListExaminations(
                        spec.getDate(),
                        spec.getStartDate(),
                        spec.getEndDate(),
                        callback);
                break;
            case COUNT_BILLS:
                queryCountBills(callback);
                break;
            case LIST_BILLS:
                queryListBills(
                        spec.getDate(),
                        spec.getStartDate(),
                        spec.getEndDate(),
                        callback
                );
                break;
            case INVENTORY_STATS:
                queryInventoryStats(callback);
                break;
            case TIMEKEEPING:
                callback.onAnswer(errorJson(getDeniedMessage(IntentType.TIMEKEEPING)));
                break;
            default:
                callback.onAnswer(errorJson("Chưa hỗ trợ truy vấn này."));
                break;
        }
    }

    public void queryPatientProfile(String patientIdOrCccd, AnswerCallback callback) {
        if (TextUtils.isEmpty(patientIdOrCccd)) {
            callback.onAnswer(errorJson("Thiếu thông tin ID hoặc CCCD bệnh nhân."));
            return;
        }
        patientRepository.getProfileByIdOrCccd(patientIdOrCccd).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onAnswer(successJson(IntentType.PATIENT_PROFILE, formatPatientProfiles(response.body())));
                } else {
                    callback.onAnswer(errorJson("Không tìm thấy thông tin bệnh nhân."));
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi truy vấn thông tin bệnh nhân: " + t.getMessage()));
            }
        });
    }

    public void queryAppointmentsByPatient(String patientIdOrCccd, AnswerCallback callback) {
        if (TextUtils.isEmpty(patientIdOrCccd)) {
            callback.onAnswer(errorJson("Thiếu thông tin ID hoặc CCCD bệnh nhân."));
            return;
        }
        patientRepository.getProfileByIdOrCccd(patientIdOrCccd).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String resolvedPatientId = response.body().get(0).getId();
                    appointmentRepository.getAppointmentsByPatientId(resolvedPatientId).enqueue(new Callback<List<AppointmentItem>>() {
                        @Override
                        public void onResponse(Call<List<AppointmentItem>> call, Response<List<AppointmentItem>> response2) {
                            if (response2.isSuccessful() && response2.body() != null) {
                                callback.onAnswer(successJson(IntentType.APPOINTMENT_BY_PATIENT, formatAppointments(response2.body())));
                            } else {
                                callback.onAnswer(errorJson("Không tìm thấy danh sách lịch hẹn của bệnh nhân."));
                            }
                        }

                        @Override
                        public void onFailure(Call<List<AppointmentItem>> call, Throwable t) {
                            callback.onAnswer(errorJson("Lỗi truy vấn lịch hẹn: " + t.getMessage()));
                        }
                    });
                } else {
                    callback.onAnswer(errorJson("Không tìm thấy thông tin bệnh nhân để tra cứu lịch hẹn."));
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi xác thực thông tin bệnh nhân: " + t.getMessage()));
            }
        });
    }

    public void queryMedicalRecordByPatient(String patientIdOrCccd, AnswerCallback callback) {
        if (TextUtils.isEmpty(patientIdOrCccd)) {
            callback.onAnswer(errorJson("Thiếu thông tin ID hoặc CCCD bệnh nhân."));
            return;
        }
        patientRepository.getProfileByIdOrCccd(patientIdOrCccd).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String resolvedPatientId = response.body().get(0).getId();
                    patientRepository.getMedicalRecordsByPatientId(resolvedPatientId).enqueue(new Callback<List<MedicalRecordByPatientResponse>>() {
                        @Override
                        public void onResponse(Call<List<MedicalRecordByPatientResponse>> call, Response<List<MedicalRecordByPatientResponse>> response2) {
                            if (response2.isSuccessful() && response2.body() != null) {
                                callback.onAnswer(successJson(IntentType.MEDICAL_RECORD_BY_PATIENT, formatMedicalRecords(response2.body())));
                            } else {
                                callback.onAnswer(errorJson("Không tìm thấy bệnh án của bệnh nhân."));
                            }
                        }

                        @Override
                        public void onFailure(Call<List<MedicalRecordByPatientResponse>> call, Throwable t) {
                            callback.onAnswer(errorJson("Lỗi truy vấn bệnh án: " + t.getMessage()));
                        }
                    });
                } else {
                    callback.onAnswer(errorJson("Không tìm thấy thông tin bệnh nhân để tra cứu bệnh án."));
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi xác thực thông tin bệnh nhân: " + t.getMessage()));
            }
        });
    }

    public void queryDoctorSchedule(String date, AnswerCallback callback) {
        String doctorFilter = null;
        if (UserRole.BAC_SI.name().equals(roleName)) {
            doctorFilter = currentProfileId != null ? "eq." + currentProfileId : null;
        }

        String dateFilter = null;
        if (!TextUtils.isEmpty(date)) {
            dateFilter = "eq." + date;
        }

        exFormApi.getFormsByDate(
                dateFilter,
                EXAM_SELECT,
                "ngay_kham.desc,gio_du_kien.asc",
                "not.in.(Vắng,Đã hủy)",
                doctorFilter
        ).enqueue(new Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(Call<List<ExaminationFormWithPatientDto>> call, Response<List<ExaminationFormWithPatientDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onAnswer(successJson(IntentType.DOCTOR_SCHEDULE, formatSchedule(response.body())));
                } else {
                    callback.onAnswer(errorJson("Không tìm thấy lịch khám."));
                }
            }

            @Override
            public void onFailure(Call<List<ExaminationFormWithPatientDto>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi truy vấn lịch khám: " + t.getMessage()));
            }
        });
    }

    public void queryBillsByPatient(String patientIdOrCccd, AnswerCallback callback) {
        if (TextUtils.isEmpty(patientIdOrCccd)) {
            callback.onAnswer(errorJson("Thiếu thông tin ID hoặc CCCD bệnh nhân."));
            return;
        }
        patientRepository.getProfileByIdOrCccd(patientIdOrCccd).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String resolvedPatientId = response.body().get(0).getId();
                    billRepository.getBillsByPatientId(resolvedPatientId).enqueue(new Callback<List<ExamFormWithBillDto>>() {
                        @Override
                        public void onResponse(Call<List<ExamFormWithBillDto>> call, Response<List<ExamFormWithBillDto>> response2) {
                            if (response2.isSuccessful() && response2.body() != null) {
                                callback.onAnswer(successJson(IntentType.BILL_BY_PATIENT, formatBills(response2.body())));
                            } else {
                                callback.onAnswer(errorJson("Không tìm thấy hóa đơn của bệnh nhân."));
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ExamFormWithBillDto>> call, Throwable t) {
                            callback.onAnswer(errorJson("Lỗi truy vấn hóa đơn: " + t.getMessage()));
                        }
                    });
                } else {
                    callback.onAnswer(errorJson("Không tìm thấy thông tin bệnh nhân để tra cứu hóa đơn."));
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi xác thực thông tin bệnh nhân: " + t.getMessage()));
            }
        });
    }

    public void queryBillsByDate(String date, AnswerCallback callback) {
        String fromDate = date;
        String toDate = date;

        if (TextUtils.isEmpty(fromDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            fromDate = sdf.format(new Date());
            toDate = fromDate;
        }

        billRepository.getBillsByDateRange(fromDate, toDate).enqueue(new Callback<List<ExamFormWithBillDto>>() {
            @Override
            public void onResponse(Call<List<ExamFormWithBillDto>> call, Response<List<ExamFormWithBillDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onAnswer(successJson(IntentType.BILL_BY_DATE, formatBills(response.body())));
                } else {
                    callback.onAnswer(errorJson("Không tìm thấy hóa đơn."));
                }
            }

            @Override
            public void onFailure(Call<List<ExamFormWithBillDto>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi truy vấn hóa đơn: " + t.getMessage()));
            }
        });
    }

    public void queryMedicineLookup(String medicineName, AnswerCallback callback) {
        prescriptionRepository.getAllMedicines().enqueue(new Callback<List<MedicineItem>>() {
            @Override
            public void onResponse(Call<List<MedicineItem>> call, Response<List<MedicineItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MedicineItem> filtered = new ArrayList<>();
                    if (!TextUtils.isEmpty(medicineName)) {
                        String query = medicineName.toLowerCase().trim();
                        for (MedicineItem item : response.body()) {
                            if ((item.getTen_thuoc() != null && item.getTen_thuoc().toLowerCase().contains(query))
                                    || (item.getHoat_chat() != null && item.getHoat_chat().toLowerCase().contains(query))) {
                                filtered.add(item);
                            }
                        }
                    } else {
                        filtered.addAll(response.body());
                    }
                    callback.onAnswer(successJson(IntentType.MEDICINE_LOOKUP, formatMedicineLookup(filtered)));
                } else {
                    callback.onAnswer(errorJson("Không lấy được danh sách thuốc."));
                }
            }

            @Override
            public void onFailure(Call<List<MedicineItem>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi tra cứu thuốc: " + t.getMessage()));
            }
        });
    }

    public void queryClinicalLookup(String clinicalName, AnswerCallback callback) {
        clinicalRepository.getAllClinicalItems().enqueue(new Callback<List<ClinicalItem>>() {
            @Override
            public void onResponse(Call<List<ClinicalItem>> call, Response<List<ClinicalItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ClinicalItem> filtered = new ArrayList<>();
                    if (!TextUtils.isEmpty(clinicalName)) {
                        String query = clinicalName.toLowerCase().trim();
                        for (ClinicalItem item : response.body()) {
                            if ((item.getTen_dich_vu() != null && item.getTen_dich_vu().toLowerCase().contains(query))
                                    || (item.getMo_ta() != null && item.getMo_ta().toLowerCase().contains(query))) {
                                filtered.add(item);
                            }
                        }
                    } else {
                        filtered.addAll(response.body());
                    }
                    callback.onAnswer(successJson(IntentType.CLINICAL_LOOKUP, formatClinicalLookup(filtered)));
                } else {
                    callback.onAnswer(errorJson("Không lấy được danh sách dịch vụ lâm sàng."));
                }
            }

            @Override
            public void onFailure(Call<List<ClinicalItem>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi tra cứu dịch vụ lâm sàng: " + t.getMessage()));
            }
        });
    }

    private void queryCountPatients(AnswerCallback callback) {
        patientRepository.getAllPatients().enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().size();
                    callback.onAnswer("{\n" +
                            "  \"success\": true,\n" +
                            "  \"intent\": \"COUNT_PATIENTS\",\n" +
                            "  \"data\": {\n" +
                            "    \"count\": " + count + "\n" +
                            "  }\n" +
                            "}");
                } else {
                    callback.onAnswer(errorJson("Không lấy được số lượng bệnh nhân."));
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi đếm số bệnh nhân: " + t.getMessage()));
            }
        });
    }

    private void queryListPatients(AnswerCallback callback) {
        patientRepository.getAllPatients().enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onAnswer(successJson(IntentType.LIST_PATIENTS, formatPatientProfiles(response.body())));
                } else {
                    callback.onAnswer(errorJson("Không lấy được danh sách bệnh nhân."));
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi lấy danh sách bệnh nhân: " + t.getMessage()));
            }
        });
    }

    private void queryCountAppointments(
            String startDate,
            String endDate,
            AnswerCallback callback
    ) {

        Call<List<AppointmentItem>> call;

        if (startDate != null && endDate != null) {
            call = appointmentRepository.getAppointmentsByDateRange(
                    startDate,
                    endDate
            );
        } else {
            call = appointmentRepository.getAllAppointments();
        }

        call.enqueue(new Callback<List<AppointmentItem>>() {

            @Override
            public void onResponse(
                    Call<List<AppointmentItem>> call,
                    Response<List<AppointmentItem>> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    int count = response.body().size();

                    callback.onAnswer(
                            "{\n" +
                                    "  \"success\": true,\n" +
                                    "  \"intent\": \"COUNT_APPOINTMENTS\",\n" +
                                    "  \"data\": {\n" +
                                    "    \"count\": " + count + "\n" +
                                    "  }\n" +
                                    "}"
                    );

                } else {
                    callback.onAnswer(
                            errorJson("Không lấy được số lượng lịch hẹn.")
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<List<AppointmentItem>> call,
                    Throwable t
            ) {

                callback.onAnswer(
                        errorJson(
                                "Lỗi đếm lịch hẹn: "
                                        + t.getMessage()
                        )
                );
            }
        });
    }

    private void queryListAppointments(
            String date,
            String startDate,
            String endDate,
            AnswerCallback callback
    ) {

        Call<List<AppointmentItem>> call;

        if (startDate != null && endDate != null) {

            call = appointmentRepository.getAppointmentsByDateRange(
                    startDate,
                    endDate
            );

        } else if (date != null) {

            call = appointmentRepository.getAppointmentsByDate(
                    date
            );

        } else {

            call = appointmentRepository.getAllAppointments();
        }

        call.enqueue(new Callback<List<AppointmentItem>>() {

            @Override
            public void onResponse(
                    Call<List<AppointmentItem>> call,
                    Response<List<AppointmentItem>> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onAnswer(
                            successJson(
                                    IntentType.LIST_APPOINTMENTS,
                                    formatAppointments(response.body())
                            )
                    );

                } else {

                    callback.onAnswer(
                            errorJson("Không lấy được danh sách lịch hẹn.")
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<List<AppointmentItem>> call,
                    Throwable t
            ) {

                callback.onAnswer(
                        errorJson(
                                "Lỗi lấy danh sách lịch hẹn: "
                                        + t.getMessage()
                        )
                );
            }
        });
    }

    private void queryCountExaminations(
            String startDate,
            String endDate,
            AnswerCallback callback) {

        exFormApi.getFormsByDate(
                null,
                EXAM_SELECT,
                "ngay_kham.desc,gio_du_kien.asc",
                null,
                null
        ).enqueue(new Callback<List<ExaminationFormWithPatientDto>>() {

            @Override
            public void onResponse(
                    Call<List<ExaminationFormWithPatientDto>> call,
                    Response<List<ExaminationFormWithPatientDto>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    callback.onAnswer(errorJson("Không lấy được số lượng phiếu khám."));
                    return;
                }

                List<ExaminationFormWithPatientDto> forms = response.body();

                int count = 0;

                try {
                    SimpleDateFormat sdf =
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    Date from = sdf.parse(startDate);
                    Date to = sdf.parse(endDate);

                    for (ExaminationFormWithPatientDto item : forms) {

                        if (item.getNgay_kham() == null) continue;

                        Date examDate = item.getNgay_kham();

                        if (!examDate.before(from) && !examDate.after(to)) {
                            count++;
                        }
                    }

                    callback.onAnswer("{\n" +
                            "  \"success\": true,\n" +
                            "  \"intent\": \"COUNT_EXAMINATIONS\",\n" +
                            "  \"data\": {\n" +
                            "    \"count\": " + count + "\n" +
                            "  }\n" +
                            "}");

                } catch (Exception e) {
                    callback.onAnswer(errorJson(e.getMessage()));
                }
            }

            @Override
            public void onFailure(
                    Call<List<ExaminationFormWithPatientDto>> call,
                    Throwable t) {

                callback.onAnswer(
                        errorJson("Lỗi đếm số phiếu khám: " + t.getMessage()));
            }
        });
    }

    private void queryListExaminations(
            String date,
            String startDate,
            String endDate,
            AnswerCallback callback
    ) {

        Call<List<ExaminationFormWithPatientDto>> call;

        if (startDate != null && endDate != null) {

            String dateRangeFilter =
                    "(ngay_kham.gte." + startDate +
                            ",ngay_kham.lte." + endDate + ")";

            call = exFormApi.getFormsByDateRange(
                    dateRangeFilter,
                    EXAM_SELECT,
                    "ngay_kham.desc,gio_du_kien.asc",
                    null,
                    null
            );

        } else if (date != null) {

            call = exFormApi.getFormsByDate(
                    "eq." + date,
                    EXAM_SELECT,
                    "ngay_kham.desc,gio_du_kien.asc",
                    null,
                    null
            );

        } else {

            call = exFormApi.getFormsByDate(
                    null,
                    EXAM_SELECT,
                    "ngay_kham.desc,gio_du_kien.asc",
                    null,
                    null
            );
        }

        call.enqueue(new Callback<List<ExaminationFormWithPatientDto>>() {

            @Override
            public void onResponse(
                    Call<List<ExaminationFormWithPatientDto>> call,
                    Response<List<ExaminationFormWithPatientDto>> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onAnswer(
                            successJson(
                                    IntentType.LIST_EXAMINATIONS,
                                    formatSchedule(response.body())
                            )
                    );

                } else {

                    callback.onAnswer(
                            errorJson("Không lấy được danh sách phiếu khám.")
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<List<ExaminationFormWithPatientDto>> call,
                    Throwable t
            ) {

                callback.onAnswer(
                        errorJson(
                                "Lỗi lấy danh sách phiếu khám: "
                                        + t.getMessage()
                        )
                );
            }
        });
    }

    private void queryCountBills(AnswerCallback callback) {
        billRepository.getAllBills().enqueue(new Callback<List<ExamFormWithBillDto>>() {
            @Override
            public void onResponse(Call<List<ExamFormWithBillDto>> call, Response<List<ExamFormWithBillDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().size();
                    callback.onAnswer("{\n" +
                            "  \"success\": true,\n" +
                            "  \"intent\": \"COUNT_BILLS\",\n" +
                            "  \"data\": {\n" +
                            "    \"count\": " + count + "\n" +
                            "  }\n" +
                            "}");
                } else {
                    callback.onAnswer(errorJson("Không lấy được số lượng hóa đơn."));
                }
            }

            @Override
            public void onFailure(Call<List<ExamFormWithBillDto>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi đếm số hóa đơn: " + t.getMessage()));
            }
        });
    }

    private void queryListBills(
            String date,
            String startDate,
            String endDate,
            AnswerCallback callback
    ) {

        Call<List<ExamFormWithBillDto>> call;

        if (startDate != null && endDate != null) {

            call = billRepository.getBillsByDateRange(
                    startDate,
                    endDate
            );

        } else if (date != null) {

            call = billRepository.getBillsByDateRange(
                    date,
                    date
            );

        } else {

            call = billRepository.getAllBills();
        }

        call.enqueue(new Callback<List<ExamFormWithBillDto>>() {

            @Override
            public void onResponse(
                    Call<List<ExamFormWithBillDto>> call,
                    Response<List<ExamFormWithBillDto>> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onAnswer(
                            successJson(
                                    IntentType.LIST_BILLS,
                                    formatBills(response.body())
                            )
                    );

                } else {

                    callback.onAnswer(
                            errorJson("Không lấy được danh sách hóa đơn.")
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<List<ExamFormWithBillDto>> call,
                    Throwable t
            ) {

                callback.onAnswer(
                        errorJson(
                                "Lỗi lấy danh sách hóa đơn: "
                                        + t.getMessage()
                        )
                );
            }
        });
    }

    private void queryInventoryStats(AnswerCallback callback) {
        prescriptionRepository.getAllMedicines().enqueue(new Callback<List<MedicineItem>>() {
            @Override
            public void onResponse(Call<List<MedicineItem>> call, Response<List<MedicineItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MedicineItem> list = response.body();
                    int totalItems = list.size();
                    long totalStock = 0;
                    int lowStockCount = 0;
                    List<MedicineItem> lowStockItems = new ArrayList<>();
                    for (MedicineItem item : list) {
                        totalStock += item.getTon_kho();
                        if (item.getTon_kho() < 10) {
                            lowStockCount++;
                            lowStockItems.add(item);
                        }
                    }

                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("total_distinct_medicines", totalItems);
                    stats.put("total_stock_count", totalStock);
                    stats.put("low_stock_types_count", lowStockCount);
                    stats.put("low_stock_details", lowStockItems);

                    callback.onAnswer(successJson(IntentType.INVENTORY_STATS, gson.toJson(stats)));
                } else {
                    callback.onAnswer(errorJson("Không lấy được thống kê kho thuốc."));
                }
            }

            @Override
            public void onFailure(Call<List<MedicineItem>> call, Throwable t) {
                callback.onAnswer(errorJson("Lỗi lấy thống kê kho thuốc: " + t.getMessage()));
            }
        });
    }

    public String formatPatientProfiles(List<PatientProfile> profiles) {
        return gson.toJson(profiles);
    }

    public String formatAppointments(List<AppointmentItem> appointments) {
        return gson.toJson(appointments);
    }

    public String formatMedicalRecords(List<MedicalRecordByPatientResponse> records) {
        return gson.toJson(records);
    }

    public String formatSchedule(List<ExaminationFormWithPatientDto> schedule) {
        return gson.toJson(schedule);
    }

    public String formatBills(List<ExamFormWithBillDto> bills) {

        List<java.util.Map<String, Object>> result = new ArrayList<>();

        for (ExamFormWithBillDto item : bills) {

            java.util.Map<String, Object> map = new java.util.HashMap<>();

            map.put("ma_phieu_kham", item.getId());

            map.put("ngay_kham", item.getNgay_kham());

            if (item.getPatient() != null) {
                map.put("ten_benh_nhan",
                        item.getPatient().getHo_ten());
            }

            if (item.getMedical_record() != null
                    && item.getMedical_record().getBill() != null) {

                ExamFormWithBillDto.BillSummaryDto bill =
                        item.getMedical_record().getBill();

                map.put("tong_thanh_toan",
                        bill.getTong_thanh_toan());

                map.put("trang_thai_thanh_toan",
                        bill.getTrang_thai_thanh_toan());

                map.put("phuong_thuc_thanh_toan",
                        bill.getPhuong_thuc_thanh_toan());

                map.put("ngay_tao_hoa_don",
                        bill.getCreated_at());
            }

            result.add(map);
        }

        return gson.toJson(result);
    }

    private String formatPatients(List<PatientProfile> patients) {

        List<Map<String, Object>> result = new ArrayList<>();

        for (PatientProfile patient : patients) {

            Map<String, Object> map = new HashMap<>();

            map.put("id", patient.getId());

            map.put("ho_ten", patient.getHo_ten());

            map.put("so_dien_thoai", patient.getSo_dien_thoai());

            map.put("cccd", patient.getCccd());

            map.put("gioi_tinh", patient.getGioi_tinh());

            map.put("ngay_sinh", patient.getNgay_sinh());

            result.add(map);
        }

        return gson.toJson(result);
    }

    public String formatMedicineLookup(List<MedicineItem> medicines) {
        return gson.toJson(medicines);
    }

    public String formatClinicalLookup(List<ClinicalItem> clinicalItems) {
        return gson.toJson(clinicalItems);
    }

    private String successJson(IntentType intent, String dataJson) {
        return "{\n" +
                "  \"success\": true,\n" +
                "  \"intent\": \"" + intent.name() + "\",\n" +
                "  \"message\": \"OK\",\n" +
                "  \"data\": " + dataJson + "\n" +
                "}";
    }

    private String errorJson(String message) {
        String escaped = message == null ? "" : message.replace("\"", "\\\"");
        return "{\n" +
                "  \"success\": false,\n" +
                "  \"message\": \"" + escaped + "\"\n" +
                "}";
    }
}
