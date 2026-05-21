package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.FullMedicalRecordResponse;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.PatientBriefDto;
import retrofit2.Call;

public class ExaminationFormDetail_doctor extends BaseActivity {
    private enum DetailTab {
        PATIENT_INFO(0),
        CLINICAL(1),
        DIAGNOSIS(2),
        PRESCRIPTION(3);

        private final int position;

        DetailTab(int position) {
            this.position = position;
        }

        int getPosition() {
            return position;
        }

        static DetailTab fromPosition(int position) {
            for (DetailTab tab : values()) {
                if (tab.position == position) {
                    return tab;
                }
            }
            return PATIENT_INFO;
        }
    }

    public static final String EXTRA_FORM_ID = "extra_form_id";
    public static final String EXTRA_PATIENT_NAME = "extra_patient_name";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_PATIENT_ID = "extra_patient_id";
    public static final String EXTRA_SEQUENCE_NUMBER = "extra_sequence_number";
    public static final String EXTRA_SYMPTOMS = "extra_symptoms";
    public static final String EXTRA_PATIENT_BIRTHDAY = "extra_patient_birthday";
    public static final String EXTRA_PATIENT_ADDRESS = "extra_patient_address";
    public static final String EXTRA_PATIENT_CCCD = "extra_patient_cccd";
    public static final String EXTRA_PATIENT_GENDER = "extra_patient_gender";
    public static final String EXTRA_PATIENT_PHONE = "extra_patient_phone";

    private LinearLayout tabExformPatient;
    private LinearLayout tabCanLamSang;
    private LinearLayout tabDiagnosis;
    private LinearLayout tabPrescription;
    private ViewPager2 viewPagerDoctorExDetail;
    private DoctorExaminationStatus currentStatus;
    private MedicalRecordRepository MedRecordRepository;
    private DoctorExDetailViewModel doctorExDetailViewModel;
    private static FullMedicalRecordResponse currentMedicalRecord;
    private static String currentFormId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_examination_form_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_doctor_examination_form_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindHeaderData();
        currentFormId = readExtra(EXTRA_FORM_ID, "--");
        setupBackButton();
        initializeViews();
        doctorExDetailViewModel = new ViewModelProvider(this).get(DoctorExDetailViewModel.class);
        setupViewPager();
        setupTabClicks();

