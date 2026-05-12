package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;

import java.util.Locale;

import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.PatientBriefDto;

public class ExaminationFormDetail_doctor extends AppCompatActivity {
    public static final String EXTRA_FORM_ID = "extra_form_id";
    public static final String EXTRA_PATIENT_NAME = "extra_patient_name";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_PATIENT_ID = "extra_patient_id";
    public static final String EXTRA_SEQUENCE_NUMBER = "extra_sequence_number";
    public static final String EXTRA_SYMPTOMS = "extra_symptoms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_examination_form_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindHeaderData();
        setupBackButton();
    }

    public static Intent createIntent(Context context, ExaminationFormWithPatientDto form) {
        PatientBriefDto patient = form.getPatient();

        return new Intent(context, ExaminationFormDetail_doctor.class)
                .putExtra(EXTRA_FORM_ID, safeText(form.getId(), "--"))
                .putExtra(EXTRA_PATIENT_NAME, patient != null ? safeText(patient.getHo_ten(), "--") : "--")
                .putExtra(EXTRA_STATUS, safeText(form.getTrang_thai(), "Cho kham"))
                .putExtra(EXTRA_TIME, formatDisplayTime(form.getGio_du_kien()))
                .putExtra(EXTRA_PATIENT_ID, patient != null ? safeText(patient.getId(), "--") : "--")
                .putExtra(EXTRA_SEQUENCE_NUMBER, String.valueOf(form.getSo_tiep_nhan()))
                .putExtra(EXTRA_SYMPTOMS, safeText(form.getTrieu_chung_ban_dau(), "Khong co trieu chung ban dau"));
    }

    private void bindHeaderData() {
        TextView tvPatientName = findViewById(R.id.tvDoctorExDetailPatientName);
        TextView tvStatus = findViewById(R.id.tvDoctorExDetailStatusBadge);
        TextView tvTime = findViewById(R.id.tvDoctorExDetailTime);
        TextView tvPatientId = findViewById(R.id.tvDoctorExDetailPatientID);
        TextView tvSequenceNumber = findViewById(R.id.tvDoctorExDetailReceptNumber);

        tvPatientName.setText(readExtra(EXTRA_PATIENT_NAME, "--"));
        tvStatus.setText(buildStatusBadge(readExtra(EXTRA_STATUS, "Cho kham")));
        tvTime.setText(readExtra(EXTRA_TIME, "--"));
        tvPatientId.setText(readExtra(EXTRA_PATIENT_ID, "--"));
        tvSequenceNumber.setText(readExtra(EXTRA_SEQUENCE_NUMBER, "--"));
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBackDoctorExDetail);
        btnBack.setOnClickListener(v -> finish());
    }

    private String readExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return safeText(value, fallback);
    }

    private String buildStatusBadge(String status) {
        return String.format(Locale.getDefault(), "• %s", safeText(status, "Cho kham"));
    }

    private static String formatDisplayTime(String rawTime) {
        String displayTime = safeText(rawTime, "--");
        if ("--".equals(displayTime)) {
            return displayTime;
        }

        String[] parts = displayTime.split(":");
        if (parts.length >= 2) {
            return parts[0] + ":" + parts[1];
        }

        return displayTime;
    }

    private static String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
