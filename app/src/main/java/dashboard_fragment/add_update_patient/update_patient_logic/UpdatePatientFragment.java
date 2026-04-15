package dashboard_fragment.add_update_patient.update_patient_logic;

import android.os.Bundle;
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

import dashboard_fragment.add_update_patient.ExternalCall;

public class UpdatePatientFragment extends Fragment {
    private EditText EdtSearchPatient, EdtFullName, EdtBirthday, EdtAddress, EdtPhone;
    private TextInputLayout TilSearchPatientLayout;
    private RadioGroup RgGender;
    private RadioButton selectedRadioButton;
    private String currentToken;
    public UpdatePatientFragment(String token) {currentToken = token;}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.staff_fragment_update_patient, container, false);

        initializeViews(view);
        setupListeners();

        return view;
    }
    private void initializeViews(View view) {
        EdtSearchPatient = view.findViewById(R.id.edtUpdatePatientSearchPatient);

        TilSearchPatientLayout = view.findViewById(R.id.tilUpdatePatientSearchPatientLayout);

        EdtFullName = view.findViewById(R.id.edtUpdatePatientFullName);
        EdtBirthday = view.findViewById(R.id.edtUpdatePatientBirthday);
        EdtAddress = view.findViewById(R.id.edtUpdatePatientAddress);
        EdtPhone = view.findViewById(R.id.edtUpdatePatientPhone);
    }
    private void setupListeners() {
        TilSearchPatientLayout.setEndIconOnClickListener(v -> sendPatientSearchRequest());
    }

    private void sendPatientSearchRequest() {
        Toast.makeText(requireContext(), "I am searching!", Toast.LENGTH_SHORT).show();
    }
    @ExternalCall
    public void submitPatientData() {
        Toast.makeText(getContext(), "Update Patient Logic here!", Toast.LENGTH_SHORT).show();
    }


}