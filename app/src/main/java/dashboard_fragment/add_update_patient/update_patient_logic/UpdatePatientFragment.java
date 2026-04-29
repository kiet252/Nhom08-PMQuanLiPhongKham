package dashboard_fragment.add_update_patient.update_patient_logic;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.add_update_patient.ExternalCall;
import dashboard_fragment.add_update_patient.PatientDatabaseConstraintsChecker;
import dashboard_fragment.add_update_patient.PatientProfile;
import dashboard_fragment.add_update_patient.PatientRepository;
import dashboard_fragment.add_update_patient.update_patient_logic.update_patient_logic.UpdatePatientRequest;
import retrofit2.Call;

public class UpdatePatientFragment extends Fragment {
    private EditText EdtSearchPatient, EdtFullName, EdtBirthday, EdtAddress, EdtPhone, EdtCCCD;
    private TextInputLayout TilSearchPatientLayout;
    private RadioGroup RgGender;
    private RadioButton selectedRadioButton;
    private PatientRepository repository;
    private PatientProfile foundPatient;
    public UpdatePatientFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.staff_fragment_update_patient, container, false);

        initializeViews(view);
        setupListeners();

        repository = new PatientRepository(requireContext());

        return view;
    }
    private void initializeViews(View view) {
        EdtSearchPatient = view.findViewById(R.id.edtUpdatePatientSearchPatient);

        TilSearchPatientLayout = view.findViewById(R.id.tilUpdatePatientSearchPatientLayout);

        EdtFullName = view.findViewById(R.id.edtUpdatePatientFullName);
        EdtBirthday = view.findViewById(R.id.edtUpdatePatientBirthday);
        EdtAddress = view.findViewById(R.id.edtUpdatePatientAddress);
        EdtPhone = view.findViewById(R.id.edtUpdatePatientPhone);

        RgGender = view.findViewById(R.id.rgUpdatePatientGender);

        EdtCCCD = view.findViewById(R.id.edtUpdatePatientCCCD);
    }
    private void setupListeners() {
        TilSearchPatientLayout.setEndIconOnClickListener(v -> sendPatientSearchRequest());
    }

    private void sendPatientSearchRequest() {
        String SearchValue = EdtSearchPatient.getText().toString().trim();
        if (SearchValue.isEmpty()) {
            EdtSearchPatient.setError("Vui lòng nhập id hoặc cccd cần tìm!");
            EdtSearchPatient.requestFocus();
            return;
        }

        repository.getProfileByIdOrCccd(SearchValue).enqueue(new retrofit2.Callback<List<PatientProfile>>() {
            @Override
            public void onResponse(@NonNull Call<List<PatientProfile>> call, @NonNull retrofit2.Response<List<PatientProfile>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        Toast.makeText(requireContext(), "Tìm thành công!", Toast.LENGTH_SHORT).show();

                        foundPatient = response.body().get(0);
                        modifyCurrentPatientViewsValues();
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                    }
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

    private void modifyCurrentPatientViewsValues() {
        if (foundPatient == null) return;

        EdtFullName.setText(foundPatient.getHo_ten());
        EdtAddress.setText(foundPatient.getDia_chi());

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = formatter.format(foundPatient.getNgay_sinh());
        EdtBirthday.setText(formattedDate);

        EdtPhone.setText(foundPatient.getSo_dien_thoai());

        selectRadioButtonGender(foundPatient.getGioi_tinh());

        EdtCCCD.setText(foundPatient.getCccd());
    }

    private void selectRadioButtonGender(String targetGender) {
        if (targetGender == null) return;

        for (int i = 0; i < RgGender.getChildCount(); i++) {
            View view = RgGender.getChildAt(i);

            if (view instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) view;

                if (radioButton.getText().toString().equalsIgnoreCase(targetGender.trim())) {
                    radioButton.setChecked(true);
                    return;
                }
            }
        }
    }
    @ExternalCall
    public void submitPatientData() {
        if (foundPatient == null) {
            Toast.makeText(requireContext(), "Hãy tìm thông tin bệnh nhân cần cập nhật!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!allCredentialsValid()) return;

        String requestSDT = EdtPhone.getText().toString().trim();
        String requestDiaChi = EdtAddress.getText().toString().trim();
        String requestCCCD = EdtCCCD.getText().toString().trim();
        String requestGioiTinh = selectedRadioButton.getText().toString().trim();

        UpdatePatientRequest request = new UpdatePatientRequest(requestSDT, requestDiaChi, requestCCCD, requestGioiTinh);

        repository.updateProfile(foundPatient.getId(), request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    resetForm();
                } else if (response.code() == 409) {
                    EdtCCCD.setError("Số CCCD này đã tồn tại trong hệ thống!");
                    EdtCCCD.requestFocus();
                } else {
                    Log.e("API_ERROR", "Code: " + response.code() + " Message: " + response.message());
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_FAILURE", t.getMessage());
                Toast.makeText(getContext(), "Kết nối thất bại. Vui lòng kiểm tra mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean allCredentialsValid() {
        if (!isValidAddress()) return false;
        if (!isValidPhoneNumber()) return false;
        if (!isValidGender()) return false;
        if (!isValidCCCD()) return false;
        return true;
    }

    private boolean isValidCCCD() {
        if (EdtCCCD == null) return false;
        String cccd = EdtCCCD.getText().toString().trim();

        if (cccd.isEmpty()) {
            EdtCCCD.setError("Xin hãy nhập CCCD!");
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

    @ExternalCall
    public void resetForm() {
        EdtSearchPatient.setText("");
        EdtFullName.setText("");
        EdtBirthday.setText("");
        EdtAddress.setText("");
        EdtPhone.setText("");
        EdtCCCD.setText("");

        EdtSearchPatient.setError(null);
        EdtAddress.setError(null);
        EdtPhone.setError(null);
        EdtCCCD.setError(null);

        RgGender.clearCheck();
        selectedRadioButton = null;
        foundPatient = null;
    }


}