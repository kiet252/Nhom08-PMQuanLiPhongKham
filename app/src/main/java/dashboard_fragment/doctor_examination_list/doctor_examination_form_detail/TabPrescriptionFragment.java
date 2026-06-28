package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.FullMedicalRecordResponse;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordMedicineWrapper;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionRecommendationAssistant;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db.SaveFullMedicalRecordMedicinePayload;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.send_full_medical_record_db.SaveFullMedicalRecordRequest;
import retrofit2.Call;

public class TabPrescriptionFragment extends Fragment {
    private static final String TAG = "PrescriptionSuggest";

    private PrescriptionRepository prescriptionRepository;
    private MedicalRecordRepository medicalRecordRepository;
    private PrescriptionRecommendationAssistant recommendationAssistant;
    private List<PrescriptionItem> selectedMedicines = new ArrayList<>();
    private List<PrescriptionRecommendationAssistant.RecommendationItem> pendingRecommendationItems = new ArrayList<>();

    private android.widget.LinearLayout containerSelectedMedicines;
    private View layoutPrescriptionEmptyState;
    private View cardPrescriptionSummary;
    private View cardMedicineRecommendation;
    private View layoutRecommendationActions;
    private android.widget.TextView tvPrescriptionSummary;
    private android.widget.TextView tvRecommendationStatus;
    private android.widget.TextView tvRecommendationContent;
    private android.widget.TextView tvRecommendationWarning;
    private DoctorExDetailViewModel doctorExDetailViewModel;
    private final Handler recommendationHandler = new Handler(Looper.getMainLooper());
    private String lastRecommendationSignature = "";

    private final Runnable recommendationRunnable = this::loadAutomaticRecommendation;

    public TabPrescriptionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_prescription, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        prescriptionRepository = new PrescriptionRepository(requireContext());
        medicalRecordRepository = new MedicalRecordRepository(requireContext());
        recommendationAssistant = new PrescriptionRecommendationAssistant();

        containerSelectedMedicines = view.findViewById(R.id.containerSelectedMedicines);
        layoutPrescriptionEmptyState = view.findViewById(R.id.layoutPrescriptionEmptyState);
        cardPrescriptionSummary = view.findViewById(R.id.cardPrescriptionSummary);
        cardMedicineRecommendation = view.findViewById(R.id.cardMedicineRecommendation);
        layoutRecommendationActions = view.findViewById(R.id.layoutRecommendationActions);
        tvPrescriptionSummary = view.findViewById(R.id.tvPrescriptionSummary);
        tvRecommendationStatus = view.findViewById(R.id.tvRecommendationStatus);
        tvRecommendationContent = view.findViewById(R.id.tvRecommendationContent);
        tvRecommendationWarning = view.findViewById(R.id.tvRecommendationWarning);
        doctorExDetailViewModel =
                new ViewModelProvider(requireActivity()).get(DoctorExDetailViewModel.class);
        selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();

        super.onViewCreated(view, savedInstanceState);
        observeMedicalRecord();
        renderSelectedMedicines();

        View btnAddMedicine = view.findViewById(R.id.btnAddMedicine);
        View btnSavePrescription = view.findViewById(R.id.btnSavePrescription);
        View btnApplyRecommendation = view.findViewById(R.id.btnApplyRecommendation);

        if (btnAddMedicine != null) {
            btnAddMedicine.setOnClickListener(v -> showMedicineDialog());
        }

        if (btnApplyRecommendation != null) {
            btnApplyRecommendation.setOnClickListener(v -> applyRecommendationWithConfirmation());
        }

        if (btnSavePrescription != null) {
            btnSavePrescription.setOnClickListener(v -> saveFullMedicalRecord());
        }

