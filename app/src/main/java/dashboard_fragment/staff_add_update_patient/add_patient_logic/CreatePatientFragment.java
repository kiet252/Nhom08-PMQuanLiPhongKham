package dashboard_fragment.staff_add_update_patient.add_patient_logic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.staff_add_update_patient.ExternalCall;
import dashboard_fragment.staff_add_update_patient.PatientDatabaseConstraintsChecker;
import dashboard_fragment.staff_add_update_patient.PatientProfile;
import dashboard_fragment.staff_add_update_patient.PatientRepository;
import retrofit2.Call;

public class CreatePatientFragment extends Fragment {
    private EditText EdtFullName, EdtAddress, EdtPhone, EdtCCCD;
    private RadioGroup RgGender;
    private RadioButton selectedRadioButton;
    private Spinner SpnBirthDay, SpnBirthMonth, SpnBirthYear;
    private CardView btnOCR, btnQR;
    private PatientRepository repository;
    private ActivityResultLauncher<Intent> ocrLauncher;
    private ActivityResultLauncher<Intent> qrLauncher;

    public CreatePatientFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ocrLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        updateUIWithOCR(result.getData());
                    }
                }
        );

        qrLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        updateUIWithQR(result.getData());
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.staff_fragment_create_patient, container, false);

        initializeViews(view);
        initializeValuesForMonthAndYearSpinners();
        setupListeners();

        repository = new PatientRepository(requireContext());

        return view;
    }

    private void initializeViews(View view) {
        EdtFullName = view.findViewById(R.id.edtCreatePatientFullName);
        EdtAddress = view.findViewById(R.id.edtCreatePatientAddress);
        EdtPhone = view.findViewById(R.id.edtCreatePatientPhone);
        EdtCCCD = view.findViewById(R.id.edtCreatePatientCCCD);

        RgGender = view.findViewById(R.id.rgCreatePatientGender);

        SpnBirthDay = view.findViewById(R.id.spnCreatePatientDay);
        SpnBirthMonth = view.findViewById(R.id.spnCreatePatientMonth);
        SpnBirthYear = view.findViewById(R.id.spnCreatePatientYear);

        btnOCR = view.findViewById(R.id.btnOCR);
        btnQR = view.findViewById(R.id.btnQRCCCD);
    }

    private void initializeValuesForMonthAndYearSpinners() {
        List<Integer> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1900; i--) {
            years.add(i);
        }

        List<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
        }

        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<Integer> monthAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                months
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SpnBirthYear.setAdapter(yearAdapter);
        SpnBirthMonth.setAdapter(monthAdapter);

        SpnBirthYear.setSelection(0);
        SpnBirthMonth.setSelection(0);
        updateDaySpinner();
        SpnBirthDay.setSelection(0);
    }

    private void setupListeners() {
        SpnBirthMonth.setOnItemSelectedListener(dateChangeListener);
        SpnBirthYear.setOnItemSelectedListener(dateChangeListener);

        btnOCR.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OCRCameraActivity.class);
            ocrLauncher.launch(intent);
        });
        btnQR.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QRCCCDCameraActivity.class);
            qrLauncher.launch(intent);
        });
    }

    private final AdapterView.OnItemSelectedListener dateChangeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateDaySpinner();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private void updateDaySpinner() {
        if (SpnBirthMonth == null || SpnBirthYear == null || SpnBirthDay == null) return;

        Object monthObj = SpnBirthMonth.getSelectedItem();
        Object yearObj = SpnBirthYear.getSelectedItem();

        if (monthObj == null || yearObj == null) return;

        int selectedMonth = (int) monthObj;
        int selectedYear = (int) yearObj;

        Object dayObj = SpnBirthDay.getSelectedItem();
        int currentSelectedDay = (dayObj != null) ? (int) dayObj : 1;

        List<Integer> days = getDaysList(selectedMonth, selectedYear);

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                days
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpnBirthDay.setAdapter(adapter);

        if (currentSelectedDay <= days.size()) {
            SpnBirthDay.setSelection(currentSelectedDay - 1);
        } else {
            SpnBirthDay.setSelection(days.size() - 1);
        }
    }

    private List<Integer> getDaysList(int month, int year) {
        List<Integer> days = new ArrayList<>();

        Calendar calendar = new GregorianCalendar(year, month - 1, 1);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDays; i++) {
            days.add(i);
        }
        return days;
    }

    @ExternalCall
    public void submitPatientData() {
        if (!allCredentialsValid()) return;

        repository.createProfile(PatientToCreate()).enqueue(new retrofit2.Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call, @NonNull retrofit2.Response<List<PatientProfile>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        Toast.makeText(requireContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        Log.e("API_DEBUG", "Success but empty body");
                    }
                } else if (response.code() == 409) {
                    EdtCCCD.setError("Số CCCD này đã tồn tại trong hệ thống!");
                    EdtCCCD.requestFocus();
                } else {
                    try {
                        Toast.makeText(requireContext(), "Lỗi: " + response.code() + " " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                        Log.d("Error", "Lỗi kết nối: " + response.code() + ", " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private boolean allCredentialsValid() {
        if (!isValidFullName()) return false;
        if (!isValidAddress()) return false;
        if (!isValidPhoneNumber()) return false;
        if (!isValidGender()) return false;
        if (!isValidBirthDate()) return false;
        if (!isValidCCCD()) return false;
        return true;
    }

    private CreatePatientRequest PatientToCreate() {
        String fullName = EdtFullName.getText().toString().trim();

        int day = (int) SpnBirthDay.getSelectedItem();
        int month = (int) SpnBirthMonth.getSelectedItem();
        int year = (int) SpnBirthYear.getSelectedItem();
        Calendar cal = new GregorianCalendar(year, month - 1, day);
        Date birthday = cal.getTime();

        String gender = selectedRadioButton.getText().toString().trim();
        String address = EdtAddress.getText().toString().trim();
        String cccd = EdtCCCD.getText().toString().trim();
        String phoneNum = EdtPhone.getText().toString().trim();

        return new CreatePatientRequest(fullName, birthday, gender, address, cccd, phoneNum);
    }

    private boolean isValidFullName() {
        if (EdtFullName == null) return false;
        String fullName = EdtFullName.getText().toString().trim();

        if (fullName.isEmpty()) {
            EdtFullName.setError("Xin hãy nhập họ và tên!");
            EdtFullName.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidAddress() {
        if (EdtAddress == null) return false;
        String address = EdtAddress.getText().toString().trim();

        if (address.isEmpty()) {
            EdtAddress.setError("Xin hãy nhập địa chỉ!");
            EdtAddress.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidPhoneNumber() {
        if (EdtPhone == null) return false;
        String phoneNum = EdtPhone.getText().toString().trim();

        if (phoneNum.isEmpty()) {
            EdtPhone.setError("Xin hãy nhập số điện thoại!");
            EdtPhone.requestFocus();
            return false;
        }

        if (!PatientDatabaseConstraintsChecker.isValidPhoneNumInDB(phoneNum)) {
            EdtPhone.setError("Số điện thoại phải bắt đầu bằng số 0 và gồm 10 số!");
            EdtPhone.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidGender() {
        int selectedRadioBtnId = RgGender.getCheckedRadioButtonId();

        if (selectedRadioBtnId == -1) {
            Toast.makeText(requireContext(), "Xin hãy chọn giới tính!", Toast.LENGTH_SHORT).show();
            return false;
        }

        selectedRadioButton = RgGender.findViewById(selectedRadioBtnId);
        return selectedRadioButton != null;
    }

    private boolean isValidBirthDate() {
        int day = (int) SpnBirthDay.getSelectedItem();
        int month = (int) SpnBirthMonth.getSelectedItem();
        int year = (int) SpnBirthYear.getSelectedItem();

        Calendar birthDate = new GregorianCalendar(year, month - 1, day);
        Calendar today = Calendar.getInstance();

        if (birthDate.after(today)) {
            Toast.makeText(requireContext(), "Ngày sinh không thể là trong tương lai!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidCCCD() {
        if (EdtCCCD == null) return false;
        String cccd = EdtCCCD.getText().toString().trim();

        if (cccd.isEmpty()) {
            EdtCCCD.setError("Xin hãy nhập căn cước công dân!");
            EdtCCCD.requestFocus();
            return false;
        }

        if (!PatientDatabaseConstraintsChecker.isValidCCCDInDB(cccd)) {
            EdtCCCD.setError("Căn cước công dân phải đúng 12 số!");
            EdtCCCD.requestFocus();
            return false;
        }
        return true;
    }

    @ExternalCall
    public void resetForm() {
        EdtFullName.setText("");
        EdtAddress.setText("");
        EdtPhone.setText("");
        EdtCCCD.setText("");

        EdtFullName.setError(null);
        EdtAddress.setError(null);
        EdtPhone.setError(null);
        EdtCCCD.setError(null);

        RgGender.clearCheck();
        selectedRadioButton = null;

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 0; i < SpnBirthYear.getCount(); i++) {
            Object item = SpnBirthYear.getItemAtPosition(i);
            if (item instanceof Integer && (Integer) item == currentYear) {
                SpnBirthYear.setSelection(i);
                break;
            }
        }

        SpnBirthMonth.setSelection(0);
        updateDaySpinner();
        SpnBirthDay.setSelection(0);
    }

    private void updateUIWithOCR(Intent data) {
        String fullName = trimToNull(data.getStringExtra("full_name"));
        String idNumber = trimToNull(data.getStringExtra("id_number"));
        String dob = trimToNull(data.getStringExtra("dob"));
        String gender = trimToNull(data.getStringExtra("gender"));
        String address = trimToNull(data.getStringExtra("address"));

        if (fullName != null) EdtFullName.setText(fullName);
        if (idNumber != null) EdtCCCD.setText(idNumber);
        if (address != null) EdtAddress.setText(address);

        if (gender != null) {
            selectGender(gender);
        }

        if (dob != null && dob.contains("/")) {
            String[] parts = dob.split("/");
            if (parts.length == 3) {

                setSpinnerValue(SpnBirthYear, parts[2]);
                setSpinnerValue(SpnBirthMonth, parts[1]);

                updateDaySpinner();

                setSpinnerValue(SpnBirthDay, parts[0]);
            }
        }
    }

    private void updateUIWithQR(Intent data) {
        String fullName = trimToNull(data.getStringExtra("full_name"));
        String idNumber = trimToNull(data.getStringExtra("id_number"));
        String dob = trimToNull(data.getStringExtra("dob"));
        String gender = trimToNull(data.getStringExtra("gender"));
        String address = trimToNull(data.getStringExtra("address"));

        if (fullName != null) EdtFullName.setText(fullName);
        if (idNumber != null) EdtCCCD.setText(idNumber);
        if (address != null) EdtAddress.setText(address);
        if (gender != null) selectGender(gender);
        if (dob != null && dob.contains("/")) {

            String[] parts = dob.split("/");
            if (parts.length == 3) {
                setSpinnerValue(SpnBirthYear, parts[2]);
                setSpinnerValue(SpnBirthMonth, parts[1]);
                updateDaySpinner();
                setSpinnerValue(SpnBirthDay, parts[0]);

            }
        }
    }

    private void selectGender(String gender) {
        String normalizedGender = normalizeText(gender);

        for (int i = 0; i < RgGender.getChildCount(); i++) {
            View child = RgGender.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                String normalizedText = normalizeText(rb.getText().toString());

                if (normalizedGender.contains("nam") && normalizedText.contains("nam")) {
                    rb.setChecked(true);
                    return;
                }

                if ((normalizedGender.contains("nu") || normalizedGender.contains("female"))
                        && (normalizedText.contains("nu") || normalizedText.contains("khac") || normalizedText.contains("other"))) {
                    if (normalizedText.contains("nu")) {
                        rb.setChecked(true);
                        return;
                    }
                }
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        try {
            int val = Integer.parseInt(value.trim());
            ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
            if (adapter == null) return;

            for (int i = 0; i < adapter.getCount(); i++) {
                Object item = adapter.getItem(i);
                if (item != null && item.toString().equals(String.valueOf(val))) {
                    spinner.setSelection(i);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Log.e("OCR_ERROR", "Cannot parse date value: " + value);
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}