package dashboard_fragment.staff_manage_bill;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;

import android.net.Uri;

import java.text.Normalizer;

import coil.Coil;
import coil.request.ImageRequest;

import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;

public class BillDetail_staff extends BaseActivity {
    private ImageView btnBack;
    private TextView tvDetailPatientName, tvDetailPatientID, tvDetailPhone, tvDetailAddress;
    private TextView tvGrandTotal, tvTransferContent;
    private AutoCompleteTextView actvInvoiceStatus, actvPaymentMethod;
    private LinearLayout layoutQRCode;
    private MaterialButton btnSaveBillDetail;
    private LinearLayout containerServiceRows;
    private LinearLayout containerMedicineRows;
    private ImageView imgQRCode;
    private static final String QR_BANK_ID = "970436";
    private static final String QR_ACCOUNT_NO = "1015392878";
    private ExamFormWithBillDto examFormData;
    private long selectedBillId;
    private double computedGrandTotal = 0.0;
    private boolean isPaidLocked = false;
    private BillRepository billRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_bill_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_bill_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupDropdownMenus();
        setupListeners();
        unpackIntentData();
        billRepository = new BillRepository(this);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnStaffBillBack);
        btnSaveBillDetail = findViewById(R.id.btnSaveBillDetail);

        tvDetailPatientName = findViewById(R.id.tvDetailPatientName);
        tvDetailPatientID = findViewById(R.id.tvDetailPatientID);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);

        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        tvTransferContent = findViewById(R.id.tvTransferContent);
        actvInvoiceStatus = findViewById(R.id.actvInvoiceStatus);
        actvPaymentMethod = findViewById(R.id.actvPaymentMethod);
        layoutQRCode = findViewById(R.id.layoutQRCode);

        containerServiceRows = findViewById(R.id.containerServiceRows);
        containerMedicineRows = findViewById(R.id.containerMedicineRows);

        imgQRCode = findViewById(R.id.imgQRCode);
    }

    private void setupDropdownMenus() {
        String[] statuses = {"Chưa thanh toán", "Đã thanh toán"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        actvInvoiceStatus.setAdapter(statusAdapter);

        String[] methods = {"Tiền mặt", "Chuyển khoản"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, methods);
        actvPaymentMethod.setAdapter(methodAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        actvInvoiceStatus.setOnItemClickListener((parent, view, position, id) -> {
            updateEditableState();
        });

        actvPaymentMethod.setOnItemClickListener((parent, view, position, id) -> {
            updateEditableState();
        });

        btnSaveBillDetail.setOnClickListener(v -> saveInvoiceChanges());
    }

    private void unpackIntentData() {
        examFormData = (ExamFormWithBillDto) getIntent().getSerializableExtra("EXTRA_EXAM_FORM_DATA");
        String billIdStr = getIntent().getStringExtra("EXTRA_SELECTED_BILL_ID");

        if (examFormData == null || billIdStr == null) {
            Toast.makeText(this, "Không thể tải thông tin chi tiết hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            selectedBillId = Long.parseLong(billIdStr);
            bindDataToViews();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Mã hóa đơn không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindDataToViews() {
        if (examFormData.getPatient() != null) {
            ExamFormWithBillDto.PatientWrapper patient = examFormData.getPatient();
            tvDetailPatientName.setText(patient.getHo_ten());
            tvDetailPatientID.setText(patient.getId() != null ? String.valueOf(patient.getId()) : "--");
            tvDetailPhone.setText(patient.getSo_dien_thoai() != null ? patient.getSo_dien_thoai() : "--");
            tvDetailAddress.setText(patient.getDia_chi() != null ? patient.getDia_chi() : "--");
        }

        populateTablesData();

        computedGrandTotal = BillRowBinder.calculateGrandTotal(examFormData);
        tvGrandTotal.setText(String.format("%,.0fđ", computedGrandTotal));

        if (examFormData.getPatient() != null) {
            updateTransferContent();
        }

        //Exit early if any required data is missing or doesn't match
        if (examFormData.getMedical_record() == null || examFormData.getMedical_record().getBill() == null) {
            updateEditableState();
            return;
        }

        ExamFormWithBillDto.BillSummaryDto bill = examFormData.getMedical_record().getBill();
        if (bill.getId() == null || bill.getId() != selectedBillId) {
            updateEditableState();
            return;
        }

        //Format Status & Method (Capitalize first letter cleanly)
        String status = capitalizeText(bill.getTrang_thai_thanh_toan());
        String method = capitalizeText(bill.getPhuong_thuc_thanh_toan());

        //Update UI and State
        actvInvoiceStatus.setText(status, false);
        actvPaymentMethod.setText(method, false);

        isPaidLocked = "Đã thanh toán".equalsIgnoreCase(status);
        updateEditableState();
    }

    private String capitalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void populateTablesData() {
        if (examFormData == null || examFormData.getMedical_record() == null) {
            containerServiceRows.removeAllViews();
            containerMedicineRows.removeAllViews();
            return;
        }

        BillRowBinder.bindServices(containerServiceRows, examFormData.getMedical_record().getMedical_record_clinical());
        BillRowBinder.bindMedicines(containerMedicineRows, examFormData.getMedical_record().getMedical_record_medicine());
    }

    private boolean isPaidStatus(String status) {
        return "Đã thanh toán".equalsIgnoreCase(status != null ? status.trim() : "");
    }

    private boolean isTransferMethod(String method) {
        return "Chuyển khoản".equalsIgnoreCase(method != null ? method.trim() : "");
    }
    private void setFieldEnabled(AutoCompleteTextView view, boolean enabled) {
        view.setEnabled(enabled);
        view.setFocusable(enabled);
        view.setFocusableInTouchMode(enabled);
        view.setClickable(enabled);
        view.setLongClickable(enabled);
        view.setCursorVisible(enabled);
        view.setAlpha(enabled ? 1f : 0.5f);
    }

    private void updateEditableState() {
        String currentStatus = actvInvoiceStatus.getText() != null
                ? actvInvoiceStatus.getText().toString().trim()
                : "";
        String currentMethod = actvPaymentMethod.getText() != null
                ? actvPaymentMethod.getText().toString().trim()
                : "";

        boolean isPaid = isPaidStatus(currentStatus);
        boolean shouldShowQrCode = !isPaid && isTransferMethod(currentMethod);

        if (isPaidLocked) {
            btnSaveBillDetail.setVisibility(View.GONE);
            setFieldEnabled(actvInvoiceStatus, false);
            setFieldEnabled(actvPaymentMethod, false);
        } else {
            btnSaveBillDetail.setVisibility(View.VISIBLE);
            btnSaveBillDetail.setEnabled(true);
            setFieldEnabled(actvInvoiceStatus, true);
            setFieldEnabled(actvPaymentMethod, !isPaid);
        }

        layoutQRCode.setVisibility(shouldShowQrCode ? View.VISIBLE : View.GONE);

        if (shouldShowQrCode) {
            loadVietQrForBill();
        }
    }


    private void loadVietQrForBill() {
        String transferContent = buildTransferContentForQr();
        tvTransferContent.setText("Nội dung: " + transferContent);

        String qrUrl = buildVietQrImageUrl(transferContent, Math.round(computedGrandTotal));

        imgQRCode.setImageDrawable(null);

        ImageRequest request = new ImageRequest.Builder(this)
                .data(qrUrl)
                .target(imgQRCode)
                .crossfade(true)
                .build();

        Coil.imageLoader(this).enqueue(request);
    }

    private String buildTransferContentForQr() {
        if (examFormData == null || examFormData.getPatient() == null) {
            return "[MaBN] [HoTen]";
        }

        ExamFormWithBillDto.PatientWrapper patient = examFormData.getPatient();
        String patientId = patient.getId() != null ? String.valueOf(patient.getId()) : "";
        String patientName = removeVietnameseTones(patient.getHo_ten());

        patientName = patientName == null ? "" : patientName.replaceAll("\\s+", " ").trim();

        if (patientId.isEmpty() || patientName.isEmpty()) {
            return "[MaBN] [HoTen]";
        }

        return patientId + " " + patientName;
    }

    private String buildVietQrImageUrl(String transferContent, long amount) {
        return "https://img.vietqr.io/image/"
                + QR_BANK_ID
                + "-"
                + QR_ACCOUNT_NO
                + "-compact2.png?amount="
                + amount
                + "&addInfo="
                + Uri.encode(transferContent);
    }

    private String removeVietnameseTones(String input) {
        if (input == null) return "";

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace('đ', 'd').replace('Đ', 'D');

        return normalized;
    }
    private void updateTransferContent() {
        tvTransferContent.setText("Nội dung: " + buildTransferContentForQr());
    }
    private void saveInvoiceChanges() {
        String finalStatus = actvInvoiceStatus.getText() != null ? actvInvoiceStatus.getText().toString().trim() : "";
        String finalMethod = actvPaymentMethod.getText() != null ? actvPaymentMethod.getText().toString().trim() : "";

        if (isPaidLocked) {
            Toast.makeText(this, "Hóa đơn đã thanh toán, không thể chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (finalStatus.isEmpty()) {
            actvInvoiceStatus.setError("Vui lòng chọn trạng thái");
            actvInvoiceStatus.requestFocus();
            return;
        }

        if (!isPaidStatus(finalStatus)) {
            if (finalMethod.isEmpty()) {
                actvPaymentMethod.setError("Vui lòng chọn phương thức thanh toán");
                actvPaymentMethod.requestFocus();
                return;
            }
        } else {
            // 2. If it IS a paid status, safely extract the text
            CharSequence text = actvPaymentMethod.getText();
            finalMethod = (text != null) ? text.toString().trim() : "";
        }

        Toast.makeText(this, "Đang xử lý cập nhật...", Toast.LENGTH_SHORT).show();

        billRepository.updateBill(selectedBillId, finalMethod, finalStatus, computedGrandTotal).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BillDetail_staff.this, "Cập nhật hóa đơn " + String.format("%,.0fđ", computedGrandTotal) + " thành công!", Toast.LENGTH_SHORT).show();

                            if (isPaidStatus(finalStatus)) {
                                isPaidLocked = true;
                            }

                            updateEditableState();
                            finish();
                        } else {
                            Toast.makeText(BillDetail_staff.this, "Lỗi hệ thống: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        Toast.makeText(BillDetail_staff.this, "Lỗi kết nối", Toast.LENGTH_LONG).show();
                        Log.d("Error", "Lỗi kết nối: " + t.getMessage());
                    }
                });
    }
}
