package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordMedicineWrapper;

public class TabPrescriptionFragment extends Fragment {

    private PrescriptionRepository prescriptionRepository;
    private java.util.List<dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem> selectedMedicines = new java.util.ArrayList<>();

    private android.widget.LinearLayout containerSelectedMedicines;
    private View layoutPrescriptionEmptyState;
    private View cardPrescriptionSummary;
    private android.widget.TextView tvPrescriptionSummary;

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

        containerSelectedMedicines = view.findViewById(R.id.containerSelectedMedicines);
        layoutPrescriptionEmptyState = view.findViewById(R.id.layoutPrescriptionEmptyState);
        cardPrescriptionSummary = view.findViewById(R.id.cardPrescriptionSummary);
        tvPrescriptionSummary = view.findViewById(R.id.tvPrescriptionSummary);


        super.onViewCreated(view, savedInstanceState);

        View btnAddMedicine = view.findViewById(R.id.btnAddMedicine);
        View btnSavePrescription = view.findViewById(R.id.btnSavePrescription);

        if (btnAddMedicine != null) {
            btnAddMedicine.setOnClickListener(v -> showMedicineDialog());
        }

        if (btnSavePrescription != null) {
            btnSavePrescription.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Lưu bệnh án & hoàn thành", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void showMedicineDialog() {
        prescriptionRepository.getAllMedicines().enqueue(new retrofit2.Callback<java.util.List<dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem>>() {
            @Override
            public void onResponse(
                    @androidx.annotation.NonNull retrofit2.Call<java.util.List<dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem>> call,
                    @androidx.annotation.NonNull retrofit2.Response<java.util.List<dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem>> response
            ) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    openMedicineDialog(response.body());
                } else {
                    android.widget.Toast.makeText(requireContext(), "Không tải được danh sách thuốc", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                    @androidx.annotation.NonNull retrofit2.Call<java.util.List<dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem>> call,
                    @androidx.annotation.NonNull Throwable t
            ) {
                if (!isAdded()) return;
                android.widget.Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openMedicineDialog(java.util.List<dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem> medicineItems) {
        View dialogView = getLayoutInflater().inflate(R.layout.doctor_select_medicine_dialog, null, false);

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        androidx.recyclerview.widget.RecyclerView rvMedicineList = dialogView.findViewById(R.id.rvMedicineList);
        android.widget.EditText edtSearchMedicine = dialogView.findViewById(R.id.edtSearchMedicine);
        View btnClose = dialogView.findViewById(R.id.btnCloseMedicineDialog);
        View btnConfirm = dialogView.findViewById(R.id.btnConfirmMedicineSelection);


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
            selectedMedicines.clear();
            for (dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem item : adapter.getItems()) {
                if (item.isSelected()) {
                    selectedMedicines.add(
                            new dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem(item)
                    );
                }
            }


            renderSelectedMedicines();

            android.widget.Toast.makeText(requireContext(),
                    "Đã chọn " + selectedMedicines.size() + " thuốc",
                    android.widget.Toast.LENGTH_SHORT).show();

            dialog.dismiss();

        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }
    private void renderSelectedMedicines() {
        if (containerSelectedMedicines == null) return;

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

        java.util.List<String> frequencyOptions = java.util.Arrays.asList(
                "1 lần/ngày", "2 lần/ngày", "3 lần/ngày", "4 lần/ngày", "Sáng/Tối", "Sáng/Trưa/Tối"
        );
        java.util.List<String> durationOptions = java.util.Arrays.asList(
                "3 ngày", "5 ngày", "7 ngày", "10 ngày", "14 ngày", "1 tháng"
        );

        for (int i = 0; i < selectedMedicines.size(); i++) {
            dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem item = selectedMedicines.get(i);


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
                    ? "đơn vị"
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
                    int doseValue = 0;

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
        if (tvPrescriptionSummary == null) return;

        if (selectedMedicines.isEmpty()) {
            tvPrescriptionSummary.setText("");
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selectedMedicines.size(); i++) {
            dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem item = selectedMedicines.get(i);

            String donVi = item.getMedicine().getDon_vi() == null || item.getMedicine().getDon_vi().trim().isEmpty()
                    ? "đơn vị"
                    : item.getMedicine().getDon_vi().trim();

            builder.append("• ")
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

    private int parseFrequencyPerDay(String frequencyText) {
        if (frequencyText == null) return 0;

        String value = frequencyText.trim().toLowerCase();

        if (value.startsWith("1 lần")) return 1;
        if (value.startsWith("2 lần")) return 2;
        if (value.startsWith("3 lần")) return 3;
        if (value.startsWith("4 lần")) return 4;
        if (value.contains("sáng/tối")) return 2;
        if (value.contains("sáng/trưa/tối")) return 3;

        return 0;
    }

    private int parseDurationDays(String durationText) {
        if (durationText == null) return 0;

        String value = durationText.trim().toLowerCase();

        if (value.startsWith("3 ngày")) return 3;
        if (value.startsWith("5 ngày")) return 5;
        if (value.startsWith("7 ngày")) return 7;
        if (value.startsWith("10 ngày")) return 10;
        if (value.startsWith("14 ngày")) return 14;
        if (value.startsWith("1 tháng")) return 30;

        return 0;
    }

    private int calculateQuantity(dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem item) {
        int dose = item.getLieuDung();
        int frequency = parseFrequencyPerDay(item.getTanSuat());
        int duration = parseDurationDays(item.getThoiGian());

        return dose * frequency * duration;
    }


    private void updateSelectedMedicineComputedViews(
            dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.PrescriptionItem item,
            android.widget.TextView tvQuantity,
            android.widget.TextView tvStock
    ) {
        int quantity = calculateQuantity(item);
        item.setSoLuong(quantity);

        String donVi = item.getMedicine().getDon_vi() == null || item.getMedicine().getDon_vi().trim().isEmpty()
                ? "đơn vị"
                : item.getMedicine().getDon_vi().trim();

        tvQuantity.setText("Số lượng kê: " + quantity + " " + donVi);

        int tonKhoConLai = Math.max(0, item.getMedicine().getTon_kho() - quantity);
        tvStock.setText("TK: " + tonKhoConLai + " " + donVi);
    }

}
