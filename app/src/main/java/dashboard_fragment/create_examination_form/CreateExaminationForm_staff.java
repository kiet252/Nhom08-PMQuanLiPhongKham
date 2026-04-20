package dashboard_fragment.create_examination_form;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.add_update_patient.PatientProfile;
import dashboard_fragment.add_update_patient.PatientRepository;
import dashboard_fragment.create_examination_form.create_ex_form_logic.CreateExFormRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateExaminationForm_staff extends AppCompatActivity {
    private ImageButton BtnBackCreateSlip;
    private MaterialButton BtnSearchPatient, BtnCreateSlip, BtnDeleteSlip;
    private TextView TvFullName, TvBirthday, TvAddress, TvSTN, TvTransferContent;
    private TextInputEditText EdtSearchPatient, EdtDateExam, EdtTimeExam, EdtSymptoms, EdtFee, EdtStatus;
    private AutoCompleteTextView ActvDoctor, ActvPaymentMethod;
    private LinearLayout LayoutQRCode;
    private String currentToken;
    private ExaminationFormRepository ExFormRepository;
    private PatientRepository PaRepository;
    private PatientProfile foundPatient;
    private ProfileRepository profileRepository;
    private List<UserProfile> doctorList;
    private String selectedDoctorID;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_create_examination_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_examination), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeValuesForPaymentMethod();
        setupListeners();
        getIntentInfo();

        ExFormRepository = new ExaminationFormRepository(this);
        PaRepository = new PatientRepository(this);
        profileRepository = new ProfileRepository(this);

        loadDoctors();
        TvSTN.setText("Số tiếp nhận: --");

    }
    private void getIntentInfo() {
        Intent currentIntent = getIntent();
        currentToken = currentIntent.getStringExtra("accessToken");
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

    private void loadDoctors(){
        profileRepository.getDoctors(currentToken).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfile>> call, @NonNull Response<List<UserProfile>> response) {

                if (!response.isSuccessful()) {
                    try {

                        String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Không rõ lỗi";
                        Toast.makeText(CreateExaminationForm_staff.this, "Lỗi tải danh sách bác sĩ: " + response.code() + " - " + errorMessage, Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {

                        Toast.makeText(CreateExaminationForm_staff.this, "Lỗi tải danh sách bác sĩ: " + response.code(), Toast.LENGTH_SHORT).show();

                    }
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {

                    Toast.makeText(CreateExaminationForm_staff.this, "Không có bác sĩ nào để hiển thị!", Toast.LENGTH_SHORT).show();
                    return;

                }

                doctorList = response.body();

                List<String> doctorNames = new ArrayList<>();
                for (UserProfile doctor : doctorList) {
                    doctorNames.add(doctor.getHo_ten());
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
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfile>> call, @NonNull Throwable t) {

                Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối khi tải bác sĩ: " + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    EdtDateExam.setText(date);
                    updatePredictedReceptionNumber();
                },
                year, month, day
        );

        long today = System.currentTimeMillis();

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 30);

        datePickerDialog.getDatePicker().setMinDate(today);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void showTimePicker(){
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {

            String dateStr = EdtDateExam.getText().toString().trim();

            if (dateStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date selectedDate = sdf.parse(dateStr);

                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);

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

                    if (selectedHour < currentTime.get(Calendar.HOUR_OF_DAY) || (selectedHour == currentTime.get(Calendar.HOUR_OF_DAY)
                            && selectedMinute <= currentTime.get(Calendar.MINUTE))) {

                        Toast.makeText(this, "Giờ dự kiến phải lớn hơn thời gian hiện tại!", Toast.LENGTH_SHORT).show();


                        EdtTimeExam.setText("");
                        EdtTimeExam.setError("Giờ không hợp lệ");

                        return;
                    }
                }

                EdtTimeExam.setError(null);
                String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                EdtTimeExam.setText(time);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi xử lý ngày!", Toast.LENGTH_SHORT).show();
            }}, hour, minute, true
        );

        timePickerDialog.show();
    }
    private void initializeValuesForPaymentMethod(){
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
    private void setupListeners() {
        BtnBackCreateSlip.setOnClickListener(v -> backToPreviousActivity());
        BtnCreateSlip.setOnClickListener(v->CreateSlip());
        BtnSearchPatient.setOnClickListener(v->sendPatientSearchRequest());

        EdtDateExam.setOnClickListener(v -> showDatePicker());
        EdtTimeExam.setOnClickListener(v-> showTimePicker());
        ActvPaymentMethod.setOnClickListener(v->ActvPaymentMethod.showDropDown());
        ActvPaymentMethod.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMethod = parent.getItemAtPosition(position).toString();

            if ("Chuyển khoản".equals(selectedMethod)) {
                LayoutQRCode.setVisibility(View.VISIBLE);
            } else {
                LayoutQRCode.setVisibility(View.GONE);
            }
        });

        BtnDeleteSlip.setOnClickListener(v -> resetForm());
    }
    private void backToPreviousActivity() {
        finish();
    }
    private void CreateSlip() {
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
                    selectedDoctorID
            );

            ExFormRepository.createForm(request).enqueue(new Callback<List<ExaminationForm>>() {
                @Override
                public void onResponse(@NonNull Call<List<ExaminationForm>> call,
                                       @NonNull Response<List<ExaminationForm>> response) {

                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {

                        Toast.makeText(CreateExaminationForm_staff.this,
                                "Tạo phiếu khám thành công!",
                                Toast.LENGTH_SHORT).show();

                        resetForm();

                    } else {
                        try {
                            String errorMessage = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Không rõ lỗi";

                            Toast.makeText(CreateExaminationForm_staff.this,
                                    "Tạo phiếu thất bại: " + response.code() + " - " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Toast.makeText(CreateExaminationForm_staff.this,
                                    "Tạo phiếu thất bại: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ExaminationForm>> call, @NonNull Throwable t) {
                    Toast.makeText(CreateExaminationForm_staff.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Dữ liệu không hợp lệ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPatientSearchRequest(){
        String SearchValue = EdtSearchPatient.getText().toString().trim();

        if (SearchValue.isEmpty()) {
            EdtSearchPatient.setError("Vui lòng nhập id hoặc cccd cần tìm!");
            EdtSearchPatient.requestFocus();
            return;
        }

        PaRepository.getProfileByIdOrCccd(SearchValue).enqueue(new retrofit2.Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call, @NonNull retrofit2.Response<List<PatientProfile>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        Toast.makeText(CreateExaminationForm_staff.this, "Tìm thành công!", Toast.LENGTH_SHORT).show();

                        foundPatient = response.body().get(0);
                        modifyCurrentPatientViewsValues();
                    } else {
                        Toast.makeText(CreateExaminationForm_staff.this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Toast.makeText(CreateExaminationForm_staff.this, "Lỗi: " + response.code() + (response.errorBody()).string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                Toast.makeText(CreateExaminationForm_staff.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void modifyCurrentPatientViewsValues() {
        if (foundPatient == null) return;

        TvFullName.setText("Họ tên: "+foundPatient.getHo_ten());
        TvAddress.setText("Địa chỉ: "+foundPatient.getDia_chi());

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = formatter.format(foundPatient.getNgay_sinh());
        TvBirthday.setText("Ngày sinh: "+ formattedDate);

        updateTransferContent();
    }

    private void updateTransferContent() {
        if (foundPatient == null) {
            TvTransferContent.setText("Nội dung: [MaBN] [HoTen]");
            return;
        }

        String patientId = foundPatient.getId();
        String patientName = foundPatient.getHo_ten();

        String transferContent = "Nội dung: " + patientId + " " + patientName;
        TvTransferContent.setText(transferContent);
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
                public void onResponse(@NonNull Call<List<ExaminationForm>> call,
                                       @NonNull Response<List<ExaminationForm>> response) {
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
    private void resetForm(){
        foundPatient = null;
        EdtSearchPatient.setText("");
        TvFullName.setText("Họ tên: --");
        TvBirthday.setText("Ngày sinh: --");
        TvAddress.setText("Địa chỉ: --");
        EdtDateExam.setText("");
        EdtTimeExam.setText("");
        TvSTN.setText("Số tiếp nhận: --");
        selectedDoctorID = null;
        ActvDoctor.setText("", false);
        ActvDoctor.clearFocus();
        EdtSymptoms.setText("");
        EdtFee.setText("100.000");
        EdtStatus.setText("Chờ khám");
        ActvPaymentMethod.setText("Tiền mặt", false);
        LayoutQRCode.setVisibility(View.GONE);
        TvTransferContent.setText("Nội dung: [MaBN] [HoTen]");
        EdtDateExam.setError(null);
        EdtTimeExam.setError(null);
        EdtSymptoms.setError(null);
        ActvDoctor.setError(null);
        ActvPaymentMethod.setError(null);
        EdtSearchPatient.setError(null);

    }

}
