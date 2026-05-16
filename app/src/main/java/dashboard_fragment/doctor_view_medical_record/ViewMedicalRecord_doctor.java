package dashboard_fragment.doctor_view_medical_record;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.Toast;

import java.util.List;

import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import dashboard_fragment.doctor_view_medical_record.ExaminationFormHistoryResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewMedicalRecord_doctor extends AppCompatActivity {
    private EditText edtMedRecordSearch;
    private MaterialButton btnMedRecordSearch;
    private ChipGroup chipGroupRecentSearch;
    private PatientRepository patientRepository;
    private android.widget.LinearLayout layoutMedicalRecordEmptyState;
    private android.widget.LinearLayout containerMedicalRecordResult;

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
        patientRepository.getExaminationFormHistoryByPatientId(String.valueOf(patient.getId())).enqueue(new Callback<List<ExaminationFormHistoryResponse>>() {
            @Override
            public void onResponse(Call<List<ExaminationFormHistoryResponse>> call, Response<List<ExaminationFormHistoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ExaminationFormHistoryResponse> histories = response.body();
                    renderPatientInfo(patient, histories.size());
                    renderHistoryItems(histories);
                } else {
                    renderPatientInfo(patient, 0);
                    Toast.makeText(ViewMedicalRecord_doctor.this, "Không có bệnh án nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ExaminationFormHistoryResponse>> call, Throwable t) {
                Toast.makeText(ViewMedicalRecord_doctor.this, "Lỗi tải bệnh án: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderHistoryItems(List<ExaminationFormHistoryResponse> histories) {
        for (int i = 0; i < histories.size(); i++) {
            ExaminationFormHistoryResponse history = histories.get(i);
            android.view.View historyView = getLayoutInflater().inflate(R.layout.doctor_item_medical_record_history, containerMedicalRecordResult, false);
            
            android.widget.TextView tvRecordDate = historyView.findViewById(R.id.tvRecordDate);
            android.widget.TextView tvRecordStatus = historyView.findViewById(R.id.tvRecordStatus);
            android.widget.TextView tvRecordDoctor = historyView.findViewById(R.id.tvRecordDoctor);
            android.widget.TextView tvRecordSymptom = historyView.findViewById(R.id.tvRecordSymptom);
            
            android.widget.TextView tvDiagnosisCount = historyView.findViewById(R.id.tvDiagnosisCount);
            android.widget.TextView tvMedicineCount = historyView.findViewById(R.id.tvMedicineCount);
            android.widget.TextView tvClinicalCount = historyView.findViewById(R.id.tvClinicalCount);

            tvRecordDate.setText(history.getNgay_kham() != null ? history.getNgay_kham() : "");
            tvRecordStatus.setText(history.getTrang_thai());
            
            String doctorName = (history.getProfiles() != null) ? history.getProfiles().getHo_ten() : "Bác sĩ";
            tvRecordDoctor.setText(String.format("BS. %s · %d", doctorName, history.getId()));
            
            tvRecordSymptom.setText(history.getTrieu_chung_ban_dau());

            int diagCount = 0;
            int medCount = 0;
            int clinCount = 0;

            if (history.getMedical_record() != null && !history.getMedical_record().isEmpty()) {
                ExaminationFormHistoryResponse.MedicalRecordInner mr = history.getMedical_record().get(0);
                
                if (mr.getChanDoanChinh() != null && !mr.getChanDoanChinh().trim().isEmpty()) {
                    diagCount += mr.getChanDoanChinh().trim().split("\n").length;
                }
                if (mr.getChanDoanBoSung() != null && !mr.getChanDoanBoSung().trim().isEmpty()) {
                    diagCount += mr.getChanDoanBoSung().trim().split("\n").length;
                }
                
                if (mr.getMedicineData() != null) {
                    medCount = mr.getMedicineData().size();
                }
                
                if (mr.getClinicalData() != null) {
                    clinCount = mr.getClinicalData().size();
                }

                android.widget.TextView tvRecordSymptomDetail = historyView.findViewById(R.id.tvRecordSymptomDetail);
                if (mr.getGhiChuLamSang() != null && !mr.getGhiChuLamSang().isEmpty()) {
                    tvRecordSymptomDetail.setText(mr.getGhiChuLamSang());
                } else {
                    tvRecordSymptomDetail.setText(history.getTrieu_chung_ban_dau());
                }
            } else {
                android.widget.TextView tvRecordSymptomDetail = historyView.findViewById(R.id.tvRecordSymptomDetail);
                tvRecordSymptomDetail.setText(history.getTrieu_chung_ban_dau());
            }
            
            tvDiagnosisCount.setText(diagCount + " chẩn đoán");
            tvMedicineCount.setText(medCount + " thuốc");
            tvClinicalCount.setText(clinCount + " CLS");

            android.widget.LinearLayout layoutRecordHeader = historyView.findViewById(R.id.layoutRecordHeader);
            android.widget.LinearLayout containerRecordDetail = historyView.findViewById(R.id.containerRecordDetail);
            android.widget.ImageView ivRecordArrow = historyView.findViewById(R.id.ivRecordArrow);
            
            layoutRecordHeader.setOnClickListener(v -> {
                boolean isExpanded = containerRecordDetail.getVisibility() == android.view.View.VISIBLE;
                if (isExpanded) {
                    containerRecordDetail.setVisibility(android.view.View.GONE);
                    ivRecordArrow.setRotation(0);
                } else {
                    containerRecordDetail.setVisibility(android.view.View.VISIBLE);
                    ivRecordArrow.setRotation(180);
                }
            });

            if (i == histories.size() - 1) {
                android.view.View connector = historyView.findViewById(R.id.doctorMedicalRecordHistoryItemConnector);
                if (connector != null) {
                    connector.setVisibility(android.view.View.INVISIBLE);
                }
            }

            containerMedicalRecordResult.addView(historyView);
        }
    }

    private void renderPatientInfo(PatientProfile patient, int recordCount) {
        layoutMedicalRecordEmptyState.setVisibility(android.view.View.GONE);
        containerMedicalRecordResult.setVisibility(android.view.View.VISIBLE);
        
        containerMedicalRecordResult.removeAllViews();
        
        android.view.View patientCard = getLayoutInflater().inflate(R.layout.doctor_medical_record_patient_card, containerMedicalRecordResult, false);
        
        android.widget.TextView tvPatientName = patientCard.findViewById(R.id.tvPatientName);
        android.widget.TextView tvPatientExaminedCount = patientCard.findViewById(R.id.tvPatientExaminedCount);
        android.widget.TextView tvPatientMeta = patientCard.findViewById(R.id.tvPatientMeta);
        android.widget.TextView tvPatientPhone = patientCard.findViewById(R.id.tvPatientPhone);
        android.widget.TextView tvPatientAddress = patientCard.findViewById(R.id.tvPatientAddress);
        
        tvPatientName.setText(patient.getHo_ten());
        tvPatientExaminedCount.setText(recordCount + " lần khám");
        
        long patientIdNum = 0;
        try {
            if (patient.getId() != null) {
                patientIdNum = Long.parseLong(patient.getId());
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        String birthDateStr = "";
        if (patient.getNgay_sinh() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            birthDateStr = sdf.format(patient.getNgay_sinh());
        }
        
        String meta = String.format("%d · %s · %s", patientIdNum, patient.getGioi_tinh(), birthDateStr);
        tvPatientMeta.setText(meta);
        
        tvPatientPhone.setText(patient.getSo_dien_thoai());
        tvPatientAddress.setText(patient.getDia_chi());
        
        containerMedicalRecordResult.addView(patientCard);

        android.widget.TextView tvHistoryTitle = new android.widget.TextView(this);
        tvHistoryTitle.setText("Lịch sử khám (" + recordCount + " lần)");
        tvHistoryTitle.setTextColor(android.graphics.Color.parseColor("#1E293B"));
        tvHistoryTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
        tvHistoryTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, (int) (14 * getResources().getDisplayMetrics().density));
        tvHistoryTitle.setLayoutParams(params);
        
        containerMedicalRecordResult.addView(tvHistoryTitle);
    }

    private void addRecentSearchChip(PatientProfile patient) {
        long patientIdNum = 0;
        try {
            if (patient.getId() != null) {
                patientIdNum = Long.parseLong(patient.getId());
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        String chipText = String.format("%d - %s", patientIdNum, patient.getHo_ten());
        
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
}