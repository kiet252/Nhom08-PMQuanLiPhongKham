package dashboard_fragment.staff_manage_bill;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

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
            updateInvoiceList(new ArrayList<>());
            return;
        }

        patientRepository.getProfileByIdOrCccd(keyword).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call,
                                   @NonNull Response<List<PatientProfile>> response) {
                if (!response.isSuccessful()) {
                    showSearchError();
                    showApiError(response);
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {
                    showSearchError();
                    updateInvoiceList(new ArrayList<>());
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
                    return;
                }

                List<ExamFormWithBillDto> forms = response.body() != null
                        ? response.body()
                        : new ArrayList<>();

                List<StaffInvoiceItem> items = BillMapper.fromExamForms(forms);

                if (items.isEmpty()) {
                    Toast.makeText(ManageBill_staff.this,
                            "Bệnh nhân chưa có hóa đơn nào", Toast.LENGTH_SHORT).show();
                    updateInvoiceList(new ArrayList<>());
                    return;
                }

                hideSearchError();
                fillMissingPatientNames(items, patient.getHo_ten());
                updateInvoiceList(items);
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
}
