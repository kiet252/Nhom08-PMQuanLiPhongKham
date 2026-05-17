package dashboard_fragment.doctor_view_medical_record;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordClinicalWrapper;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordMedicineWrapper;
import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewMedicalRecord_doctor extends AppCompatActivity {
    private static final String TAG = "MED_RECORD_DEBUG";
    private static final int ITEM_STYLE_CLINICAL = 1;
    private static final int ITEM_STYLE_DIAGNOSIS = 2;
    private static final int ITEM_STYLE_MEDICINE = 3;

    private EditText edtMedRecordSearch;
    private MaterialButton btnMedRecordSearch;
    private ChipGroup chipGroupRecentSearch;
    private PatientRepository patientRepository;
    private LinearLayout layoutMedicalRecordEmptyState;
    private LinearLayout containerMedicalRecordResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_view_medical_record);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_medical_record), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();
        setupBackButton();
        setupSearchButton();
        loadRecentSearches();
    }

    private void loadRecentSearches() {
        for (PatientProfile patient : SearchHistoryManager.getInstance().getRecentSearches()) {
            addRecentSearchChip(patient);
        }
    }

    private void initializeViews() {
        edtMedRecordSearch = findViewById(R.id.edtMedicalRecordSearch);
        btnMedRecordSearch = findViewById(R.id.btnMedicalRecordSearch);
        chipGroupRecentSearch = findViewById(R.id.chipGroupRecentSearch);
        patientRepository = new PatientRepository(this);
        layoutMedicalRecordEmptyState = findViewById(R.id.layoutMedicalRecordEmptyState);
        containerMedicalRecordResult = findViewById(R.id.containerMedicalRecordResult);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBackDoctorMedicalRecord);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearchButton() {
        btnMedRecordSearch.setOnClickListener(v -> {
            String input = edtMedRecordSearch.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã bệnh nhân hoặc CCCD", Toast.LENGTH_SHORT).show();
                return;
            }
            performSearch(input);
        });
    }

    private void performSearch(String input) {
        patientRepository.getProfileByIdOrCccd(input).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(Call<List<PatientProfile>> call, Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    PatientProfile patient = response.body().get(0);
                    SearchHistoryManager.getInstance().addSearch(patient);
                    addRecentSearchChip(patient);
                    fetchMedicalRecords(patient);
                } else {
                    Toast.makeText(ViewMedicalRecord_doctor.this, "Không tìm thấy bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                Toast.makeText(ViewMedicalRecord_doctor.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMedicalRecords(PatientProfile patient) {
        patientRepository.getExaminationFormHistoryByPatientId(String.valueOf(patient.getId()))
                .enqueue(new Callback<List<ExaminationFormHistoryResponse>>() {
                    @Override
                    public void onResponse(Call<List<ExaminationFormHistoryResponse>> call, Response<List<ExaminationFormHistoryResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ExaminationFormHistoryResponse> histories = response.body();
                            Log.d(TAG, "History loaded: " + histories.size() + " examination_form rows for patient " + patient.getId());
                            renderPatientInfo(patient, histories.size());
                            renderHistoryItems(histories);
                        } else {
                            logApiError("getExaminationFormHistoryByPatientId", response);
                            renderPatientInfo(patient, 0);
                            Toast.makeText(ViewMedicalRecord_doctor.this, "Không có bệnh án nào", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ExaminationFormHistoryResponse>> call, Throwable t) {
                        Log.e(TAG, "History request failed: " + t.getMessage(), t);
                        Toast.makeText(ViewMedicalRecord_doctor.this, "Lỗi tải bệnh án: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderHistoryItems(List<ExaminationFormHistoryResponse> histories) {
        for (int i = 0; i < histories.size(); i++) {
            ExaminationFormHistoryResponse history = histories.get(i);
            View historyView = getLayoutInflater().inflate(
                    R.layout.doctor_item_medical_record_history,
                    containerMedicalRecordResult,
                    false
            );

            TextView tvRecordDate = historyView.findViewById(R.id.tvRecordDate);
            TextView tvRecordStatus = historyView.findViewById(R.id.tvRecordStatus);
            TextView tvRecordDoctor = historyView.findViewById(R.id.tvRecordDoctor);
            TextView tvRecordSymptom = historyView.findViewById(R.id.tvRecordSymptom);
            TextView tvDiagnosisCount = historyView.findViewById(R.id.tvDiagnosisCount);
            TextView tvMedicineCount = historyView.findViewById(R.id.tvMedicineCount);
            TextView tvClinicalCount = historyView.findViewById(R.id.tvClinicalCount);
            TextView tvRecordSymptomDetail = historyView.findViewById(R.id.tvRecordSymptomDetail);

            LinearLayout containerClinicalItems = historyView.findViewById(R.id.containerClinicalItems);
            LinearLayout containerDiagnosisItems = historyView.findViewById(R.id.containerDiagnosisItems);
            LinearLayout containerMedicineItems = historyView.findViewById(R.id.containerMedicineItems);

            tvRecordDate.setText(safeText(history.getNgay_kham()));
            tvRecordStatus.setText(safeText(history.getTrang_thai()));

            String doctorName = history.getProfiles() != null
                    ? safeText(history.getProfiles().getHo_ten())
                    : "Bác sĩ";
            tvRecordDoctor.setText("BS. " + doctorName + " · " + String.valueOf(history.getId()));
            tvRecordSymptom.setText(safeText(history.getTrieu_chung_ban_dau()));

            containerClinicalItems.removeAllViews();
            containerDiagnosisItems.removeAllViews();
            containerMedicineItems.removeAllViews();

            Set<String> diagnosisItems = new LinkedHashSet<>();
            List<String> medicineItems = new ArrayList<>();
            List<String> clinicalItems = new ArrayList<>();
            String symptomDetail = safeText(history.getTrieu_chung_ban_dau());

            ExaminationFormHistoryResponse.MedicalRecordInner medicalRecord = history.getMedical_record();
            if (medicalRecord != null) {
                diagnosisItems.addAll(splitDiagnosisItems(medicalRecord.getChanDoanChinh()));
                diagnosisItems.addAll(splitDiagnosisItems(medicalRecord.getChanDoanBoSung()));

                if (medicalRecord.getMedicineData() != null) {
                    for (MedicalRecordMedicineWrapper medicineWrapper : medicalRecord.getMedicineData()) {
                        String medicineText = buildMedicineDisplayText(medicineWrapper);
                        if (!medicineText.isEmpty()) {
                            medicineItems.add(medicineText);
                        }
                    }
                }

                if (medicalRecord.getClinicalData() != null) {
                    for (MedicalRecordClinicalWrapper clinicalWrapper : medicalRecord.getClinicalData()) {
                        if (clinicalWrapper == null || clinicalWrapper.getClinical() == null) {
                            continue;
                        }

                        String clinicalName = safeText(clinicalWrapper.getClinical().getTen_dich_vu());
                        if (!clinicalName.isEmpty()) {
                            clinicalItems.add(clinicalName);
                        }
                    }
                }

                if (symptomDetail.isEmpty()) {
                    symptomDetail = safeText(medicalRecord.getGhiChuLamSang());
                }
            }

            for (String diagnosisItem : diagnosisItems) {
                addStyledItemToContainer(containerDiagnosisItems, diagnosisItem, ITEM_STYLE_DIAGNOSIS, 0);
            }
            for (int medicineIndex = 0; medicineIndex < medicineItems.size(); medicineIndex++) {
                addStyledItemToContainer(
                        containerMedicineItems,
                        medicineItems.get(medicineIndex),
                        ITEM_STYLE_MEDICINE,
                        medicineIndex + 1
                );
            }
            for (String clinicalItem : clinicalItems) {
                addStyledItemToContainer(containerClinicalItems, clinicalItem, ITEM_STYLE_CLINICAL, 0);
            }

            tvRecordSymptomDetail.setText(symptomDetail);
            tvDiagnosisCount.setText(diagnosisItems.size() + " chẩn đoán");
            tvMedicineCount.setText(medicineItems.size() + " thuốc");
            tvClinicalCount.setText(clinicalItems.size() + " CLS");

            LinearLayout layoutRecordHeader = historyView.findViewById(R.id.layoutRecordHeader);
            LinearLayout containerRecordDetail = historyView.findViewById(R.id.containerRecordDetail);
            android.widget.ImageView ivRecordArrow = historyView.findViewById(R.id.ivRecordArrow);

            layoutRecordHeader.setOnClickListener(v -> {
                boolean isExpanded = containerRecordDetail.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    containerRecordDetail.setVisibility(View.GONE);
                    ivRecordArrow.setRotation(0);
                } else {
                    containerRecordDetail.setVisibility(View.VISIBLE);
                    ivRecordArrow.setRotation(180);
                }
            });

            if (i == histories.size() - 1) {
                View connector = historyView.findViewById(R.id.doctorMedicalRecordHistoryItemConnector);
                if (connector != null) {
                    connector.setVisibility(View.INVISIBLE);
                }
            }

            containerMedicalRecordResult.addView(historyView);
        }
    }

    private void renderPatientInfo(PatientProfile patient, int recordCount) {
        layoutMedicalRecordEmptyState.setVisibility(View.GONE);
        containerMedicalRecordResult.setVisibility(View.VISIBLE);
        containerMedicalRecordResult.removeAllViews();

        View patientCard = getLayoutInflater().inflate(
                R.layout.doctor_medical_record_patient_card,
                containerMedicalRecordResult,
                false
        );

        TextView tvPatientName = patientCard.findViewById(R.id.tvPatientName);
        TextView tvPatientExaminedCount = patientCard.findViewById(R.id.tvPatientExaminedCount);
        TextView tvPatientMeta = patientCard.findViewById(R.id.tvPatientMeta);
        TextView tvPatientPhone = patientCard.findViewById(R.id.tvPatientPhone);
        TextView tvPatientAddress = patientCard.findViewById(R.id.tvPatientAddress);

        tvPatientName.setText(safeText(patient.getHo_ten()));
        tvPatientExaminedCount.setText(recordCount + " lần khám");

        long patientIdNum = 0;
        try {
            if (patient.getId() != null) {
                patientIdNum = Long.parseLong(patient.getId());
            }
        } catch (NumberFormatException ignored) {
        }

        String birthDateStr = "";
        if (patient.getNgay_sinh() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            birthDateStr = sdf.format(patient.getNgay_sinh());
        }

        String meta = patientIdNum + " · " + safeText(patient.getGioi_tinh()) + " · " + birthDateStr;
        tvPatientMeta.setText(meta);
        tvPatientPhone.setText(safeText(patient.getSo_dien_thoai()));
        tvPatientAddress.setText(safeText(patient.getDia_chi()));

        containerMedicalRecordResult.addView(patientCard);

        TextView tvHistoryTitle = new TextView(this);
        tvHistoryTitle.setText("Lịch sử khám (" + recordCount + " lần)");
        tvHistoryTitle.setTextColor(Color.parseColor("#1E293B"));
        tvHistoryTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
        tvHistoryTitle.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(14));
        tvHistoryTitle.setLayoutParams(params);

        containerMedicalRecordResult.addView(tvHistoryTitle);
    }

    private void addRecentSearchChip(PatientProfile patient) {
        long patientIdNum = 0;
        try {
            if (patient.getId() != null) {
                patientIdNum = Long.parseLong(patient.getId());
            }
        } catch (NumberFormatException ignored) {
        }

        String chipText = patientIdNum + " - " + safeText(patient.getHo_ten());

        for (int i = 0; i < chipGroupRecentSearch.getChildCount(); i++) {
            Chip existingChip = (Chip) chipGroupRecentSearch.getChildAt(i);
            if (existingChip.getText().toString().equals(chipText)) {
                return;
            }
        }

        Chip chip = new Chip(this);
        chip.setText(chipText);
        chip.setCheckable(false);
        chip.setCloseIconVisible(false);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setRippleColorResource(android.R.color.darker_gray);
        chip.setOnClickListener(v -> {
            edtMedRecordSearch.setText(String.valueOf(patient.getId()));
            performSearch(String.valueOf(patient.getId()));
        });

        chipGroupRecentSearch.addView(chip, 0);
    }

    private void addStyledItemToContainer(LinearLayout container, String text, int style, int index) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);

        int backgroundColor;
        int accentColor;
        switch (style) {
            case ITEM_STYLE_CLINICAL:
                backgroundColor = Color.parseColor("#FFF6E5");
                accentColor = Color.parseColor("#F59E0B");
                break;
            case ITEM_STYLE_DIAGNOSIS:
                backgroundColor = Color.parseColor("#E8F2FF");
                accentColor = Color.parseColor("#3B82F6");
                break;
            case ITEM_STYLE_MEDICINE:
            default:
                backgroundColor = Color.parseColor("#ECF9EF");
                accentColor = Color.parseColor("#2E7D32");
                break;
        }
        row.setBackground(createRoundedBackground(backgroundColor, 999));

        View leadingView = createItemLeadingView(style, index, accentColor);
        if (leadingView != null) {
            row.addView(leadingView);
        }

        TextView textView = new TextView(this);
        textView.setText(text.trim());
        textView.setTextColor(Color.parseColor("#1E293B"));
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setLineSpacing(0f, 1.1f);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        textView.setLayoutParams(textParams);

        row.addView(textView);
        container.addView(row);
    }

    private List<String> splitDiagnosisItems(String rawDiagnosis) {
        List<String> items = new ArrayList<>();
        if (rawDiagnosis == null) {
            return items;
        }

        String normalized = rawDiagnosis.trim();
        if (normalized.isEmpty()) {
            return items;
        }

        String[] parts = normalized.split("\\s*(?:;|\\r?\\n)+\\s*");
        for (String part : parts) {
            String cleaned = safeText(part);
            if (!cleaned.isEmpty()) {
                items.add(cleaned);
            }
        }
        return items;
    }

    private String buildMedicineDisplayText(MedicalRecordMedicineWrapper medWrapper) {
        if (medWrapper == null || medWrapper.getMedicine() == null) {
            return "";
        }

        String tenThuoc = safeText(medWrapper.getMedicine().getTen_thuoc());
        if (tenThuoc.isEmpty()) {
            return "";
        }

        String hamLuong = safeText(medWrapper.getMedicine().getHam_luong());
        String tenThuocDayDu = tenThuoc;
        if (!hamLuong.isEmpty()) {
            tenThuocDayDu = tenThuoc + " " + hamLuong;
        }

        StringBuilder detailBuilder = new StringBuilder();

        String donVi = safeText(medWrapper.getMedicine().getDon_vi());
        int lieuDung = medWrapper.getLieuDung();
        if (lieuDung > 0) {
            detailBuilder.append(lieuDung);
            if (!donVi.isEmpty()) {
                detailBuilder.append(" ").append(donVi);
            }
        } else if (!donVi.isEmpty()) {
            detailBuilder.append(donVi);
        }

        String tanSuat = safeText(medWrapper.getTanSuat());
        if (!tanSuat.isEmpty()) {
            appendMedicineDetail(detailBuilder, tanSuat);
        }

        String thoiGian = safeText(medWrapper.getThoiGian());
        if (!thoiGian.isEmpty()) {
            appendMedicineDetail(detailBuilder, thoiGian);
        }

        String ghiChu = safeText(medWrapper.getGhiChu());
        StringBuilder builder = new StringBuilder(tenThuocDayDu);
        if (detailBuilder.length() > 0) {
            builder.append(" - ").append(detailBuilder);
        }
        if (!ghiChu.isEmpty()) {
            builder.append(", ").append(ghiChu);
        }

        return builder.toString().trim();
    }

    private void appendMedicineDetail(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private View createItemLeadingView(int style, int index, int accentColor) {
        if (style == ITEM_STYLE_CLINICAL) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(8), dp(8));
            params.setMargins(dp(4), 0, dp(10), 0);
            dot.setLayoutParams(params);
            dot.setBackground(createRoundedBackground(accentColor, 999));
            return dot;
        }

        TextView badge = new TextView(this);
        badge.setTextColor(accentColor);
        badge.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params;
        if (style == ITEM_STYLE_DIAGNOSIS) {
            badge.setText("i");
            params = new LinearLayout.LayoutParams(dp(18), dp(18));
            params.setMargins(0, 0, dp(10), 0);
            badge.setLayoutParams(params);
            badge.setBackground(createOutlinedCircle(accentColor));
        } else {
            badge.setText(index + ".");
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, dp(10), 0);
            badge.setLayoutParams(params);
            badge.setMinWidth(dp(18));
        }

        return badge;
    }

    private GradientDrawable createRoundedBackground(int fillColor, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable createOutlinedCircle(int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void logApiError(String source, Response<?> response) {
        String errorText = "";
        if (response.errorBody() != null) {
            try {
                errorText = response.errorBody().string();
            } catch (IOException e) {
                errorText = "Cannot read errorBody: " + e.getMessage();
            }
        }
        Log.e(TAG, source + " failed. code=" + response.code() + ", message=" + response.message() + ", body=" + errorText);
    }
}
