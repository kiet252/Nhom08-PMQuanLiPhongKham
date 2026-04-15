package dashboard_fragment.add_update_patient.add_patient_logic;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import dashboard_fragment.add_update_patient.ExternalCall;
import dashboard_fragment.add_update_patient.PatientDatabaseConstraintsChecker;
import dashboard_fragment.add_update_patient.PatientProfile;
import dashboard_fragment.add_update_patient.PatientRepository;
import retrofit2.Call;

public class CreatePatientFragment extends Fragment {
    private EditText EdtFullName, EdtAddress, EdtPhone, EdtCCCD;
    private RadioGroup RgGender;
    private RadioButton selectedRadioButton;
    private Spinner SpnBirthDay, SpnBirthMonth, SpnBirthYear;
    private String currentToken;
    private PatientRepository repository;
    public CreatePatientFragment(String token) {
        currentToken = token;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.staff_fragment_create_patient, container, false);

        initializeViews(view);
        initializeValuesForMonthAndYearSpinners();
        setupListeners();

        repository = new PatientRepository(getString(R.string.abAIkey));

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
    }

    private void initializeValuesForMonthAndYearSpinners() {
        // Years (e.g., 1900 to current year)
        List<Integer> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1900; i--) years.add(i);

        // Months (1-12)
        List<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add(i);

        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years);
        ArrayAdapter<Integer> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, months);

        SpnBirthYear.setAdapter(yearAdapter);
        SpnBirthMonth.setAdapter(monthAdapter);
    }
    private void setupListeners() {
        SpnBirthMonth.setOnItemSelectedListener(dateChangeListener);
        SpnBirthYear.setOnItemSelectedListener(dateChangeListener);
    }

    private final AdapterView.OnItemSelectedListener dateChangeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateDaySpinner();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void updateDaySpinner() {
        Object monthObj = SpnBirthMonth.getSelectedItem();
        Object yearObj = SpnBirthYear.getSelectedItem();

        if (monthObj == null || yearObj == null) return;

        int selectedMonth = (int) monthObj;
        int selectedYear = (int) yearObj;

        Object dayObj = SpnBirthDay.getSelectedItem();
        int currentSelectedDay = (dayObj != null) ? (int) dayObj : 1;

        List<Integer> days = getDaysList(selectedMonth, selectedYear);

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpnBirthDay.setAdapter(adapter);

        // If the previous day still exists (e.g., switching from Jan 31 to Mar 31), keep it
        if (currentSelectedDay <= days.size()) {
            SpnBirthDay.setSelection(currentSelectedDay - 1);
        } else {
            // If it doesn't exist (e.g., Feb 31), cap it at the max (Feb 28/29)
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

        repository.createProfile(currentToken, PatientToCreate()).enqueue(new retrofit2.Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call, @NonNull retrofit2.Response<List<PatientProfile>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        Toast.makeText(requireContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("API_DEBUG", "Success but empty body");
                    }
                } else if (response.code() == 409) {
                    EdtCCCD.setError("Số CCCD này đã tồn tại trong hệ thống!");
                    EdtCCCD.requestFocus();
                } else {
                    try {
                        Toast.makeText(requireContext(), "Lỗi: " + response.code() + (response.errorBody()).string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PatientProfile>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (selectedRadioButton == null) return false;

        return true;
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
}