package dashboard_fragment.staff_create_examination_form;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
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

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dashboard_fragment.doctor_create_appointment.AppointmentRepository;
import dashboard_fragment.doctor_create_appointment.create_appointment_logic.AppointmentItem;
import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import dashboard_fragment.staff_create_examination_form.create_ex_form_logic.CreateExFormRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.net.Uri;

import java.text.Normalizer;

import coil.Coil;
import coil.request.ImageRequest;

public class CreateExaminationForm_staff extends BaseActivity {
    private ImageButton BtnBackCreateSlip;
    private MaterialButton BtnSearchPatient, BtnCreateSlip, BtnDeleteSlip;
    private TextView TvFullName, TvBirthday, TvAddress, TvSTN, TvTransferContent;
    private TextView TvLatestAppointmentEmpty, TvLatestAppointmentPatientName, TvLatestAppointmentDate, TvLatestAppointmentDoctor;
    private TextInputEditText EdtSearchPatient, EdtDateExam, EdtTimeExam, EdtSymptoms, EdtFee, EdtStatus;
    private AutoCompleteTextView ActvDoctor, ActvPaymentMethod;
    private LinearLayout LayoutQRCode;
    private MaterialCardView CardLatestAppointmentItem;
    private ImageView ImgLatestAppointmentCheck;
    private TextView TvLatestAppointmentNote;
    private ImageView ImgQRCode;

    private static final String QR_BANK_ID = "970436"; // Vietcombank
    private static final String QR_ACCOUNT_NO = "1015392878";
    private static final int QR_AMOUNT = 100000;

    private ExaminationFormRepository ExFormRepository;
    private PatientRepository PaRepository;
    private AppointmentRepository AppointmentRepository;
    private PatientProfile foundPatient;
    private ProfileRepository profileRepository;
    private List<UserProfile> doctorList = new ArrayList<>();
    private String selectedDoctorID;

