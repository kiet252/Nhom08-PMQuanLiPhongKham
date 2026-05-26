package dashboard_fragment.staff_manage_bill;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

public class ManageBill_staff extends BaseActivity {
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (edtSearchPatient != null && edtSearchPatient.getText() != null) {
            String keyword = edtSearchPatient.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchInvoicesByPatient();
            } else {
                loadDefaultBillsForToday();
            }
        }
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

        String formattedDate = LocalDate.now().format(DISPLAY_DATE_FORMATTER);

        edtFromDate.setText(formattedDate);
        edtToDate.setText(formattedDate);

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
            loadDefaultBillsForToday();
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

    private void loadDefaultBillsForToday() {
        hideSearchError();
        edtSearchPatient.setError(null);

        LocalDate today = LocalDate.now();
        String todayDisplay = today.format(DISPLAY_DATE_FORMATTER);
        String todayIso = today.format(API_DATE_FORMATTER);

        edtFromDate.setText(todayDisplay);
        edtToDate.setText(todayDisplay);

        billRepository.getBillsByDateRange(todayIso, todayIso).enqueue(new Callback<List<ExamFormWithBillDto>>() {
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

                allInvoices.clear();
                allInvoices.addAll(BillMapper.fromExamForms(forms));
                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<List<ExamFormWithBillDto>> call, @NonNull Throwable t) {
                Toast.makeText(ManageBill_staff.this,
                        "Lá»—i táº£i hÃ³a Ä‘Æ¡n: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 23);
        todayCal.set(Calendar.MINUTE, 59);
        todayCal.set(Calendar.SECOND, 59);
        todayCal.set(Calendar.MILLISECOND, 999);
        Date today = todayCal.getTime();

        Date minDate = null;
        Date maxDate = today;

        try {
            if (isFromDate) {
                String toDateStr = edtToDate.getText() != null
                        ? edtToDate.getText().toString().trim()
                        : "";
                if (!toDateStr.isEmpty()) {
                    Date toDate = sdf.parse(toDateStr);
                    if (toDate != null && toDate.before(maxDate)) {
                        maxDate = toDate;
                    }
                }
            } else {
                String fromDateStr = edtFromDate.getText() != null
                        ? edtFromDate.getText().toString().trim()
                        : "";
                if (!fromDateStr.isEmpty()) {
                    minDate = sdf.parse(fromDateStr);
                }
            }
        } catch (ParseException ignored) {
        }

        String currentText = editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
        Date targetDate = null;

        if (!currentText.isEmpty()) {
            try {
                targetDate = sdf.parse(currentText);
            } catch (ParseException ignored) {
            }
        }

        if (targetDate == null) {
            if (!isFromDate && minDate != null) {
                targetDate = minDate;
            } else {
                targetDate = maxDate;
            }
        }

        if (minDate != null && targetDate.before(minDate)) {
            targetDate = minDate;
        }
        if (maxDate != null && targetDate.after(maxDate)) {
            targetDate = maxDate;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);

        AlertDialog dialog = new AlertDialog.Builder(this).create();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(22), dp(24), dp(18));
        layout.setBackground(createRoundedBackground("#FFFFFF", 24));

        DatePicker datePicker = new DatePicker(new ContextThemeWrapper(this, R.style.ReportDatePickerTheme));
        datePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );

        if (minDate != null) {
            datePicker.setMinDate(minDate.getTime());
        }
        if (maxDate != null) {
            datePicker.setMaxDate(maxDate.getTime());
        }

        layout.addView(datePicker);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setWeightSum(3);
        buttonRow.setPadding(0, dp(12), 0, 0);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        buttonParams.setMarginEnd(dp(8));

        LinearLayout.LayoutParams lastButtonParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );

        TextView btnCancel = createPickerButton("Hủy", "#64748B");
        btnCancel.setLayoutParams(buttonParams);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        TextView btnClear = createPickerButton("Xóa", "#F44336");
        btnClear.setLayoutParams(buttonParams);
        btnClear.setOnClickListener(v -> {
            editText.setText("");
            applyFilter();
            dialog.dismiss();
        });

        TextView btnApply = createPickerButton("Áp dụng", "#0D3F6E");
        btnApply.setLayoutParams(lastButtonParams);
        btnApply.setOnClickListener(v -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(
                    datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth(),
                    0,
                    0,
                    0
            );
            selectedCal.set(Calendar.MILLISECOND, 0);

            String selectedDateStr = sdf.format(selectedCal.getTime());

            if (isFromDate) {
                edtFromDate.setText(selectedDateStr);

                String toDateStr = edtToDate.getText() != null
                        ? edtToDate.getText().toString().trim()
                        : "";
                if (!toDateStr.isEmpty()) {
                    try {
                        Date currentToDate = sdf.parse(toDateStr);
                        if (currentToDate != null && currentToDate.before(selectedCal.getTime())) {
                            edtToDate.setText(selectedDateStr);
                        }
                    } catch (ParseException ignored) {
                    }
                }
            } else {
                edtToDate.setText(selectedDateStr);
            }

            applyFilter();
            dialog.dismiss();
        });

        buttonRow.addView(btnCancel);
        buttonRow.addView(btnClear);
        buttonRow.addView(btnApply);
        layout.addView(buttonRow);

        dialog.setView(layout);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    private TextView createPickerButton(String text, String backgroundColor) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        button.setBackground(createRoundedBackground(backgroundColor, 12));
        return button;
    }

    private GradientDrawable createRoundedBackground(String color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
    private void applyFilter() {
        String fromDateStr = edtFromDate.getText().toString().trim();
        String toDateStr = edtToDate.getText().toString().trim();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
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