        MedRecordRepository = new MedicalRecordRepository(this);
        requestFullMedicalRecord();
    }

    public static Intent createIntent(Context context, ExaminationFormWithPatientDto form) {
        PatientBriefDto patient = form.getPatient();
        currentFormId = form.getId();

        return new Intent(context, ExaminationFormDetail_doctor.class)
                .putExtra(EXTRA_FORM_ID, safeText(form.getId(), "--"))
                .putExtra(EXTRA_PATIENT_NAME, patient != null ? safeText(patient.getHo_ten(), "--") : "--")
                .putExtra(EXTRA_STATUS, safeText(form.getTrang_thai(), DoctorExaminationStatus.WAITING.getDisplayName()))
                .putExtra(EXTRA_TIME, formatDisplayTime(form.getGio_du_kien()))
                .putExtra(EXTRA_PATIENT_ID, patient != null ? safeText(patient.getId(), "--") : "--")
                .putExtra(EXTRA_SEQUENCE_NUMBER, String.valueOf(form.getSo_tiep_nhan()))
                .putExtra(EXTRA_SYMPTOMS, safeText(form.getTrieu_chung_ban_dau(), "Không có triệu chứng ban đầu"))
                .putExtra(EXTRA_PATIENT_BIRTHDAY, formatDate(patient != null ? patient.getNgay_sinh() : null))
                .putExtra(EXTRA_PATIENT_ADDRESS, patient != null ? safeText(patient.getDia_chi(), "--") : "--")
                .putExtra(EXTRA_PATIENT_CCCD, patient != null ? safeText(patient.getCccd(), "--") : "--")
                .putExtra(EXTRA_PATIENT_GENDER, patient != null ? safeText(patient.getGioi_tinh(), "--") : "--")
                .putExtra(EXTRA_PATIENT_PHONE, patient != null ? safeText(patient.getSo_dien_thoai(), "--") : "--");
    }

    private void bindHeaderData() {
        TextView tvPatientName = findViewById(R.id.tvDoctorExDetailPatientName);
        TextView tvStatus = findViewById(R.id.tvDoctorExDetailStatusBadge);
        TextView tvTime = findViewById(R.id.tvDoctorExDetailTime);
        TextView tvPatientId = findViewById(R.id.tvDoctorExDetailPatientID);
        TextView tvSequenceNumber = findViewById(R.id.tvDoctorExDetailReceptNumber);
        currentStatus = DoctorExaminationStatus.fromValue(
                readExtra(EXTRA_STATUS, DoctorExaminationStatus.WAITING.getDisplayName())
        );

        tvPatientName.setText(readExtra(EXTRA_PATIENT_NAME, "--"));
        DoctorExaminationStatusStyle.applyBadgeStyle(tvStatus, currentStatus.getDisplayName());
        tvTime.setText(readExtra(EXTRA_TIME, "--"));
        tvPatientId.setText(readExtra(EXTRA_PATIENT_ID, "--"));
        tvSequenceNumber.setText(readExtra(EXTRA_SEQUENCE_NUMBER, "--"));
    }

    public DoctorExaminationStatus getCurrentStatus() {
        return currentStatus == null ? DoctorExaminationStatus.WAITING : currentStatus;
    }

    public void updateCurrentStatus(DoctorExaminationStatus status) {
        currentStatus = status == null ? DoctorExaminationStatus.WAITING : status;
        TextView tvStatus = findViewById(R.id.tvDoctorExDetailStatusBadge);
        DoctorExaminationStatusStyle.applyBadgeStyle(tvStatus, currentStatus.getDisplayName());
        getIntent().putExtra(EXTRA_STATUS, currentStatus.getDisplayName());
        updateStatusDependentUi();
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBackDoctorExDetail);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        tabExformPatient = findViewById(R.id.tabExformPatient);
        tabCanLamSang = findViewById(R.id.tabCanLamSang);
        tabDiagnosis = findViewById(R.id.tabDiagnosis);
        tabPrescription = findViewById(R.id.tabPrescription);
        viewPagerDoctorExDetail = findViewById(R.id.viewPagerDoctorExDetail);
        updateStatusDependentUi();
    }

    private void setupViewPager() {
        viewPagerDoctorExDetail.setAdapter(new DoctorExDetailPagerAdapter(this));
        viewPagerDoctorExDetail.setCurrentItem(DetailTab.PATIENT_INFO.getPosition(), false);
        updateSelectedTab(DetailTab.PATIENT_INFO);
        viewPagerDoctorExDetail.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateSelectedTab(DetailTab.fromPosition(position));
            }
        });
    }

    private void setupTabClicks() {
        tabExformPatient.setOnClickListener(v -> selectTab(DetailTab.PATIENT_INFO));
        tabCanLamSang.setOnClickListener(v -> selectTab(DetailTab.CLINICAL));
        tabDiagnosis.setOnClickListener(v -> selectTab(DetailTab.DIAGNOSIS));
        tabPrescription.setOnClickListener(v -> selectTab(DetailTab.PRESCRIPTION));
    }

    private void selectTab(DetailTab tab) {
        if (isDoneLocked() && tab != DetailTab.PATIENT_INFO) {
            return;
        }
        viewPagerDoctorExDetail.setCurrentItem(tab.getPosition(), true);
    }

    public void navigateToClinicalTab() {
        selectTab(DetailTab.CLINICAL);
    }

    public void navigateToDiagnosisTab() {
        selectTab(DetailTab.DIAGNOSIS);
    }

    public void navigateToPrescriptionTab() {
        selectTab(DetailTab.PRESCRIPTION);
    }

    private void updateSelectedTab(DetailTab selectedTab) {
        applyTabState(tabExformPatient, selectedTab == DetailTab.PATIENT_INFO);
        applyTabState(tabCanLamSang, selectedTab == DetailTab.CLINICAL);
        applyTabState(tabDiagnosis, selectedTab == DetailTab.DIAGNOSIS);
        applyTabState(tabPrescription, selectedTab == DetailTab.PRESCRIPTION);
    }

    private void applyTabState(LinearLayout tabView, boolean isSelected) {
        tabView.setBackgroundResource(isSelected
                ? R.drawable.bg_doctor_detail_tab_selected
                : R.drawable.bg_doctor_detail_tab_unselected);

        int textColor = ContextCompat.getColor(this, isSelected ? android.R.color.white : R.color.filter_tab_unselected_text);
        int iconColor = ContextCompat.getColor(this, isSelected ? android.R.color.white : R.color.filter_tab_unselected_text);

        for (int index = 0; index < tabView.getChildCount(); index++) {
            View child = tabView.getChildAt(index);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(textColor);
            } else if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(iconColor);
            }
        }
    }

    private void updateStatusDependentUi() {
        boolean isDone = isDoneLocked();
        setTabEnabled(tabCanLamSang, !isDone);
        setTabEnabled(tabDiagnosis, !isDone);
        setTabEnabled(tabPrescription, !isDone);
        if (viewPagerDoctorExDetail != null) {
            viewPagerDoctorExDetail.setUserInputEnabled(!isDone);
        }
    }

    private void setTabEnabled(LinearLayout tabView, boolean enabled) {
        if (tabView == null) {
            return;
        }
        tabView.setEnabled(enabled);
        tabView.setClickable(enabled);
        tabView.setAlpha(enabled ? 1f : 0.45f);
    }

    public boolean isDoneLocked() {
        return getCurrentStatus() == DoctorExaminationStatus.DONE;
    }

    private String readExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return safeText(value, fallback);
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

    private static String formatDate(java.util.Date date) {
        if (date == null) {
            return "--";
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    static String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static class DoctorExDetailPagerAdapter extends FragmentStateAdapter {
        DoctorExDetailPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        @Override
        public Fragment createFragment(int position) {
            switch (DetailTab.fromPosition(position)) {
                case CLINICAL:
                    return new TabClinicalFragment();
                case DIAGNOSIS:
                    return new TabDiagnosisFragment();
                case PRESCRIPTION:
                    return new TabPrescriptionFragment();
                case PATIENT_INFO:
                default:
                    return new TabPatientInfoFragment();
            }
        }
    }

    public void requestListReload(boolean isFormCompleted) {
        Intent data = new Intent();

        setResult(Activity.RESULT_OK, data);
        if (isFormCompleted) finish();
    }

    private void requestFullMedicalRecord() {
        MedRecordRepository.getMedicalRecordJoinDiagnosisJoinPrescription(currentFormId).enqueue(new retrofit2.Callback<List<FullMedicalRecordResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<FullMedicalRecordResponse>> call, @NonNull retrofit2.Response<List<FullMedicalRecordResponse>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        currentMedicalRecord = response.body().get(0);
                        doctorExDetailViewModel.setMedicalRecord(currentMedicalRecord);
                    } else {
                        doctorExDetailViewModel.setMedicalRecord(null);
                    }
                } else {
                    try {
                        Toast.makeText(ExaminationFormDetail_doctor.this, "Lỗi: " + response.code() + " " + (response.errorBody()).string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FullMedicalRecordResponse>> call, @NonNull Throwable t) {
                Toast.makeText(ExaminationFormDetail_doctor.this, "Lỗi kết nối khi tải bệnh án: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