    private AppointmentItem latestAvailableAppointment;
    private AppointmentItem selectedAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_create_examination_form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_examination), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ExFormRepository = new ExaminationFormRepository(this);
        PaRepository = new PatientRepository(this);
        AppointmentRepository = new AppointmentRepository(this);
        profileRepository = new ProfileRepository(this);

        initializeViews();
        initializeValuesForPaymentMethod();
        setupListeners();
        loadDoctors();

        TvSTN.setText("Số tiếp nhận: --");
        resetLatestAppointmentCard();
    }

    private void initializeViews() {
        BtnBackCreateSlip = findViewById(R.id.btnBackCreateSlip);
        BtnSearchPatient = findViewById(R.id.btnCreateExaminationFormSearchPatient);
        BtnCreateSlip = findViewById(R.id.btnCreateExaminationFormCreateSlip);
        BtnDeleteSlip = findViewById(R.id.btnCreateExaminationFormDeleteCurrentSlip);

        TvFullName = findViewById(R.id.tvCreateExaminationFormFullName);
        TvBirthday = findViewById(R.id.tvCreateExaminationFormBirthday);
        TvAddress = findViewById(R.id.tvCreateExaminationFormAddress);
        TvSTN = findViewById(R.id.tvCreateExaminationFormSTN);
        TvTransferContent = findViewById(R.id.tvTransferContent);

        TvLatestAppointmentEmpty = findViewById(R.id.tvLatestAppointmentEmpty);
        TvLatestAppointmentPatientName = findViewById(R.id.tvLatestAppointmentPatientName);
        TvLatestAppointmentDate = findViewById(R.id.tvLatestAppointmentDate);
        TvLatestAppointmentDoctor = findViewById(R.id.tvLatestAppointmentDoctor);
        TvLatestAppointmentNote = findViewById(R.id.tvLatestAppointmentNote);
        CardLatestAppointmentItem = findViewById(R.id.cardLatestAppointmentItem);
        ImgLatestAppointmentCheck = findViewById(R.id.imgLatestAppointmentCheck);

        EdtSearchPatient = findViewById(R.id.edtCreateExaminationFormSearchPatient);
        EdtDateExam = findViewById(R.id.edtCreateExaminationFormDateExam);
        EdtTimeExam = findViewById(R.id.edtCreateExaminationFormTimeExam);
        EdtSymptoms = findViewById(R.id.edtCreateExaminationFormSymptoms);
        EdtFee = findViewById(R.id.edtCreateExaminationFormFee);
        EdtStatus = findViewById(R.id.edtCreateExaminationFormStatus);

        ActvDoctor = findViewById(R.id.actvCreateExaminationFormDoctor);
        ActvPaymentMethod = findViewById(R.id.actvCreateExaminationFormPaymentMethod);

        LayoutQRCode = findViewById(R.id.layoutQRCode);
        ImgQRCode = findViewById(R.id.imgQRCode);
    }

    private void setupListeners() {
        BtnBackCreateSlip.setOnClickListener(v -> finish());
        BtnCreateSlip.setOnClickListener(v -> createSlip());
        BtnDeleteSlip.setOnClickListener(v -> resetForm());
        BtnSearchPatient.setOnClickListener(v -> sendPatientSearchRequest());

        EdtDateExam.setOnClickListener(v -> showDatePicker());
        EdtTimeExam.setOnClickListener(v -> showTimePicker());

        ActvPaymentMethod.setOnClickListener(v -> ActvPaymentMethod.showDropDown());
        ActvPaymentMethod.setOnItemClickListener((parent, view, position, id) -> {
            updateVietQrSection();
        });

        CardLatestAppointmentItem.setOnClickListener(v -> toggleAppointmentSelection());
    }

    private void loadDoctors() {
        profileRepository.getDoctors().enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfile>> call, @NonNull Response<List<UserProfile>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CreateExaminationForm_staff.this, "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body() == null) return;
                doctorList = response.body();

                List<String> doctorNames = new ArrayList<>();
                for (UserProfile doctor : doctorList) {
                    doctorNames.add(removeDoctorPrefix(doctor.getHo_ten()));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CreateExaminationForm_staff.this,
                        android.R.layout.simple_dropdown_item_1line,
                        doctorNames
                );

                ActvDoctor.setAdapter(adapter);
                ActvDoctor.setInputType(0);
                ActvDoctor.setKeyListener(null);

                ActvDoctor.setOnClickListener(v -> ActvDoctor.showDropDown());
                ActvDoctor.setOnItemClickListener((parent, view, position, id) -> {
                    UserProfile selectedDoctor = doctorList.get(position);
                    selectedDoctorID = selectedDoctor.getID();
                    ActvDoctor.setError(null);

                    if (selectedAppointment != null &&
                            !selectedDoctorID.equals(selectedAppointment.getDoctor_id())) {
                        clearSelectedAppointmentState();
                    }
                });

                refreshAppointmentDoctorTexts();
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfile>> call, @NonNull Throwable t) {
                Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void initializeValuesForPaymentMethod() {
        String[] paymentMethods = {"Tiền mặt", "Chuyển khoản"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                paymentMethods
        );

        ActvPaymentMethod.setAdapter(adapter);
        ActvPaymentMethod.setText("Tiền mặt", false);
        LayoutQRCode.setVisibility(View.GONE);
        updateVietQrSection();
    }

    private void sendPatientSearchRequest() {
        String searchValue = EdtSearchPatient.getText() != null
                ? EdtSearchPatient.getText().toString().trim()
                : "";

        if (searchValue.isEmpty()) {
            EdtSearchPatient.setError("Vui lòng nhập id hoặc cccd cần tìm!");
            EdtSearchPatient.requestFocus();
            return;
        }

        PaRepository.getProfileByIdOrCccd(searchValue).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call, @NonNull Response<List<PatientProfile>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CreateExaminationForm_staff.this, "Lỗi tìm bệnh nhân", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body() != null && !response.body().isEmpty()) {
                    foundPatient = response.body().get(0);
                    modifyCurrentPatientViewsValues();
                    loadLatestAvailableAppointment();
                    Toast.makeText(CreateExaminationForm_staff.this, "Tìm thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    foundPatient = null;
                    clearPatientInfo();
                    resetLatestAppointmentCard();
                    Toast.makeText(CreateExaminationForm_staff.this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PatientProfile>> call, @NonNull Throwable t) {
                Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadLatestAvailableAppointment() {
        if (foundPatient == null) {
            resetLatestAppointmentCard();
            return;
        }

        ExFormRepository.getUsedAppointmentsByPatientId(foundPatient.getId()).enqueue(new Callback<List<ExaminationForm>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationForm>> call, @NonNull Response<List<ExaminationForm>> response) {
                Set<Long> usedAppointmentIds = new HashSet<>();

                if (response.isSuccessful() && response.body() != null) {
                    for (ExaminationForm form : response.body()) {
                        if (form.getAppointment_id() != null) {
                            usedAppointmentIds.add(form.getAppointment_id());
                        }
                    }
                }

                fetchLatestAvailableAppointment(usedAppointmentIds);
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationForm>> call, @NonNull Throwable t) {
                fetchLatestAvailableAppointment(new HashSet<>());
            }
        });
    }

    private void fetchLatestAvailableAppointment(Set<Long> usedAppointmentIds) {
        AppointmentRepository.getAppointmentsByPatientId(foundPatient.getId()).enqueue(new Callback<List<AppointmentItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<AppointmentItem>> call, @NonNull Response<List<AppointmentItem>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    resetLatestAppointmentCard();
                    return;
                }

                AppointmentItem latestAppointment = response.body().get(0);

                if (latestAppointment.getId() == null) {
                    resetLatestAppointmentCard();
                    return;
                }

                if (usedAppointmentIds.contains(latestAppointment.getId())) {
                    resetLatestAppointmentCard();
                    return;
                }

                bindLatestAppointment(latestAppointment);
            }

            @Override
            public void onFailure(@NonNull Call<List<AppointmentItem>> call, @NonNull Throwable t) {
                resetLatestAppointmentCard();
            }
        });
    }

    private void bindLatestAppointment(AppointmentItem appointmentItem) {
        latestAvailableAppointment = appointmentItem;
        selectedAppointment = null;

        TvLatestAppointmentEmpty.setVisibility(View.GONE);
        CardLatestAppointmentItem.setVisibility(View.VISIBLE);

        TvLatestAppointmentPatientName.setText("Họ và tên: " + safeText(foundPatient != null ? foundPatient.getHo_ten() : null));
        TvLatestAppointmentDate.setText("Ngày hẹn: " + formatApiDateToDisplay(appointmentItem.getNgay_hen()));
        TvLatestAppointmentDoctor.setText("Bác sĩ: " + safeText(findDoctorNameById(appointmentItem.getDoctor_id())));
        TvLatestAppointmentNote.setText("Ghi chú: " + safeText(appointmentItem.getGhi_chu()));

        setAppointmentCardSelected(false);
    }

    private void resetLatestAppointmentCard() {
        latestAvailableAppointment = null;
        selectedAppointment = null;

        TvLatestAppointmentEmpty.setVisibility(View.VISIBLE);
        CardLatestAppointmentItem.setVisibility(View.GONE);
        setAppointmentCardSelected(false);

        TvLatestAppointmentPatientName.setText("Họ và tên: --");
        TvLatestAppointmentDate.setText("Ngày hẹn: --");
        TvLatestAppointmentDoctor.setText("Bác sĩ: --");
        TvLatestAppointmentNote.setText("Ghi chú: --");
    }

    private void toggleAppointmentSelection() {
        if (latestAvailableAppointment == null) return;

        if (selectedAppointment != null &&
                selectedAppointment.getId() != null &&
                selectedAppointment.getId().equals(latestAvailableAppointment.getId())) {
            clearSelectedAppointmentState();
            return;
        }

        selectedAppointment = latestAvailableAppointment;
        setAppointmentCardSelected(true);
        fillFormFromAppointment(selectedAppointment);
    }

    private void clearSelectedAppointmentState() {
        selectedAppointment = null;
        setAppointmentCardSelected(false);
    }

    private void setAppointmentCardSelected(boolean selected) {
        ImgLatestAppointmentCheck.setImageResource(
                selected ? R.drawable.bg_appointment_circle_checked
                        : R.drawable.bg_clinical_circle_unchecked
        );

        CardLatestAppointmentItem.setStrokeWidth(selected ? dp(2) : dp(1));
        CardLatestAppointmentItem.setStrokeColor(Color.parseColor(selected ? "#0D3F6E" : "#CBD5E1"));
        CardLatestAppointmentItem.setCardBackgroundColor(Color.parseColor(selected ? "#EFF6FF" : "#F8FAFC"));
    }

    private void fillFormFromAppointment(AppointmentItem appointmentItem) {
        if (shouldCopyAppointmentDate(appointmentItem)) {
            String displayDate = formatApiDateToDisplay(appointmentItem.getNgay_hen());
            EdtDateExam.setText(displayDate);
        }

        updatePredictedReceptionNumber();

        selectedDoctorID = appointmentItem.getDoctor_id();
        ActvDoctor.setText(removeDoctorPrefix(findDoctorNameById(selectedDoctorID)), false);
        ActvDoctor.setError(null);
    }

    private void refreshAppointmentDoctorTexts() {
        if (latestAvailableAppointment != null) {
            TvLatestAppointmentDoctor.setText("Bác sĩ: " + safeText(findDoctorNameById(latestAvailableAppointment.getDoctor_id())));
        }

        if (selectedAppointment != null && selectedDoctorID != null) {
            ActvDoctor.setText(removeDoctorPrefix(findDoctorNameById(selectedDoctorID)), false);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        AlertDialog dialog = new AlertDialog.Builder(this).create();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(22), dp(24), dp(18));
        layout.setBackground(createRoundedBackground("#FFFFFF", 24));

        DatePicker datePicker = new DatePicker(new ContextThemeWrapper(this, R.style.ReportDatePickerTheme));
        datePicker.init(year, month, day, null);

        long today = System.currentTimeMillis();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 30);

        datePicker.setMinDate(today);
        datePicker.setMaxDate(maxDate.getTimeInMillis());

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
            EdtDateExam.setText("");
            EdtDateExam.setError(null);
            TvSTN.setText("Số tiếp nhận: --");

            if (selectedAppointment != null) {
                clearSelectedAppointmentState();
            }

            dialog.dismiss();
        });

        TextView btnApply = createPickerButton("Áp dụng", "#0D3F6E");
        btnApply.setLayoutParams(lastButtonParams);
        btnApply.setOnClickListener(v -> {
            int selectedYear = datePicker.getYear();
            int selectedMonth = datePicker.getMonth();
            int selectedDay = datePicker.getDayOfMonth();

            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            EdtDateExam.setText(date);
            EdtDateExam.setTextColor(Color.parseColor("#1E293B"));
            updatePredictedReceptionNumber();

            if (selectedAppointment != null && shouldCopyAppointmentDate(selectedAppointment)) {
                String appointmentDate = formatApiDateToDisplay(selectedAppointment.getNgay_hen());
                if (!appointmentDate.equals(date)) {
                    clearSelectedAppointmentState();
                }
            }

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


    private void showTimePicker() {
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String dateStr = EdtDateExam.getText() != null ? EdtDateExam.getText().toString().trim() : "";

            if (dateStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date selectedDate = sdf.parse(dateStr);

                Calendar selectedCal = Calendar.getInstance();
                if (selectedDate != null) {
                    selectedCal.setTime(selectedDate);
                }

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);

                selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                selectedCal.set(Calendar.MINUTE, 0);
                selectedCal.set(Calendar.SECOND, 0);

                boolean isToday = selectedCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && selectedCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

                if (isToday) {
                    Calendar currentTime = Calendar.getInstance();

                    if (selectedHour < currentTime.get(Calendar.HOUR_OF_DAY)
                            || (selectedHour == currentTime.get(Calendar.HOUR_OF_DAY)
                            && selectedMinute <= currentTime.get(Calendar.MINUTE))) {
                        Toast.makeText(this, "Giờ dự kiến phải lớn hơn thời gian hiện tại!", Toast.LENGTH_SHORT).show();
                        EdtTimeExam.setText("");
                        EdtTimeExam.setError("Giờ không hợp lệ");
                        return;
                    }
                }

                EdtTimeExam.setError(null);
                EdtTimeExam.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi xử lý ngày!", Toast.LENGTH_SHORT).show();
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void createSlip() {
        if (foundPatient == null) {
            Toast.makeText(this, "Vui lòng tìm bệnh nhân trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDoctorID == null || selectedDoctorID.isEmpty()) {
            ActvDoctor.setError("Vui lòng chọn bác sĩ");
            ActvDoctor.requestFocus();
            return;
        }

        String dateStr = EdtDateExam.getText() != null ? EdtDateExam.getText().toString().trim() : "";
        String timeStr = EdtTimeExam.getText() != null ? EdtTimeExam.getText().toString().trim() : "";
        String symptoms = EdtSymptoms.getText() != null ? EdtSymptoms.getText().toString().trim() : "";
        String feeStr = EdtFee.getText() != null ? EdtFee.getText().toString().trim() : "";
        String status = EdtStatus.getText() != null ? EdtStatus.getText().toString().trim() : "";
        String paymentMethod = ActvPaymentMethod.getText() != null ? ActvPaymentMethod.getText().toString().trim() : "";

        if (dateStr.isEmpty()) {
            EdtDateExam.setError("Vui lòng chọn ngày khám");
            EdtDateExam.requestFocus();
            return;
        }

        if (timeStr.isEmpty()) {
            EdtTimeExam.setError("Vui lòng chọn giờ khám");
            EdtTimeExam.requestFocus();
            return;
        }

        if (symptoms.isEmpty()) {
            EdtSymptoms.setError("Vui lòng nhập triệu chứng ban đầu");
            EdtSymptoms.requestFocus();
            return;
        }

        if (paymentMethod.isEmpty()) {
            ActvPaymentMethod.setError("Vui lòng chọn phương thức thanh toán");
            ActvPaymentMethod.requestFocus();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date ngayKham = sdf.parse(dateStr);

            if (ngayKham == null) {
                Toast.makeText(this, "Ngày khám không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            int phiKham = Integer.parseInt(feeStr.replace(".", "").trim());

            CreateExFormRequest request = new CreateExFormRequest(
                    ngayKham,
                    timeStr,
                    0,
                    symptoms,
                    phiKham,
                    status,
                    paymentMethod,
                    foundPatient.getId(),
                    selectedDoctorID,
                    selectedAppointment != null ? selectedAppointment.getId() : null
            );

            ExFormRepository.createForm(request).enqueue(new Callback<List<ExaminationForm>>() {
                @Override
                public void onResponse(@NonNull Call<List<ExaminationForm>> call, @NonNull Response<List<ExaminationForm>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Toast.makeText(CreateExaminationForm_staff.this, "Tạo phiếu khám thành công!", Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        Toast.makeText(CreateExaminationForm_staff.this, "Tạo phiếu thất bại!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ExaminationForm>> call, @NonNull Throwable t) {
                    Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Lỗi kết nối: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_LONG).show();
            Log.d("Invalid data", e.getMessage());
        }
    }

    private void modifyCurrentPatientViewsValues() {
        if (foundPatient == null) return;

        TvFullName.setText("Họ tên: " + safeText(foundPatient.getHo_ten()));
        TvAddress.setText("Địa chỉ: " + safeText(foundPatient.getDia_chi()));

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = foundPatient.getNgay_sinh() != null
                ? formatter.format(foundPatient.getNgay_sinh())
                : "--";

        TvBirthday.setText("Ngày sinh: " + formattedDate);
        updateTransferContent();
        updateVietQrSection();
    }

    private void clearPatientInfo() {
        TvFullName.setText("Họ tên: --");
        TvBirthday.setText("Ngày sinh: --");
        TvAddress.setText("Địa chỉ: --");
        updateTransferContent();
    }

    private void updateTransferContent() {
        TvTransferContent.setText("Nội dung: " + buildTransferContentForQr());
    }

    private void updatePredictedReceptionNumber() {
        String dateStr = EdtDateExam.getText() != null ? EdtDateExam.getText().toString().trim() : "";

        if (dateStr.isEmpty()) {
            TvSTN.setText("Số tiếp nhận: --");
            return;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date selectedDate = inputFormat.parse(dateStr);
            if (selectedDate == null) {
                TvSTN.setText("Số tiếp nhận: --");
                return;
            }

            String apiDate = apiFormat.format(selectedDate);

            ExFormRepository.getFormsByDate(apiDate).enqueue(new Callback<List<ExaminationForm>>() {
                @Override
                public void onResponse(@NonNull Call<List<ExaminationForm>> call, @NonNull Response<List<ExaminationForm>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        int maxSoTiepNhan = response.body().get(0).getSo_tiep_nhan();
                        TvSTN.setText("Số tiếp nhận: " + (maxSoTiepNhan + 1));
                    } else {
                        TvSTN.setText("Số tiếp nhận: 1");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ExaminationForm>> call, @NonNull Throwable t) {
                    TvSTN.setText("Số tiếp nhận: --");
                }
            });
        } catch (Exception e) {
            TvSTN.setText("Số tiếp nhận: --");
        }
    }

    private void resetForm() {
        foundPatient = null;
        selectedDoctorID = null;
        latestAvailableAppointment = null;
        selectedAppointment = null;

        EdtSearchPatient.setText("");
        EdtDateExam.setText("");
        EdtTimeExam.setText("");
        EdtSymptoms.setText("");
        EdtFee.setText("100.000");
        EdtStatus.setText("Chờ khám");

        ActvDoctor.setText("", false);
        ActvDoctor.clearFocus();
        ActvPaymentMethod.setText("Tiền mặt", false);

        LayoutQRCode.setVisibility(View.GONE);
        TvSTN.setText("Số tiếp nhận: --");

        EdtDateExam.setError(null);
        EdtTimeExam.setError(null);
        EdtSymptoms.setError(null);
        ActvDoctor.setError(null);
        ActvPaymentMethod.setError(null);
        EdtSearchPatient.setError(null);

        clearPatientInfo();
        resetLatestAppointmentCard();
        updateVietQrSection();
    }

    private String findDoctorNameById(String doctorId) {
        if (doctorId == null || doctorList == null) return "--";

        for (UserProfile doctor : doctorList) {
            if (doctorId.equals(doctor.getID())) {
                return doctor.getHo_ten();
            }
        }

        return "--";
    }

    private String removeDoctorPrefix(String doctorName) {
        if (doctorName == null) return "";
        return doctorName.replaceFirst("^BS\\.\\s*", "").trim();
    }

    private String formatApiDateToDisplay(String apiDate) {
        if (apiDate == null || apiDate.trim().isEmpty()) return "--";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = input.parse(apiDate);
            return date != null ? output.format(date) : "--";
        } catch (Exception e) {
            return apiDate;
        }
    }

    private boolean shouldCopyAppointmentDate(AppointmentItem appointmentItem) {
        Date appointmentDate = parseApiDate(appointmentItem != null ? appointmentItem.getNgay_hen() : null);
        if (appointmentDate == null) return false;

        Date today = getDateOnly(Calendar.getInstance().getTime());
        Date appointmentDateOnly = getDateOnly(appointmentDate);

        return !today.after(appointmentDateOnly);
    }

    private Date parseApiDate(String apiDate) {
        if (apiDate == null || apiDate.trim().isEmpty()) return null;
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            input.setLenient(false);
            return input.parse(apiDate.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Date getDateOnly(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void updateVietQrSection() {
        String selectedMethod = ActvPaymentMethod.getText() != null
                ? ActvPaymentMethod.getText().toString().trim()
                : "";

        boolean isTransfer = "Chuyển khoản".equals(selectedMethod);
        LayoutQRCode.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        if (!isTransfer) {
            return;
        }

        String transferContent = buildTransferContentForQr();
        TvTransferContent.setText("Nội dung: " + transferContent);

        String qrUrl = buildVietQrImageUrl(transferContent);

        ImgQRCode.setImageDrawable(null);

        ImageRequest request = new ImageRequest.Builder(this)
                .data(qrUrl)
                .target(ImgQRCode)
                .crossfade(true)
                .build();

        Coil.imageLoader(this).enqueue(request);
    }

    private String buildTransferContentForQr() {
        if (foundPatient == null) {
            return "[MaBN] [HoTen]";
        }

        String patientId = safeText(foundPatient.getId());
        String patientName = removeVietnameseTones(safeText(foundPatient.getHo_ten()))
                .replaceAll("\\s+", " ")
                .trim();

        if ("--".equals(patientId) || patientName.isEmpty() || "--".equals(patientName)) {
            return "[MaBN] [HoTen]";
        }

        return patientId + " " + patientName;
    }

    private String buildVietQrImageUrl(String transferContent) {
        return "https://img.vietqr.io/image/"
                + QR_BANK_ID
                + "-"
                + QR_ACCOUNT_NO
                + "-compact2.png?amount="
                + QR_AMOUNT
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

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "--" : value;
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
}
