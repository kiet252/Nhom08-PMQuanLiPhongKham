package dashboard_fragment.staff_manage_bill;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageBill_staff extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputLayout tilSearchPatient;
    private TextInputEditText edtSearchPatient;
    private TextView txtSearchError;
    private RecyclerView rvInvoices;
    private StaffInvoiceAdapter invoiceAdapter;

    private PatientRepository patientRepository;
    private BillRepository billRepository;

    private TextInputEditText edtFromDate;
    private TextInputEditText edtToDate;
    private com.google.android.material.button.MaterialButton btnFilterAll;
    private com.google.android.material.button.MaterialButton btnFilterPaid;
    private com.google.android.material.button.MaterialButton btnFilterUnpaid;

    private final List<StaffInvoiceItem> allInvoices = new ArrayList<>();
    private enum FilterStatus { ALL, PAID, UNPAID }
    private FilterStatus currentFilterStatus = FilterStatus.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_manage_bill);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_bill), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        patientRepository = new PatientRepository(this);
        billRepository = new BillRepository(this);

        initializeViews();
        setupRecycler();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tilSearchPatient = findViewById(R.id.tilSearchPatient);
        edtSearchPatient = findViewById(R.id.edtSearchPatient);
        txtSearchError = findViewById(R.id.txtSearchError);
        rvInvoices = findViewById(R.id.rvInvoices);

        edtFromDate = findViewById(R.id.edtFromDate);
        edtToDate = findViewById(R.id.edtToDate);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPaid = findViewById(R.id.btnFilterPaid);
        btnFilterUnpaid = findViewById(R.id.btnFilterUnpaid);

        // Set default dates to today
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todayStr = sdf.format(calendar.getTime());
        edtFromDate.setText(todayStr);
        edtToDate.setText(todayStr);

        edtSearchPatient.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        edtSearchPatient.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchInvoicesByPatient();
                return true;
            }
            return false;
        });
    }

    private void setupRecycler() {
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        invoiceAdapter = new StaffInvoiceAdapter();
        invoiceAdapter.setOnInvoiceClickListener(this::openInvoiceDetail);
        rvInvoices.setAdapter(invoiceAdapter);
        invoiceAdapter.submitList(new ArrayList<>());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> backToPreviousActivity());

        tilSearchPatient.setStartIconOnClickListener(v -> searchInvoicesByPatient());

        edtFromDate.setOnClickListener(v -> showDatePickerDialog(edtFromDate, true));
        edtToDate.setOnClickListener(v -> showDatePickerDialog(edtToDate, false));

        btnFilterAll.setOnClickListener(v -> {
            currentFilterStatus = FilterStatus.ALL;
            updateFilterButtonsVisuals();
            applyFilter();
        });

        btnFilterPaid.setOnClickListener(v -> {
            currentFilterStatus = FilterStatus.PAID;
            updateFilterButtonsVisuals();
            applyFilter();
        });

        btnFilterUnpaid.setOnClickListener(v -> {
            currentFilterStatus = FilterStatus.UNPAID;
            updateFilterButtonsVisuals();
            applyFilter();
        });

        updateFilterButtonsVisuals();
    }

    private void searchInvoicesByPatient() {
        String keyword = edtSearchPatient.getText() != null
                ? edtSearchPatient.getText().toString().trim()
                : "";

        hideSearchError();
        edtSearchPatient.setError(null);

        if (keyword.isEmpty()) {
            edtSearchPatient.setError("Vui lòng nhập mã bệnh nhân hoặc CCCD");
            edtSearchPatient.requestFocus();
            allInvoices.clear();
            applyFilter();
            return;
        }

        patientRepository.getProfileByIdOrCccd(keyword).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call,
                                   @NonNull Response<List<PatientProfile>> response) {
                if (!response.isSuccessful()) {
                    showSearchError();
                    showApiError(response);
                    allInvoices.clear();
                    applyFilter();
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {
                    showSearchError();
                    allInvoices.clear();
                    applyFilter();
                    return;
                }

                PatientProfile patient = response.body().get(0);
                loadBillsForPatient(patient);
            }

            @Override
            public void onFailure(@NonNull Call<List<PatientProfile>> call, @NonNull Throwable t) {
                Toast.makeText(ManageBill_staff.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBillsForPatient(PatientProfile patient) {
        billRepository.getBillsByPatientId(patient.getId()).enqueue(new Callback<List<ExamFormWithBillDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExamFormWithBillDto>> call,
                                   @NonNull Response<List<ExamFormWithBillDto>> response) {
                if (!response.isSuccessful()) {
                    showApiError(response);
                    allInvoices.clear();
                    applyFilter();
                    return;
                }

                List<ExamFormWithBillDto> forms = response.body() != null
                        ? response.body()
                        : new ArrayList<>();

                List<StaffInvoiceItem> items = BillMapper.fromExamForms(forms);

                if (items.isEmpty()) {
                    Toast.makeText(ManageBill_staff.this,
                            "Bệnh nhân chưa có hóa đơn nào", Toast.LENGTH_SHORT).show();
                    allInvoices.clear();
                    applyFilter();
                    return;
                }

                hideSearchError();
                fillMissingPatientNames(items, patient.getHo_ten());
                allInvoices.clear();
                allInvoices.addAll(items);
                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<List<ExamFormWithBillDto>> call, @NonNull Throwable t) {
                Toast.makeText(ManageBill_staff.this,
                        "Lỗi tải hóa đơn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillMissingPatientNames(List<StaffInvoiceItem> items, String patientName) {
        if (patientName == null || patientName.isEmpty()) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            StaffInvoiceItem item = items.get(i);
            if ("--".equals(item.getPatientName())) {
                items.set(i, new StaffInvoiceItem(
                        item.getId(),
                        patientName,
                        item.getDate(),
                        item.getAmount(),
                        item.isPaid(),
                        item.getPaymentMethod(),
                        item.getOriginalForm()
                ));
            }
        }
    }

    private void showSearchError() {
        txtSearchError.setVisibility(View.VISIBLE);
    }

    private void hideSearchError() {
        txtSearchError.setVisibility(View.GONE);
    }

    private void showApiError(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
            Toast.makeText(this,
                    "Lỗi: " + response.code() + " " + errorBody,
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateInvoiceList(List<StaffInvoiceItem> invoices) {
        if (invoiceAdapter != null) {
            invoiceAdapter.submitList(invoices);
        }
    }

    private void openInvoiceDetail(StaffInvoiceItem invoice) {
        if (invoice == null || invoice.getOriginalForm() == null) {
            Toast.makeText(this, "Không thể hiển thị: Dữ liệu hóa đơn trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, BillDetail_staff.class);

        intent.putExtra("EXTRA_EXAM_FORM_DATA", invoice.getOriginalForm());

        intent.putExtra("EXTRA_SELECTED_BILL_ID", invoice.getId());

        startActivity(intent);
    }

    private void backToPreviousActivity() {
        finish();
    }

    private void showDatePickerDialog(TextInputEditText editText, boolean isFromDate) {
        String currentText = editText.getText().toString().trim();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(currentText);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            // Use current date if parsing fails
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(selectedYear, selectedMonth, selectedDay);
            String selectedDateStr = sdf.format(selectedCal.getTime());

            if (isFromDate) {
                edtFromDate.setText(selectedDateStr);
            } else {
                edtToDate.setText(selectedDateStr);
            }
            
            applyFilter();
        }, year, month, day);

        // Apply min/max constraints to the DatePicker inside the dialog
        try {
            if (isFromDate) {
                // edtFromDate cannot be after edtToDate
                String toDateStr = edtToDate.getText().toString().trim();
                if (!toDateStr.isEmpty()) {
                    Date toDate = sdf.parse(toDateStr);
                    if (toDate != null) {
                        datePickerDialog.getDatePicker().setMaxDate(toDate.getTime());
                    }
                }
            } else {
                // edtToDate cannot be before edtFromDate
                String fromDateStr = edtFromDate.getText().toString().trim();
                if (!fromDateStr.isEmpty()) {
                    Date fromDate = sdf.parse(fromDateStr);
                    if (fromDate != null) {
                        datePickerDialog.getDatePicker().setMinDate(fromDate.getTime());
                    }
                }
            }
        } catch (ParseException ignored) {}

        datePickerDialog.show();
    }

    private void applyFilter() {
        String fromDateStr = edtFromDate.getText().toString().trim();
        String toDateStr = edtToDate.getText().toString().trim();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date fromDate = null;
        Date toDate = null;
        
        try {
            if (!fromDateStr.isEmpty()) fromDate = sdf.parse(fromDateStr);
        } catch (ParseException ignored) {}
        
        try {
            if (!toDateStr.isEmpty()) toDate = sdf.parse(toDateStr);
        } catch (ParseException ignored) {}

        List<StaffInvoiceItem> filteredList = new ArrayList<>();
        
        for (StaffInvoiceItem item : allInvoices) {
            // 1. Filter by status
            boolean matchesStatus = false;
            switch (currentFilterStatus) {
                case ALL:
                    matchesStatus = true;
                    break;
                case PAID:
                    matchesStatus = item.isPaid();
                    break;
                case UNPAID:
                    matchesStatus = !item.isPaid();
                    break;
            }
            
            if (!matchesStatus) {
                continue;
            }
            
            // 2. Filter by date
            String itemDateStr = item.getDate();
            if (itemDateStr != null && !itemDateStr.equals("--")) {
                try {
                    Date itemDate = sdf.parse(itemDateStr);
                    if (itemDate != null) {
                        if (fromDate != null && itemDate.before(fromDate)) {
                            continue;
                        }
                        if (toDate != null && itemDate.after(toDate)) {
                            continue;
                        }
                    }
                } catch (ParseException ignored) {}
            }
            
            filteredList.add(item);
        }
        
        updateInvoiceList(filteredList);
    }

    private void updateFilterButtonsVisuals() {
        int selectedBgColor = Color.parseColor("#0D3F6E");
        int selectedTextColor = Color.WHITE;
        
        int unselectedBgColor = Color.TRANSPARENT;
        int unselectedTextColor = Color.parseColor("#64748B");
        int unselectedStrokeColor = Color.parseColor("#7B92AD");
        
        updateButtonState(btnFilterAll, currentFilterStatus == FilterStatus.ALL, selectedBgColor, selectedTextColor, unselectedBgColor, unselectedTextColor, unselectedStrokeColor);
        updateButtonState(btnFilterPaid, currentFilterStatus == FilterStatus.PAID, selectedBgColor, selectedTextColor, unselectedBgColor, unselectedTextColor, unselectedStrokeColor);
        updateButtonState(btnFilterUnpaid, currentFilterStatus == FilterStatus.UNPAID, selectedBgColor, selectedTextColor, unselectedBgColor, unselectedTextColor, unselectedStrokeColor);
    }
    
    private void updateButtonState(com.google.android.material.button.MaterialButton button, boolean isSelected, 
                                   int selectedBg, int selectedText, int unselectedBg, int unselectedText, int unselectedStroke) {
        if (isSelected) {
            button.setBackgroundTintList(ColorStateList.valueOf(selectedBg));
            button.setTextColor(selectedText);
            button.setStrokeColor(ColorStateList.valueOf(selectedBg));
            if (button.getId() == R.id.btnFilterPaid) {
                button.setIconTint(ColorStateList.valueOf(Color.WHITE));
            } else if (button.getId() == R.id.btnFilterUnpaid) {
                button.setIconTint(ColorStateList.valueOf(Color.WHITE));
            }
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
            button.setTextColor(unselectedText);
            button.setStrokeColor(ColorStateList.valueOf(unselectedStroke));
            if (button.getId() == R.id.btnFilterPaid) {
                button.setIconTint(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            } else if (button.getId() == R.id.btnFilterUnpaid) {
                button.setIconTint(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            }
        }
    }
}