        observeDiagnosisForRecommendation();
    }

    @Override
    public void onDestroyView() {
        recommendationHandler.removeCallbacks(recommendationRunnable);
        super.onDestroyView();
    }

    private void observeMedicalRecord() {
        doctorExDetailViewModel.getMedicalRecord().observe(getViewLifecycleOwner(), record -> {
            if (record == null
                    || record.getMedicineData() == null
                    || doctorExDetailViewModel.hasPrescriptionSelectionInitialized()) {
                return;
            }

            List<PrescriptionItem> prefilledMedicines = new ArrayList<>();

            for (MedicalRecordMedicineWrapper wrapper : record.getMedicineData()) {
                if (wrapper == null || wrapper.getMedicine() == null) {
                    continue;
                }

                PrescriptionItem item = new PrescriptionItem(wrapper.getMedicine());

                int parsedDose = wrapper.getLieuDung();
                if (parsedDose > 0) {
                    item.setLieuDung(parsedDose);
                }

                if (wrapper.getGhiChu() != null && !wrapper.getGhiChu().trim().isEmpty()) {
                    item.setGhiChu(wrapper.getGhiChu().trim());
                }

                item.setTanSuat(wrapper.getTanSuat() == null ? "" : wrapper.getTanSuat().trim());
                item.setThoiGian(wrapper.getThoiGian() == null ? "" : wrapper.getThoiGian().trim());

                if (wrapper.getSoLuong() > 0) {
                    item.setSoLuong(wrapper.getSoLuong());
                }

                prefilledMedicines.add(item);
            }

            prescriptionRepository.getAllMedicines().enqueue(new retrofit2.Callback<List<MedicineItem>>() {
                @Override
                public void onResponse(@NonNull Call<List<MedicineItem>> call, @NonNull retrofit2.Response<List<MedicineItem>> response) {
                    if (!isAdded()) return;
                    
                    if (response.isSuccessful() && response.body() != null) {
                        List<MedicineItem> allMedicines = response.body();
                        for (PrescriptionItem prefilled : prefilledMedicines) {
                            if (prefilled.getMedicine() != null) {
                                for (MedicineItem fullMedicine : allMedicines) {
                                    if (prefilled.getMedicine().getId() == fullMedicine.getId()) {
                                        prefilled.getMedicine().setTon_kho(fullMedicine.getTon_kho());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    doctorExDetailViewModel.initializeSelectedMedicines(prefilledMedicines);
                    selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();
                    renderSelectedMedicines();
                }

                @Override
                public void onFailure(@NonNull Call<List<MedicineItem>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    
                    doctorExDetailViewModel.initializeSelectedMedicines(prefilledMedicines);
                    selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();
                    renderSelectedMedicines();
                }
            });
        });
    }

    private void observeDiagnosisForRecommendation() {
        doctorExDetailViewModel.getDiagnosisInputVersion().observe(getViewLifecycleOwner(), ignored ->
                scheduleAutomaticRecommendation()
        );
        scheduleAutomaticRecommendation();
    }

    private void scheduleAutomaticRecommendation() {
        recommendationHandler.removeCallbacks(recommendationRunnable);

        if (TextUtils.isEmpty(buildDiagnosisText())) {
            pendingRecommendationItems = new ArrayList<>();
            lastRecommendationSignature = "";
            if (cardMedicineRecommendation != null) {
                cardMedicineRecommendation.setVisibility(View.GONE);
            }
            return;
        }

        recommendationHandler.postDelayed(recommendationRunnable, 3000);
    }

    private void loadAutomaticRecommendation() {
        if (!isAdded()) {
            return;
        }

        String primaryDiagnosis = buildDiagnosisText();
        String additionalDiagnosis = doctorExDetailViewModel.getAdditionalDiagnosis();
        String clinicalNote = doctorExDetailViewModel.getClinicalNote();
        String signature = buildRecommendationSignature(primaryDiagnosis, additionalDiagnosis, clinicalNote);

        if (TextUtils.isEmpty(primaryDiagnosis)) {
            return;
        }

        if (signature.equals(lastRecommendationSignature) && !pendingRecommendationItems.isEmpty()) {
            return;
        }

        lastRecommendationSignature = signature;
        showRecommendationLoading();

        prescriptionRepository.getAllMedicines().enqueue(new retrofit2.Callback<List<MedicineItem>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<MedicineItem>> call,
                    @NonNull retrofit2.Response<List<MedicineItem>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    showRecommendationError("Không tải được danh mục thuốc để gợi ý.");
                    return;
                }

                recommendationAssistant.recommend(
                        primaryDiagnosis,
                        additionalDiagnosis,
                        clinicalNote,
                        response.body(),
                        new PrescriptionRecommendationAssistant.RecommendationCallback() {
                            @Override
                            public void onSuccess(PrescriptionRecommendationAssistant.RecommendationResult result) {
                                if (!isAdded()) {
                                    return;
                                }
                                requireActivity().runOnUiThread(() -> {
                                    if (signature.equals(currentRecommendationSignature())) {
                                        renderRecommendationResult(result);
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                if (!isAdded()) {
                                    return;
                                }
                                requireActivity().runOnUiThread(() -> {
                                    if (signature.equals(currentRecommendationSignature())) {
                                        showRecommendationError(message);
                                    }
                                });
                            }
                        }
                );
            }

            @Override
            public void onFailure(@NonNull Call<List<MedicineItem>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                showRecommendationError("Lỗi kết nối khi tải danh mục thuốc.");
            }
        });
    }

    private String currentRecommendationSignature() {
        return buildRecommendationSignature(
                buildDiagnosisText(),
                doctorExDetailViewModel.getAdditionalDiagnosis(),
                doctorExDetailViewModel.getClinicalNote()
        );
    }

    private String buildRecommendationSignature(
            String primaryDiagnosis,
            String additionalDiagnosis,
            String clinicalNote
    ) {
        return safeTrim(primaryDiagnosis) + "\n" + safeTrim(additionalDiagnosis) + "\n" + safeTrim(clinicalNote);
    }

    private void showRecommendationLoading() {
        if (cardMedicineRecommendation == null) {
            return;
        }

        cardMedicineRecommendation.setVisibility(View.VISIBLE);
        tvRecommendationStatus.setText("Đang phân tích chẩn đoán sau khi bác sĩ ngừng nhập...");
        tvRecommendationContent.setText("");
        tvRecommendationWarning.setVisibility(View.GONE);
        layoutRecommendationActions.setVisibility(View.GONE);
    }

    private void showRecommendationError(String message) {
        Log.e(TAG, "Unable to load medicine recommendation: " + (message == null ? "" : message));
        pendingRecommendationItems = new ArrayList<>();
        if (cardMedicineRecommendation == null) {
            return;
        }

        cardMedicineRecommendation.setVisibility(View.VISIBLE);
        tvRecommendationStatus.setText("Không thể lấy gợi ý thuốc, hãy liên hệ quản trị viên");
        tvRecommendationContent.setText("");
        tvRecommendationWarning.setVisibility(View.GONE);
        layoutRecommendationActions.setVisibility(View.GONE);
    }

    private void renderRecommendationResult(PrescriptionRecommendationAssistant.RecommendationResult result) {
        pendingRecommendationItems = result == null
                ? new ArrayList<>()
                : result.getMatchedItems();

        if (cardMedicineRecommendation == null) {
            return;
        }

        cardMedicineRecommendation.setVisibility(View.VISIBLE);

        if (pendingRecommendationItems.isEmpty()) {
            tvRecommendationStatus.setText("Không có thuốc phù hợp trong danh mục hiện tại.");
            tvRecommendationContent.setText("Gemini có thể đã gợi ý thuốc ngoài danh mục thuốc của phòng khám.");
            layoutRecommendationActions.setVisibility(View.GONE);
        } else {
            tvRecommendationStatus.setText("Gợi ý tham khảo từ Gemini, bác sĩ cần kiểm tra trước khi lưu.");
            tvRecommendationContent.setText(buildRecommendationDisplayText(pendingRecommendationItems));
            layoutRecommendationActions.setVisibility(View.VISIBLE);
        }

        if (result != null && !result.getUnmatchedNames().isEmpty()) {
            tvRecommendationWarning.setVisibility(View.VISIBLE);
            tvRecommendationWarning.setText(
                    "Không áp dụng thuốc ngoài danh mục: "
                            + TextUtils.join(", ", result.getUnmatchedNames())
            );
        } else {
            tvRecommendationWarning.setVisibility(View.GONE);
        }
    }

    private String buildRecommendationDisplayText(
            List<PrescriptionRecommendationAssistant.RecommendationItem> items
    ) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            PrescriptionRecommendationAssistant.RecommendationItem item = items.get(i);
            MedicineItem medicine = item.getMedicine();
            String donVi = medicine.getDon_vi() == null || medicine.getDon_vi().trim().isEmpty()
                    ? "đơn vị"
                    : medicine.getDon_vi().trim();

            builder.append("• ")
                    .append(medicine.getTen_thuoc())
                    .append(" - ")
                    .append(item.getDose())
                    .append(" ")
                    .append(donVi)
                    .append(", ")
                    .append(item.getFrequency())
                    .append(", ")
                    .append(item.getDuration());

            if (!TextUtils.isEmpty(item.getNote())) {
                builder.append(", ").append(item.getNote());
            }

            if (!TextUtils.isEmpty(item.getReason())) {
                builder.append("\n  Lý do: ").append(item.getReason());
            }

            if (i < items.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void applyRecommendationWithConfirmation() {
        if (pendingRecommendationItems == null || pendingRecommendationItems.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có gợi ý thuốc để áp dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (doctorExDetailViewModel.getSelectedMedicines().isEmpty()) {
            replacePrescriptionWithRecommendation();
            return;
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Áp dụng gợi ý thuốc")
                .setMessage("Đơn thuốc hiện tại đã có thuốc. Bạn muốn thêm thuốc gợi ý hay thay thế toàn bộ đơn hiện tại?")
                .setPositiveButton("Thêm vào đơn", (dialog, which) -> appendRecommendationToPrescription())
                .setNegativeButton("Thay thế", (dialog, which) -> replacePrescriptionWithRecommendation())
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void appendRecommendationToPrescription() {
        List<PrescriptionItem> merged = new ArrayList<>(doctorExDetailViewModel.getSelectedMedicines());
        Set<Integer> existingIds = new LinkedHashSet<>();

        for (PrescriptionItem item : merged) {
            if (item != null && item.getMedicine() != null) {
                existingIds.add(item.getMedicine().getId());
            }
        }

        for (PrescriptionRecommendationAssistant.RecommendationItem item : pendingRecommendationItems) {
            if (item == null || item.getMedicine() == null || existingIds.contains(item.getMedicine().getId())) {
                continue;
            }
            merged.add(toPrescriptionItem(item));
            existingIds.add(item.getMedicine().getId());
        }

        doctorExDetailViewModel.replaceSelectedMedicines(merged);
        selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();
        renderSelectedMedicines();
        Toast.makeText(requireContext(), "Đã thêm thuốc gợi ý vào đơn", Toast.LENGTH_SHORT).show();
    }

    private void replacePrescriptionWithRecommendation() {
        List<PrescriptionItem> medicines = new ArrayList<>();
        for (PrescriptionRecommendationAssistant.RecommendationItem item : pendingRecommendationItems) {
            if (item != null && item.getMedicine() != null) {
                medicines.add(toPrescriptionItem(item));
            }
        }

        doctorExDetailViewModel.replaceSelectedMedicines(medicines);
        selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();
        renderSelectedMedicines();
        Toast.makeText(requireContext(), "Đã áp dụng gợi ý thuốc", Toast.LENGTH_SHORT).show();
    }

    private PrescriptionItem toPrescriptionItem(
            PrescriptionRecommendationAssistant.RecommendationItem recommendation
    ) {
        PrescriptionItem item = new PrescriptionItem(recommendation.getMedicine());
        item.setLieuDung(Math.max(1, recommendation.getDose()));
        item.setTanSuat(safeTrim(recommendation.getFrequency()));
        item.setThoiGian(safeTrim(recommendation.getDuration()));
        item.setGhiChu(safeTrim(recommendation.getNote()));
        item.setSoLuong(calculateQuantity(item));
        return item;
    }

    private void showMedicineDialog() {
        prescriptionRepository.getAllMedicines().enqueue(new retrofit2.Callback<List<MedicineItem>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<MedicineItem>> call,
                    @NonNull retrofit2.Response<List<MedicineItem>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    openMedicineDialog(response.body());
                } else {
                    Toast.makeText(requireContext(), "Không tải được danh sách thuốc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MedicineItem>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "Lỗi kết nối" + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void openMedicineDialog(List<MedicineItem> medicineItems) {
        View dialogView = getLayoutInflater().inflate(R.layout.doctor_select_medicine_dialog, null, false);

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        androidx.recyclerview.widget.RecyclerView rvMedicineList = dialogView.findViewById(R.id.rvMedicineList);
        android.widget.EditText edtSearchMedicine = dialogView.findViewById(R.id.edtSearchMedicine);
        View btnClose = dialogView.findViewById(R.id.btnCloseMedicineDialog);
        View btnConfirm = dialogView.findViewById(R.id.btnConfirmMedicineSelection);

        Set<Integer> selectedMedicineIds = new LinkedHashSet<>();
        for (PrescriptionItem selectedMedicine : selectedMedicines) {
            if (selectedMedicine != null && selectedMedicine.getMedicine() != null) {
                selectedMedicineIds.add(selectedMedicine.getMedicine().getId());
            }
        }
        for (MedicineItem medicineItem : medicineItems) {
            medicineItem.setSelected(selectedMedicineIds.contains(medicineItem.getId()));
        }

        dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineSelectAdapter adapter =
                new dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineSelectAdapter(medicineItems);

        rvMedicineList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        rvMedicineList.setAdapter(adapter);

        edtSearchMedicine.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                adapter.filter(s == null ? "" : s.toString());
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            List<PrescriptionItem> updatedMedicines = new ArrayList<>();
            for (MedicineItem item : adapter.getItems()) {
                if (item.isSelected()) {
                    PrescriptionItem existingItem = null;
                    for (PrescriptionItem oldItem : selectedMedicines) {
                        if (oldItem.getMedicine().getId() == item.getId()) {
                            existingItem = oldItem;
                            break;
                        }
                    }
                    if (existingItem != null) {
                        updatedMedicines.add(existingItem);
                    } else {
                        PrescriptionItem newItem = new PrescriptionItem(item);
                        newItem.setLieuDung(1);
                        updatedMedicines.add(newItem);
                    }
                }
            }

            doctorExDetailViewModel.replaceSelectedMedicines(updatedMedicines);
            selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();
            renderSelectedMedicines();

            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private void renderSelectedMedicines() {
        selectedMedicines = doctorExDetailViewModel.getSelectedMedicines();
        if (containerSelectedMedicines == null) {
            return;
        }

        containerSelectedMedicines.removeAllViews();

        if (selectedMedicines.isEmpty()) {
            containerSelectedMedicines.setVisibility(View.GONE);
            if (layoutPrescriptionEmptyState != null) {
                layoutPrescriptionEmptyState.setVisibility(View.VISIBLE);
            }
            if (cardPrescriptionSummary != null) {
                cardPrescriptionSummary.setVisibility(View.GONE);
            }
            return;
        }

        containerSelectedMedicines.setVisibility(View.VISIBLE);
        if (layoutPrescriptionEmptyState != null) {
            layoutPrescriptionEmptyState.setVisibility(View.GONE);
        }
        if (cardPrescriptionSummary != null) {
            cardPrescriptionSummary.setVisibility(View.VISIBLE);
        }

        List<String> frequencyOptions = java.util.Arrays.asList(
                "1 l\u1ea7n/ng\u00e0y", "2 l\u1ea7n/ng\u00e0y", "3 l\u1ea7n/ng\u00e0y", "4 l\u1ea7n/ng\u00e0y", "S\u00e1ng/T\u1ed1i", "S\u00e1ng/Tr\u01b0a/T\u1ed1i"
        );
        List<String> durationOptions = java.util.Arrays.asList(
                "3 ng\u00e0y", "5 ng\u00e0y", "7 ng\u00e0y", "10 ng\u00e0y", "14 ng\u00e0y", "1 th\u00e1ng"
        );

        for (int i = 0; i < selectedMedicines.size(); i++) {
            PrescriptionItem item = selectedMedicines.get(i);
            View itemView = getLayoutInflater().inflate(R.layout.doctor_item_selected_medicine, containerSelectedMedicines, false);

            android.widget.TextView tvIndex = itemView.findViewById(R.id.tvMedicineIndex);
            android.widget.TextView tvName = itemView.findViewById(R.id.tvSelectedMedicineName);
            android.widget.TextView tvStock = itemView.findViewById(R.id.tvSelectedMedicineStock);
            android.widget.TextView tvDoseUnit = itemView.findViewById(R.id.tvSelectedDoseUnit);
            android.widget.TextView tvQuantity = itemView.findViewById(R.id.tvSelectedQuantity);

            android.widget.EditText edtDose = itemView.findViewById(R.id.edtSelectedDose);
            android.widget.EditText edtNote = itemView.findViewById(R.id.edtSelectedNote);
            android.widget.AutoCompleteTextView spinnerFrequency = itemView.findViewById(R.id.spinnerSelectedFrequency);
            android.widget.AutoCompleteTextView spinnerDuration = itemView.findViewById(R.id.spinnerSelectedDuration);
            android.widget.ImageView btnRemove = itemView.findViewById(R.id.btnRemoveMedicine);

            tvIndex.setText(String.valueOf(i + 1));
            tvName.setText(item.getMedicine().getTen_thuoc());

            String donVi = item.getMedicine().getDon_vi() == null || item.getMedicine().getDon_vi().trim().isEmpty()
                    ? "\u0111\u01a1n v\u1ecb"
                    : item.getMedicine().getDon_vi().trim();

            tvDoseUnit.setText(donVi);

            edtDose.setText(String.valueOf(item.getLieuDung()));
            edtNote.setText(item.getGhiChu());

            updateSelectedMedicineComputedViews(item, tvQuantity, tvStock);

            android.widget.ArrayAdapter<String> frequencyAdapter = new android.widget.ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    frequencyOptions
            );
            spinnerFrequency.setAdapter(frequencyAdapter);
            spinnerFrequency.setText(item.getTanSuat(), false);

            android.widget.ArrayAdapter<String> durationAdapter = new android.widget.ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    durationOptions
            );
            spinnerDuration.setAdapter(durationAdapter);
            spinnerDuration.setText(item.getThoiGian(), false);

            spinnerFrequency.setThreshold(0);
            spinnerDuration.setThreshold(0);

            spinnerFrequency.setOnClickListener(v -> spinnerFrequency.showDropDown());
            spinnerDuration.setOnClickListener(v -> spinnerDuration.showDropDown());

            spinnerFrequency.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    spinnerFrequency.showDropDown();
                }
            });

            spinnerDuration.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    spinnerDuration.showDropDown();
                }
            });

            spinnerFrequency.setOnItemClickListener((parent, view1, position, id) -> {
                item.setTanSuat(frequencyOptions.get(position));
                updateSelectedMedicineComputedViews(item, tvQuantity, tvStock);
                updatePrescriptionSummary();
            });

            spinnerDuration.setOnItemClickListener((parent, view12, position, id) -> {
                item.setThoiGian(durationOptions.get(position));
                updateSelectedMedicineComputedViews(item, tvQuantity, tvStock);
                updatePrescriptionSummary();
            });

            edtDose.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    int doseValue;

                    try {
                        String value = s == null ? "" : s.toString().trim();
                        doseValue = value.isEmpty() ? 0 : Integer.parseInt(value);
                    } catch (Exception e) {
                        doseValue = 0;
                    }

                    item.setLieuDung(doseValue);
                    updateSelectedMedicineComputedViews(item, tvQuantity, tvStock);
                    updatePrescriptionSummary();
                }
            });

            edtNote.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    item.setGhiChu(s == null ? "" : s.toString().trim());
                    updatePrescriptionSummary();
                }
            });

            btnRemove.setOnClickListener(v -> {
                selectedMedicines.remove(item);
                renderSelectedMedicines();
            });

            containerSelectedMedicines.addView(itemView);
        }

        updatePrescriptionSummary();
    }

    private void updatePrescriptionSummary() {
        if (tvPrescriptionSummary == null) {
            return;
        }

        if (selectedMedicines.isEmpty()) {
            tvPrescriptionSummary.setText("");
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selectedMedicines.size(); i++) {
            PrescriptionItem item = selectedMedicines.get(i);

            String donVi = item.getMedicine().getDon_vi() == null || item.getMedicine().getDon_vi().trim().isEmpty()
                    ? "\u0111\u01a1n v\u1ecb"
                    : item.getMedicine().getDon_vi().trim();

            builder.append("\u2022 ")
                    .append(item.getMedicine().getTen_thuoc())
                    .append(" - ")
                    .append(item.getLieuDung())
                    .append(" ")
                    .append(donVi)
                    .append(", ")
                    .append(item.getTanSuat())
                    .append(", ")
                    .append(item.getThoiGian());

            if (item.getGhiChu() != null && !item.getGhiChu().trim().isEmpty()) {
                builder.append(", ").append(item.getGhiChu().trim());
            }

            if (i < selectedMedicines.size() - 1) {
                builder.append("\n");
            }
        }

        tvPrescriptionSummary.setText(builder.toString());
    }

    private void saveFullMedicalRecord() {
        long exFormId = readCurrentFormId();
        if (exFormId <= 0) {
            Toast.makeText(requireContext(), "Không xác định được phiếu khám để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = SharedPrefManager.getInstance(requireContext()).getProfile().getID();

        if (doctorId == null || doctorId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin bác sĩ điều trị", Toast.LENGTH_SHORT).show();
            return;
        }

        for (PrescriptionItem item : doctorExDetailViewModel.getSelectedMedicines()) {
            if (item.getSoLuong() > item.getMedicine().getTon_kho()) {
                Toast.makeText(requireContext(), "Lỗi: Số lượng kê của thuốc " + item.getMedicine().getTen_thuoc() + " vượt quá tồn kho!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        SaveFullMedicalRecordRequest request = new SaveFullMedicalRecordRequest(
                exFormId,
                doctorId,
                buildDiagnosisText(),
                doctorExDetailViewModel.getAdditionalDiagnosis(),
                doctorExDetailViewModel.getClinicalNote(),
                buildClinicalIdsPayload(),
                buildMedicinePayload()
        );

        medicalRecordRepository.saveFullMedicalRecord(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Lưu bệnh án thành công", Toast.LENGTH_SHORT).show();
                    ExaminationFormDetail_doctor activity = (ExaminationFormDetail_doctor) requireActivity();

                    activity.navigateToPatientInfoTab();

                    activity.requestListReload(false);
                    return;
                }

                String errorMessage = "Không thể lưu bệnh án";

                if (response.errorBody() != null) {
                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException ignored) {
                    }
                }

                Log.d("Unable to save record", errorMessage);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_LONG).show();
                Log.d("Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private long readCurrentFormId() {
        ExaminationFormDetail_doctor activity = (ExaminationFormDetail_doctor) requireActivity();
        String formIdText = activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_FORM_ID);
        if (formIdText == null) {
            return -1L;
        }
        try {
            return Long.parseLong(formIdText.trim());
        } catch (NumberFormatException exception) {
            return -1L;
        }
    }

    private String buildDiagnosisText() {
        Set<String> selectedDiagnoses = doctorExDetailViewModel.getSelectedDiagnoses();
        if (!selectedDiagnoses.isEmpty()) {
            return TextUtils.join("; ", selectedDiagnoses);
        }

        FullMedicalRecordResponse medicalRecord = doctorExDetailViewModel.getMedicalRecordValue();
        if (medicalRecord != null && medicalRecord.getDiagnosisNotes() != null) {
            String existingDiagnosis = medicalRecord.getDiagnosisNotes().getChanDoanChinh();
            return existingDiagnosis == null ? "" : existingDiagnosis.trim();
        }
        return "";
    }

    private List<Long> buildClinicalIdsPayload() {
        Set<Integer> clinicalIds = doctorExDetailViewModel.getSelectedClinicalIds();
        if (clinicalIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> payload = new ArrayList<>();
        for (Integer clinicalId : clinicalIds) {
            if (clinicalId != null && clinicalId > 0) {
                payload.add(clinicalId.longValue());
            }
        }
        return payload;
    }

    private List<SaveFullMedicalRecordMedicinePayload> buildMedicinePayload() {
        List<SaveFullMedicalRecordMedicinePayload> payload = new ArrayList<>();
        for (PrescriptionItem item : doctorExDetailViewModel.getSelectedMedicines()) {
            if (item == null || item.getMedicine() == null || item.getMedicine().getId() <= 0) {
                continue;
            }

            payload.add(new SaveFullMedicalRecordMedicinePayload(
                    item.getMedicine().getId(),
                    item.getSoLuong(),
                    Math.max(0, item.getLieuDung()),
                    safeTrim(item.getTanSuat()),
                    safeTrim(item.getThoiGian()),
                    safeTrim(item.getGhiChu())
            ));
        }
        return payload;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private int extractLeadingNumber(String text) {
        if (text == null) {
            return 0;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(text);
        if (!matcher.find()) {
            return 0;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private int parseFrequencyPerDay(String frequencyText) {
        if (frequencyText == null) {
            return 0;
        }

        String value = frequencyText.trim().toLowerCase();

        if (value.startsWith("1 l\u1ea7n")) return 1;
        if (value.startsWith("2 l\u1ea7n")) return 2;
        if (value.startsWith("3 l\u1ea7n")) return 3;
        if (value.startsWith("4 l\u1ea7n")) return 4;
        if (value.contains("s\u00e1ng/t\u1ed1i")) return 2;
        if (value.contains("s\u00e1ng/tr\u01b0a/t\u1ed1i")) return 3;

        return 0;
    }

    private int parseDurationDays(String durationText) {
        if (durationText == null) {
            return 0;
        }

        String value = durationText.trim().toLowerCase();

        if (value.startsWith("3 ng\u00e0y")) return 3;
        if (value.startsWith("5 ng\u00e0y")) return 5;
        if (value.startsWith("7 ng\u00e0y")) return 7;
        if (value.startsWith("10 ng\u00e0y")) return 10;
        if (value.startsWith("14 ng\u00e0y")) return 14;
        if (value.startsWith("1 th\u00e1ng")) return 30;

        return 0;
    }

    private int calculateQuantity(PrescriptionItem item) {
        int dose = item.getLieuDung();
        int frequency = parseFrequencyPerDay(item.getTanSuat());
        int duration = parseDurationDays(item.getThoiGian());

        return dose * frequency * duration;
    }

    private void updateSelectedMedicineComputedViews(
            PrescriptionItem item,
            android.widget.TextView tvQuantity,
            android.widget.TextView tvStock
    ) {
        int quantity = calculateQuantity(item);
        if (quantity <= 0) {
            quantity = item.getSoLuong();
        }
        item.setSoLuong(quantity);

        String donVi = item.getMedicine().getDon_vi() == null || item.getMedicine().getDon_vi().trim().isEmpty()
                ? "\u0111\u01a1n v\u1ecb"
                : item.getMedicine().getDon_vi().trim();

        tvQuantity.setText("Số lượng kê: " + quantity + " " + donVi);

        int tonKhoConLai = item.getMedicine().getTon_kho() - quantity;
        if (tonKhoConLai < 0) {
            tvQuantity.setTextColor(0xFFDC2626); // Red
            tvStock.setText("TK: " + item.getMedicine().getTon_kho() + " " + donVi + " (Thiếu " + Math.abs(tonKhoConLai) + ")");
            tvStock.setTextColor(0xFFDC2626); // Red
        } else {
            tvQuantity.setTextColor(0xFF0D3F6E); // Default Blue
            tvStock.setText("TK: " + tonKhoConLai + " " + donVi);
            tvStock.setTextColor(0xFF0D3F6E); // Default Blue
        }
    }
}
