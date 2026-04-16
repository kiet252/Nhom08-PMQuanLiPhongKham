package dashboard_fragment.create_examination_form;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.add_update_patient.PatientProfile;
import dashboard_fragment.add_update_patient.PatientRepository;
import retrofit2.Call;

public class CreateExaminationForm_staff extends AppCompatActivity {
    private ImageButton BtnBackCreateSlip;
    private MaterialButton BtnSearchPatient, BtnCreateSlip, BtnDeleteSlip;
    private TextView TvFullName, TvBirthday, TvAddress, TvSTT;
    private TextInputEditText EdtSearchPatient, EdtDateExam, EdtTimeExam, EdtSymptoms, EdtFee, EdtStatus;
    private AutoCompleteTextView ActvDoctor, ActvPaymentMethod;
    private String currentToken;
    private ExaminationFormRepository ExFormRepository;
    private PatientRepository PaRepository;
    private PatientProfile foundPatient;

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
        setupListeners();
        getIntentInfo();
        ExFormRepository = new ExaminationFormRepository(getString(R.string.abAIkey));
        PaRepository = new PatientRepository(getString(R.string.abAIkey));
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
        TvSTT = findViewById(R.id.tvCreateExaminationFormSTT);

        EdtSearchPatient = findViewById(R.id.edtCreateExaminationFormSearchPatient);
        EdtDateExam = findViewById(R.id.edtCreateExaminationFormDateExam);
        EdtTimeExam = findViewById(R.id.edtCreateExaminationFormTimeExam);
        EdtSymptoms = findViewById(R.id.edtCreateExaminationFormSymptoms);
        EdtFee = findViewById(R.id.edtCreateExaminationFormFee);
        EdtStatus = findViewById(R.id.edtCreateExaminationFormStatus);

        ActvDoctor = findViewById(R.id.actvCreateExaminationFormDoctor);
        ActvPaymentMethod = findViewById(R.id.actvCreateExaminationFormPaymentMethod);
    }
    private void setupListeners() {
        BtnBackCreateSlip.setOnClickListener(v -> backToPreviousActivity());
        BtnCreateSlip.setOnClickListener(v->CreateSlip());
        BtnSearchPatient.setOnClickListener(v->sendPatientSearchRequest());
    }
    private void backToPreviousActivity() {
        finish();
    }
    private void CreateSlip()
    {

    }
    private void sendPatientSearchRequest(){
        String SearchValue = EdtSearchPatient.getText().toString().trim();

        if (SearchValue.isEmpty()) {
            EdtSearchPatient.setError("Vui lòng nhập id hoặc cccd cần tìm!");
            EdtSearchPatient.requestFocus();
            return;
        }

        PaRepository.getProfileByIdOrCccd(currentToken, SearchValue).enqueue(new retrofit2.Callback<List<PatientProfile>>() {
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

        TvFullName.setText("Họ và tên: "+foundPatient.getHo_ten());
        TvAddress.setText("Địa chỉ: "+foundPatient.getDia_chi());

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = formatter.format(foundPatient.getNgay_sinh());
        TvBirthday.setText("Ngày sinh: "+ formattedDate);
    }
}
