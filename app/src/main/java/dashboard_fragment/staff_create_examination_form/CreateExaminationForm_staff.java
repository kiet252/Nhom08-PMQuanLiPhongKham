package dashboard_fragment.staff_create_examination_form;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
            String selectedMethod = parent.getItemAtPosition(position).toString();
            LayoutQRCode.setVisibility("Chuyển khoản".equals(selectedMethod) ? View.VISIBLE : View.GONE);
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
                Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối khi tải bác sĩ", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        String displayDate = formatApiDateToDisplay(appointmentItem.getNgay_hen());
        EdtDateExam.setText(displayDate);
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

            if (selectedAppointment != null) {
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
                    Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Dữ liệu không hợp lệ: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
    }

    private void clearPatientInfo() {
        TvFullName.setText("Họ tên: --");
        TvBirthday.setText("Ngày sinh: --");
        TvAddress.setText("Địa chỉ: --");
        updateTransferContent();
    }

    private void updateTransferContent() {
        if (foundPatient == null) {
            TvTransferContent.setText("Nội dung: [MaBN] [HoTen]");
            return;
        }

        TvTransferContent.setText("Nội dung: " + foundPatient.getId() + " " + safeText(foundPatient.getHo_ten()));
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