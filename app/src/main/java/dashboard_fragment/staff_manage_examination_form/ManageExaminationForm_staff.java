package dashboard_fragment.staff_manage_examination_form;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;

public class ManageExaminationForm_staff extends BaseActivity {
    Button BtnSearch, BtnFilter, BtnSort;
    ImageButton BtnBack;
    EditText EdtSearch;
    RecyclerView RvExaminationsList;
    StaffExaminationFormGroupAdapter groupAdapter;
    ExaminationFormRepository repository;
    Boolean isAscending;
    private List<ExaminationFormWithPatientDto> AllForms = new ArrayList<>();
    private List<ExaminationFormWithPatientDto> ResultExaminationFormsAndPatients = new ArrayList<>();
    private String selectedDoctorId = null; // null = all
    Map<String, List<ExaminationFormWithPatientDto>> formsByDate = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_manage_examination_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_examination), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupListeners();
        setupRecycler();

        isAscending = true;
        repository = new ExaminationFormRepository(this);

        loadAllFormsAndPatiendDTO();
    }

    private void initializeViews() {
        BtnSearch = findViewById(R.id.btnManageExFormSearch);
        BtnFilter = findViewById(R.id.btnManageExFormFilterDoctor);
        BtnSort = findViewById(R.id.btnManageExFormSort);
        BtnBack = findViewById(R.id.btnManageExFormBack);

        EdtSearch = findViewById(R.id.edtManageExFormSearch);

        RvExaminationsList = findViewById(R.id.rvManageExFormExaminationsList);
    }

    private void setupListeners() {
        BtnSearch.setOnClickListener(v -> findExaminationFormsByPatientCCCDOrID());
        BtnSort.setOnClickListener(v -> showSortingCriteria());
        BtnFilter.setOnClickListener(v -> chooseFilter());
        BtnBack.setOnClickListener(v -> backToPreviousActivity());
    }

    private void setupRecycler() {
        RvExaminationsList.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new StaffExaminationFormGroupAdapter(this, new StaffExaminationFormGroupAdapter.OnExaminationFormActionListener() {
            @Override
            public void onFormLongClick(ExaminationFormWithPatientDto form) {
                showExaminationFormActionMenu(form);
            }
        });


        RvExaminationsList.setAdapter(groupAdapter);
    }

    private void findExaminationFormsByPatientCCCDOrID() {
        if (EdtSearch == null) return;

        String keyword = EdtSearch.getText().toString().trim();

        if (keyword.isEmpty()) {
            // Empty => show all current forms again
            selectedDoctorId = null;
            ResultExaminationFormsAndPatients = new ArrayList<>(AllForms);
            applySort();
            refreshExaminationFormsList();
            return;
        }

        //filter the patient with the searched patient_id or cccd
        boolean isNumeric = keyword.matches("^\\d+$");
        List<ExaminationFormWithPatientDto> filtered = new ArrayList<>();

        for (ExaminationFormWithPatientDto form : AllForms) {
            if (form == null || form.getPatient() == null) continue;

            String patientId = form.getPatient_id();
            String patientCccd = form.getPatient().getCccd();

            boolean matchId = isNumeric && patientId != null && patientId.equals(keyword);
            boolean matchCccd = patientCccd != null && patientCccd.equals(keyword);

            if (matchId || matchCccd) {
                filtered.add(form);
            }
        }

        if (filtered.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy bệnh nhân!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tìm thành công!", Toast.LENGTH_SHORT).show();
        }

        selectedDoctorId = null; // reset doctor filter when new search is applied
        ResultExaminationFormsAndPatients = filtered;
        applySort();
        refreshExaminationFormsList();
    }

    private void loadAllFormsAndPatiendDTO() {
        repository.getAllFormsWithPatient().enqueue(new retrofit2.Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull retrofit2.Response<List<ExaminationFormWithPatientDto>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        AllForms = new ArrayList<>(response.body());
                        selectedDoctorId = null;
                        ResultExaminationFormsAndPatients = new ArrayList<>(AllForms);

                        applySort();
                        refreshExaminationFormsList();
                    } else {
                        Toast.makeText(ManageExaminationForm_staff.this, "Không tìm thấy phiếu khám!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Toast.makeText(ManageExaminationForm_staff.this, "Lỗi ", Toast.LENGTH_SHORT).show();
                        Log.d("Error", "Lỗi: " + response.code() + (response.errorBody()).string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull Throwable t) {
                Toast.makeText(ManageExaminationForm_staff.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void applySort() {
        if (ResultExaminationFormsAndPatients == null || ResultExaminationFormsAndPatients.size() < 2) return;

        Comparator<ExaminationFormWithPatientDto> comparator = (a, b) -> {
            Date da = a.getNgay_kham();
            Date db = b.getNgay_kham();

            int dateCmp;
            if (da == null && db == null) dateCmp = 0;
            else if (da == null) dateCmp = 1;
            else if (db == null) dateCmp = -1;
            else dateCmp = da.compareTo(db);

            if (dateCmp != 0) return isAscending ? dateCmp : -dateCmp;

            String ta = a.getGio_du_kien() == null ? "" : a.getGio_du_kien();
            String tb = b.getGio_du_kien() == null ? "" : b.getGio_du_kien();
            int timeCmp = ta.compareTo(tb);

            return isAscending ? timeCmp : -timeCmp;
        };

        ResultExaminationFormsAndPatients.sort(comparator);
    }


    private void refreshExaminationFormsList() {
        formsByDate.clear();
        listExaminationFormsByDates();

        List<StaffExaminationFormDateGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<ExaminationFormWithPatientDto>> entry : formsByDate.entrySet()) {
            groups.add(new StaffExaminationFormDateGroup(entry.getKey(), entry.getValue()));
        }

        groupAdapter.submitList(groups);
    }

    private void listExaminationFormsByDates() {
        for (ExaminationFormWithPatientDto formPatientDto : ResultExaminationFormsAndPatients) {
            String key = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(formPatientDto.getNgay_kham());
            formsByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(formPatientDto);
        }
    }

    private void showSortingCriteria() {
        if (AllForms == null || AllForms.isEmpty()) {
            Toast.makeText(this, "Vui lòng tìm CCCD hoặc mã bệnh nhân trước khi chọn tiêu chí sắp xếp!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        PopupMenu popup = new PopupMenu(this, BtnSort); // anchor under button
        popup.getMenuInflater().inflate(R.menu.sort_examination_date_menu, popup.getMenu());

        MenuItem ascItem = popup.getMenu().findItem(R.id.action_sort_date_asc);
        MenuItem descItem = popup.getMenu().findItem(R.id.action_sort_date_desc);

        ascItem.setIcon(null);
        descItem.setIcon(null);

        if (Boolean.TRUE.equals(isAscending)) {
            ascItem.setIcon(R.drawable.ic_check_blue);
        } else {
            descItem.setIcon(R.drawable.ic_check_blue);
        }

        try {
            java.lang.reflect.Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            menuPopupHelper.getClass()
                    .getDeclaredMethod("setForceShowIcon", boolean.class)
                    .invoke(menuPopupHelper, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sort_date_asc) {
                isAscending = true;
                applySort();
                refreshExaminationFormsList();
                return true;
            } else if (id == R.id.action_sort_date_desc) {
                isAscending = false;
                applySort();
                refreshExaminationFormsList();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void chooseFilter() {
        if (AllForms == null || AllForms.isEmpty()) {
            Toast.makeText(this, "Vui lòng tìm CCCD hoặc mã bệnh nhân trước khi chọn bác sĩ!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.staff_filter_doctor_dialog, null, false);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        RadioGroup rgDoctorFilter = dialogView.findViewById(R.id.rgDoctorFilter);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelFilter);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilter);

        rgDoctorFilter.removeAllViews();

        RadioButton rbAll = new RadioButton(this);
        rbAll.setId(View.generateViewId());
        rbAll.setText("Tất cả");
        rbAll.setTag("ALL");
        rbAll.setChecked(selectedDoctorId == null);
        rgDoctorFilter.addView(rbAll);

        Map<String, String> doctorMap = new LinkedHashMap<>();
        for (ExaminationFormWithPatientDto form : AllForms) {
            if (form.getDoctor_id() == null || form.getDoctor() == null) continue;
            doctorMap.putIfAbsent(form.getDoctor_id(), form.getDoctor().getHo_ten());
        }

        for (Map.Entry<String, String> entry : doctorMap.entrySet()) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            ));
            rb.setPadding(0, 16, 0, 16);
            rb.setText(entry.getValue());
            rb.setTag(entry.getKey());
            rb.setChecked(entry.getKey().equals(selectedDoctorId));
            rgDoctorFilter.addView(rb);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            int checkedId = rgDoctorFilter.getCheckedRadioButtonId();
            if (checkedId == -1) return;

            RadioButton rbSelected = dialogView.findViewById(checkedId);
            selectedDoctorId = "ALL".equals(String.valueOf(rbSelected.getTag())) ? null : String.valueOf(rbSelected.getTag());

            applyDoctorFilterAndRefresh();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void applyDoctorFilterAndRefresh() {
        ResultExaminationFormsAndPatients.clear();

        if (selectedDoctorId == null) {
            ResultExaminationFormsAndPatients.addAll(AllForms);
        } else {
            for (ExaminationFormWithPatientDto f : AllForms) {
                if (selectedDoctorId.equals(f.getDoctor_id())) {
                    ResultExaminationFormsAndPatients.add(f);
                }
            }
        }

        applySort();
        refreshExaminationFormsList();
    }
    private void backToPreviousActivity() {
        finish();
    }
    private void showExaminationFormDetails(ExaminationFormWithPatientDto form) {
        View dialogView = getLayoutInflater().inflate(
                R.layout.staff_examination_detail_dialog,
                null,
                false
        );

        android.widget.TextView tvPatientName = dialogView.findViewById(R.id.tvDetailPatientName);
        android.widget.TextView tvBirthday = dialogView.findViewById(R.id.tvDetailBirthday);
        android.widget.TextView tvAddress = dialogView.findViewById(R.id.tvDetailAddress);
        android.widget.TextView tvExamDate = dialogView.findViewById(R.id.tvDetailExamDate);
        android.widget.TextView tvExamTime = dialogView.findViewById(R.id.tvDetailExamTime);
        android.widget.TextView tvSequence = dialogView.findViewById(R.id.tvDetailSequence);
        android.widget.TextView tvDoctor = dialogView.findViewById(R.id.tvDetailDoctor);
        android.widget.TextView tvStatus = dialogView.findViewById(R.id.tvDetailStatus);
        android.widget.TextView tvSymptoms = dialogView.findViewById(R.id.tvDetailSymptoms);
        com.google.android.material.button.MaterialButton btnClose =
                dialogView.findViewById(R.id.btnCloseDetail);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        var patient = form.getPatient();

        tvPatientName.setText("Họ tên: " + (patient != null ? patient.getHo_ten() : "--"));
        tvBirthday.setText("Ngày sinh: " + (patient != null && patient.getNgay_sinh() != null ? sdf.format(patient.getNgay_sinh()) : "--"));
        tvAddress.setText("Địa chỉ: " + (patient != null ? patient.getDia_chi() : "--"));

        tvExamDate.setText("Ngày khám: " + (form.getNgay_kham() != null ? sdf.format(form.getNgay_kham()) : "--"));
        tvExamTime.setText("Giờ dự kiến: " + (form.getGio_du_kien() != null ? form.getGio_du_kien() : "--"));
        tvSequence.setText("Số tiếp nhận: " + form.getSo_tiep_nhan());
        tvDoctor.setText("Bác sĩ: " + (form.getDoctor() != null ? form.getDoctor().getHo_ten() : "--"));
        tvStatus.setText("Trạng thái: " + (form.getTrang_thai() != null ? form.getTrang_thai() : "--"));
        tvSymptoms.setText(form.getTrieu_chung_ban_dau() != null ? form.getTrieu_chung_ban_dau() : "--");

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private void showExaminationFormActionMenu(ExaminationFormWithPatientDto form) {
        View dialogView = getLayoutInflater().inflate(
                R.layout.staff_examination_form_action_dialog,
                null,
                false
        );

        View layoutActionDetail = dialogView.findViewById(R.id.layoutActionDetail);
        View layoutActionExport = dialogView.findViewById(R.id.layoutActionExport);
        View layoutActionCancel = dialogView.findViewById(R.id.layoutActionCancel);

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .create();

        layoutActionDetail.setOnClickListener(v -> {
            dialog.dismiss();
            showExaminationFormDetails(form);
        });

        layoutActionExport.setOnClickListener(v -> {
            exportExaminationFormToPdf(form);
            dialog.dismiss();
        });

        layoutActionCancel.setOnClickListener(v -> {
            dialog.dismiss();
            confirmCancelExaminationForm(form);
        });

        dialog.show();
    }
    private void confirmCancelExaminationForm(ExaminationFormWithPatientDto form) {
        if (form == null || form.getId() == null) return;

        if (!"Chờ khám".equalsIgnoreCase(form.getTrang_thai())) {
            Toast.makeText(this, "Chỉ có thể hủy phiếu ở trạng thái Chờ khám", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(
                R.layout.user_cancel_examination_form_dialog,
                null,
                false
        );

        com.google.android.material.button.MaterialButton btnClose =
                dialogView.findViewById(R.id.btnCloseCancelDialog);
        com.google.android.material.button.MaterialButton btnConfirm =
                dialogView.findViewById(R.id.btnConfirmCancelForm);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            cancelExaminationForm(form);
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

    }
    private void cancelExaminationForm(ExaminationFormWithPatientDto form) {
        repository.cancelForm(form.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageExaminationForm_staff.this, "Hủy phiếu thành công", Toast.LENGTH_SHORT).show();

                    AllForms.removeIf(f -> form.getId().equals(f.getId()));
                    ResultExaminationFormsAndPatients.removeIf(f -> form.getId().equals(f.getId()));

                    refreshExaminationFormsList();
                } else {
                    try {
                        String errorText = response.errorBody() != null ? response.errorBody().string() : "Không có nội dung lỗi";
                        Toast.makeText(ManageExaminationForm_staff.this, "Lỗi: Không thể hủy phiếu", Toast.LENGTH_SHORT).show();
                        Log.e("CANCEL_EX_FORM", "Code: " + response.code() + " - " + errorText);
                    } catch (Exception e) {
                        Log.e("CANCEL_EX_FORM", "Code: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ManageExaminationForm_staff.this, "Lỗi kết nối khi hủy phiếu", Toast.LENGTH_SHORT).show();
                Log.e("CANCEL_EX_FORM", "Code: " + t.getMessage());
            }
        });
    }
    private void exportExaminationFormToPdf(ExaminationFormWithPatientDto form) {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Bitmap logo = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.clinic_management_system
        );
        Bitmap scaledLogo = Bitmap.createScaledBitmap(
                logo,
                60,
                60,
                true
        );
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        paint.setTextSize(14f);

        int leftCol = 50;
        int rightCol = 320;
        int y = 120;

        canvas.drawBitmap(
                scaledLogo,
                40,
                20,
                null
        );

        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        canvas.drawText(
                "PHIẾU KHÁM BỆNH",
                130,
                55,
                titlePaint
        );

        paint.setTextSize(12f);

        canvas.drawText(
                "Phòng khám TMH",
                130,
                80,
                paint
        );

        canvas.drawLine(40, 90, 555, 90, paint);

        canvas.drawText("Mã phiếu: " + form.getId(), leftCol, y, paint);
        canvas.drawText("Số tiếp nhận: " + form.getSo_tiep_nhan(), rightCol, y, paint);

        y += 30;

        canvas.drawText(
                "Họ tên: " +
                        (form.getPatient() != null
                                ? form.getPatient().getHo_ten()
                                : "N/A"),
                leftCol,
                y,
                paint
        );

        canvas.drawText(
                "Giới tính: " +
                        (form.getPatient() != null
                                ? form.getPatient().getGioi_tinh()
                                : "N/A"),
                rightCol,
                y,
                paint
        );

        y += 30;

        canvas.drawText(
                "Ngày khám: " +
                        (form.getNgay_kham() != null
                                ? sdf.format(form.getNgay_kham())
                                : "N/A"),
                leftCol,
                y,
                paint
        );

        canvas.drawText(
                "Giờ khám: " + form.getGio_du_kien(),
                rightCol,
                y,
                paint
        );

        y += 30;

        canvas.drawText(
                "Bác sĩ: " +
                        (form.getDoctor() != null
                                ? form.getDoctor().getHo_ten()
                                : "N/A"),
                leftCol,
                y,
                paint
        );

        canvas.drawText(
                "Phí khám: " +
                        String.format("%,d VNĐ", form.getPhi_kham()),
                rightCol,
                y,
                paint
        );

        y += 30;

        canvas.drawText(
                "Trạng thái: " + form.getTrang_thai(),
                leftCol,
                y,
                paint
        );

        y += 20;

        canvas.drawLine(40, y, 555, y, paint);

        y += 35;

        Paint boldPaint = new Paint(paint);
        boldPaint.setFakeBoldText(true);

        canvas.drawText(
                "TRIỆU CHỨNG BAN ĐẦU",
                leftCol,
                y,
                boldPaint
        );

        y += 30;

        canvas.drawText(
                form.getTrieu_chung_ban_dau() != null
                        ? form.getTrieu_chung_ban_dau()
                        : "Không có",
                leftCol,
                y,
                paint
        );
        document.finishPage(page);

        String patientName =
                form.getPatient() != null
                        ? form.getPatient().getHo_ten().replaceAll("[^a-zA-Z0-9]", "_")
                        : "Unknown";

        String date =
                form.getNgay_kham() != null
                        ? new SimpleDateFormat("ddMMyyyy", Locale.getDefault())
                        .format(form.getNgay_kham())
                        : "UnknownDate";

        String fileName =
                "PhieuKham_" + form.getId()
                        + "_" + patientName
                        + "_" + date + ".pdf";

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");

            Uri uri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/PhieuKham"
                );

                uri = getContentResolver().insert(
                        MediaStore.Files.getContentUri("external"),
                        values
                );

            } else {

                File downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                        );

                File pdfDir = new File(downloadsDir, "PhieuKham");

                if (!pdfDir.exists()) {
                    pdfDir.mkdirs();
                }

                File file = new File(pdfDir, fileName);

                uri = Uri.fromFile(file);
            }

            if (uri == null) {
                throw new IOException("Không tạo được file PDF");
            }

            OutputStream outputStream =
                    getContentResolver().openOutputStream(uri);

            if (outputStream == null) {
                throw new IOException("Không mở được OutputStream");
            }

            document.writeTo(outputStream);

            outputStream.flush();
            outputStream.close();

            Toast.makeText(
                    this,
                    "Đã xuất PDF vào Downloads/PhieuKham",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Log.e("PDF_EXPORT", "Export PDF failed", e);

            Toast.makeText(
                    this,
                    "Lỗi xuất PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }

        document.close();
    }
}