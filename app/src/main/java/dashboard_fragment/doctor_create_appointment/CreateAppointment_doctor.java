package dashboard_fragment.doctor_create_appointment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.doctor_create_appointment.create_appointment_logic.CreateAppointmentRequest;
import dashboard_fragment.doctor_view_medical_record.SearchHistoryManager;
import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAppointment_doctor extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText edtSearchPatient;
    private MaterialButton btnSearchPatient, btnConfirmAppointment;
    private ChipGroup chipGroupRecentSearch;
    private LinearLayout bottomLayout;
    private MaterialCardView cardPatientFound, cardAppDate, cardAppNote, cardAppConfirm;

    private LinearLayout layoutConfirmNoteContainer;
    private TextView tvFoundName, tvFoundDetails, tvFoundPhone, tvFoundAddress;
    private TextView tvConfirmPatientName, tvConfirmDoctor, tvConfirmDate, tvConfirmNote;
    private TextInputEditText edtAppointmentDate, edtNote;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private UserProfile doctorProfile;

    private PatientProfile selectedPatient = null;
    private String selectedDate = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_create_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_appointment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        doctorProfile = SharedPrefManager.getInstance(this).getProfile();
        patientRepository = new PatientRepository(this);
        appointmentRepository = new AppointmentRepository(this);
        initViews();
        setupListeners();
        loadRecentSearches();
    }
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearchPatient = findViewById(R.id.edtSearchPatient);
        btnSearchPatient = findViewById(R.id.btnSearchPatient);
        btnConfirmAppointment = findViewById(R.id.btnConfirmAppointment);
        chipGroupRecentSearch = findViewById(R.id.chipGroupRecentSearch);
        bottomLayout = findViewById(R.id.bottomLayout);

        layoutConfirmNoteContainer = findViewById(R.id.layoutConfirmNoteContainer);

        edtAppointmentDate = findViewById(R.id.edtAppointmentDate);
        edtNote = findViewById(R.id.edtNote);
        tvFoundName = findViewById(R.id.tvFoundName);
        tvFoundDetails = findViewById(R.id.tvFoundDetails);
        tvFoundPhone = findViewById(R.id.tvFoundPhone);
        tvFoundAddress = findViewById(R.id.tvFoundAddress);
        tvConfirmPatientName = findViewById(R.id.tvConfirmPatientName);
        tvConfirmDoctor = findViewById(R.id.tvConfirmDoctor);
        tvConfirmDate = findViewById(R.id.tvConfirmDate);
        tvConfirmNote = findViewById(R.id.tvConfirmNote);

        cardPatientFound = findViewById(R.id.cardPatientFound);
        cardAppDate = findViewById(R.id.cardAppDate);
        cardAppNote = findViewById(R.id.cardAppNote);
        cardAppConfirm = findViewById(R.id.cardAppConfirm);
    }
    private void setupListeners(){
        btnBack.setOnClickListener(v -> finish());
        btnSearchPatient.setOnClickListener(v -> {
            String input = edtSearchPatient.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã bệnh nhân hoặc CCCD", Toast.LENGTH_SHORT).show();
                return;
            }
            performSearch(input);
        });
        edtAppointmentDate.setOnClickListener(v -> showDatePicker());

        edtNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String noteText = s.toString().trim();
                if (noteText.isEmpty()) {
                    layoutConfirmNoteContainer.setVisibility(View.GONE);
                } else {
                    layoutConfirmNoteContainer.setVisibility(View.VISIBLE);
                    tvConfirmNote.setText(noteText);
                }
            }
        });
        btnConfirmAppointment.setOnClickListener(v -> submitAppointment());
    }
    private void performSearch(String input) {
        patientRepository.getProfileByIdOrCccd(input).enqueue(new Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call, @NonNull Response<List<PatientProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    selectedPatient = response.body().get(0);


                    SearchHistoryManager.getInstance().addSearch(selectedPatient);
                    loadRecentSearches();


                    renderFoundPatient();

                } else {
                    selectedPatient = null;
                    Toast.makeText(CreateAppointment_doctor.this, "Không tìm thấy bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PatientProfile>> call, @NonNull Throwable t) {
                Toast.makeText(CreateAppointment_doctor.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecentSearches() {
        chipGroupRecentSearch.removeAllViews();
        for (PatientProfile patient : SearchHistoryManager.getInstance().getRecentSearches()) {
            addRecentSearchChip(patient);
        }
    }

    private void addRecentSearchChip(PatientProfile patient) {
        String patientIdStr = String.valueOf(patient.getId());
        String chipText = patientIdStr + " - " + patient.getHo_ten();

        Chip chip = new Chip(this);
        chip.setText(chipText);
        chip.setCheckable(false);
        chip.setCloseIconVisible(false);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setRippleColorResource(android.R.color.darker_gray);
        chip.setOnClickListener(v -> {
            edtSearchPatient.setText(patientIdStr);
            performSearch(patientIdStr);
        });

        chipGroupRecentSearch.addView(chip, 0);
    }

    private void renderFoundPatient() {
        cardPatientFound.setVisibility(View.VISIBLE);

        tvFoundName.setText(selectedPatient.getHo_ten());
        String gender = selectedPatient.getGioi_tinh() != null ? selectedPatient.getGioi_tinh() : "N/A";
        String dob = "";
        if (selectedPatient.getNgay_sinh() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dob = sdf.format(selectedPatient.getNgay_sinh());
        }
        tvFoundDetails.setText(selectedPatient.getId() + " · " + gender + " · " + dob);
        tvFoundPhone.setText(selectedPatient.getSo_dien_thoai() != null ? selectedPatient.getSo_dien_thoai() : "Chưa cập nhật");
        tvFoundAddress.setText(selectedPatient.getDia_chi() != null ? selectedPatient.getDia_chi() : "Chưa cập nhật");

        tvConfirmPatientName.setText(selectedPatient.getHo_ten() + " (" + selectedPatient.getId() + ")");
        tvConfirmDoctor.setText("BS. " + doctorProfile.getHo_ten());

        cardAppDate.setVisibility(View.VISIBLE);
        cardAppNote.setVisibility(View.VISIBLE);
        cardAppConfirm.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dbFormat.format(calendar.getTime());

            SimpleDateFormat uiFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String uiDate = uiFormat.format(calendar.getTime());

            edtAppointmentDate.setText(uiDate);
            tvConfirmDate.setText(uiDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void submitAppointment() {
        if (selectedPatient == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn bệnh nhân và ngày hẹn!", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = edtNote.getText().toString().trim();
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                String.valueOf(selectedPatient.getId()),
                doctorProfile.getID(),
                selectedDate,
                note
        );

        btnConfirmAppointment.setEnabled(false);
        appointmentRepository.createAppointment(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateAppointment_doctor.this, "Tạo lịch hẹn thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateAppointment_doctor.this, "Lỗi tạo lịch hẹn", Toast.LENGTH_SHORT).show();
                    btnConfirmAppointment.setEnabled(true);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CreateAppointment_doctor.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                btnConfirmAppointment.setEnabled(true);
            }
        });
    }
}
