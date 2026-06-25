package dashboard_fragment.staff_manage_bill;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private MaterialButton btnSaveBillDetail, btnPrintBillDetail;
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
        btnPrintBillDetail = findViewById(R.id.btnPrintBillDetail);

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

        btnPrintBillDetail.setOnClickListener(v -> {
            exportInvoiceToPDF();
        });
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
            btnPrintBillDetail.setVisibility(View.VISIBLE);
            setFieldEnabled(actvInvoiceStatus, false);
            setFieldEnabled(actvPaymentMethod, false);
        } else {
            btnSaveBillDetail.setVisibility(View.VISIBLE);
            btnPrintBillDetail.setVisibility(View.GONE);
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

    private void exportInvoiceToPDF() {
        if (examFormData == null) {
            Toast.makeText(this, "Không có dữ liệu để xuất hóa đơn!", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.BLACK);
        paint.setTextSize(12f);
        paint.setAntiAlias(true);

        Paint titlePaint = new Paint(paint);
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        Paint boldPaint = new Paint(paint);
        boldPaint.setFakeBoldText(true);

        Paint italicPaint = new Paint(paint);
        italicPaint.setTextSize(11f);
        italicPaint.setTextSkewX(-0.25f);

        try {
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.clinic_management_system);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 60, 60, true);
            canvas.drawBitmap(scaledLogo, 40, 25, null);
        } catch (Exception e) {
            Log.e("PDF_Export", "Không tìm thấy ảnh logo");
        }

        canvas.drawText("HÓA ĐƠN THANH TOÁN", 140, 50, titlePaint);
        paint.setTextSize(13f);
        canvas.drawText("Phòng khám TMH", 140, 75, paint);
        canvas.drawLine(40, 95, 555, 95, paint);

        int leftCol = 50;
        int rightCol = 320;
        int y = 130;

        paint.setTextSize(14f);
        canvas.drawText("THÔNG TIN BỆNH NHÂN", leftCol, y, boldPaint);
        canvas.drawLine(40, y + 8, 555, y + 8, paint);
        y += 35;
        paint.setTextSize(13f);

        String patientName = (examFormData.getPatient() != null) ? examFormData.getPatient().getHo_ten() : "N/A";
        canvas.drawText("Họ tên: " + patientName, leftCol, y, paint);
        canvas.drawText("Mã BN: " + (examFormData.getPatient() != null ? examFormData.getPatient().getId() : "--"), rightCol, y, paint);
        y += 25;
        canvas.drawText("SĐT: " + (examFormData.getPatient() != null ? examFormData.getPatient().getSo_dien_thoai() : "--"), leftCol, y, paint);
        canvas.drawText("Địa chỉ: " + (examFormData.getPatient() != null ? examFormData.getPatient().getDia_chi() : "--"), rightCol, y, paint);

        if (examFormData.getMedical_record() != null) {
            y += 40;
            canvas.drawText("THÔNG TIN DỊCH VỤ", leftCol, y, boldPaint);
            canvas.drawLine(40, y + 8, 555, y + 8, paint);

            y += 30;
            canvas.drawText("Tên dịch vụ", leftCol, y, boldPaint);
            canvas.drawText("SL", 340, y, boldPaint);
            canvas.drawText("Đơn giá", 400, y, boldPaint);
            canvas.drawText("Thành tiền", 490, y, boldPaint);

            y += 10;
            canvas.drawLine(40, y, 555, y, paint); // Đường gạch chân tiêu đề cột

            if (examFormData.getMedical_record().getMedical_record_clinical() != null &&
                    !examFormData.getMedical_record().getMedical_record_clinical().isEmpty()) {

                for (ExamFormWithBillDto.MedicalRecordClinicalDto service : examFormData.getMedical_record().getMedical_record_clinical()) {
                    if (service.getClinical() != null) {
                        y += 25;
                        double price = service.getClinical().getDon_gia() != null ? service.getClinical().getDon_gia() : 0.0;

                        canvas.drawText(service.getClinical().getTen_dich_vu(), leftCol, y, paint);
                        canvas.drawText("1", 345, y, paint);
                        canvas.drawText(String.format("%,.0f", price), 395, y, paint);
                        canvas.drawText(String.format("%,.0fđ", price), 485, y, paint);
                    }
                }
            } else {
                y += 25;
                canvas.drawText("Không sử dụng dịch vụ", leftCol, y, italicPaint);
            }


            y += 40;
            canvas.drawText("THÔNG TIN THUỐC ĐIỀU TRỊ", leftCol, y, boldPaint);
            canvas.drawLine(40, y + 8, 555, y + 8, paint);

            y += 30;
            canvas.drawText("Tên thuốc", leftCol, y, boldPaint);
            canvas.drawText("SL", 340, y, boldPaint);
            canvas.drawText("Đơn giá", 400, y, boldPaint);
            canvas.drawText("Thành tiền", 490, y, boldPaint);

            y += 10;
            canvas.drawLine(40, y, 555, y, paint);

            if (examFormData.getMedical_record().getMedical_record_medicine() != null &&
                    !examFormData.getMedical_record().getMedical_record_medicine().isEmpty()) {

                for (ExamFormWithBillDto.MedicalRecordMedicineDto med : examFormData.getMedical_record().getMedical_record_medicine()) {
                    if (med.getMedicine() != null) {
                        y += 25;
                        long qty = med.getSo_luong() != null ? med.getSo_luong() : 0;
                        double price = med.getMedicine().getDon_gia() != null ? med.getMedicine().getDon_gia() : 0.0;
                        double total = price * qty;

                        canvas.drawText(med.getMedicine().getTen_thuoc(), leftCol, y, paint);
                        canvas.drawText(String.valueOf(qty), 345, y, paint);
                        canvas.drawText(String.format("%,.0f", price), 395, y, paint);
                        canvas.drawText(String.format("%,.0fđ", total), 485, y, paint);
                    }
                }
            } else {
                y += 25;
                canvas.drawText("Không có thuốc điều trị", leftCol, y, italicPaint);
            }
        }

        y += 40;
        canvas.drawLine(40, y, 555, y, paint);
        y += 30;
        String statusStr = actvInvoiceStatus.getText().toString();
        String methodStr = actvPaymentMethod.getText().toString();

        canvas.drawText("Trạng thái: " + statusStr, leftCol, y, paint);
        canvas.drawText("TỔNG THANH TOÁN: ", 280, y, boldPaint);
        Paint redPaint = new Paint(boldPaint);
        redPaint.setColor(android.graphics.Color.RED);
        canvas.drawText(String.format("%,.0f VNĐ", computedGrandTotal), 435, y, redPaint);

        y += 25;
        paint.setTextSize(13f);
        canvas.drawText("Phương thức: " + methodStr, leftCol, y, paint);

        y += 40;

        italicPaint = new Paint(paint);
        italicPaint.setTextSize(11f);
        italicPaint.setLinearText(true);
        italicPaint.setSubpixelText(true);
        italicPaint.setTextSkewX(-0.25f);

        int signatureX = 350;

        canvas.drawText(
                "Chữ ký nhân viên",
                signatureX,
                y,
                boldPaint
        );

        y += 20;

        canvas.drawText(
                "(Ký và ghi rõ họ tên)",
                signatureX,
                y,
                italicPaint
        );

        document.finishPage(page);

        String cleanPatientName = removeVietnameseTones(patientName).replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = "HoaDon_" + selectedBillId + "_" + cleanPatientName + ".pdf";

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");

            Uri uri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/HoaDon"
                );

                uri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                );
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File pdfDir = new File(downloadsDir, "HoaDon");

                if (!pdfDir.exists()) {
                    pdfDir.mkdirs();
                }

                File file = new File(pdfDir, fileName);
                uri = Uri.fromFile(file);
            }

            if (uri == null) {
                throw new IOException("Không tạo được liên kết file PDF");
            }

            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            if (outputStream == null) {
                throw new IOException("Không mở được OutputStream");
            }

            document.writeTo(outputStream);

            outputStream.flush();
            outputStream.close();

            Toast.makeText(
                    this,
                    "Xuất hóa đơn thành công! Lưu tại Downloads/HoaDon",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            Log.e("PDF_EXPORT", "Export PDF failed", e);
            Toast.makeText(
                    this,
                    "Lỗi xuất file PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        } finally {
            document.close();
        }
    }
}
