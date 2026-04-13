package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;
import dashboard_fragment.add_update_patient.AddUpdatePatientInfo_staff;

public class HomeFragment_staff extends Fragment {

    private static final String ARG_TOKEN = "token";

    private String currentToken;
    private MaterialButton BtnCreateMedReport, BtnManageMedReport, BtnAddUpdatePatientInfo, BtnManageBill;

    public HomeFragment_staff() {
    }

    public static HomeFragment_staff newInstance(String token) {
        HomeFragment_staff fragment = new HomeFragment_staff();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentToken = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_staff, container, false);
        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        BtnCreateMedReport = view.findViewById(R.id.MBtnCreateMedReport);
        BtnManageMedReport = view.findViewById(R.id.MBtnManageMedReport);
        BtnAddUpdatePatientInfo = view.findViewById(R.id.MBtnAddUpdatePatientInfo);
        BtnManageBill = view.findViewById(R.id.MBtnManageBill);
    }

    private void setupListeners() {
        BtnAddUpdatePatientInfo.setOnClickListener(v -> startAddUpdatePatientIntent());
    }

    private void startAddUpdatePatientIntent() {
        Intent IntentAddUpdatePatient = new Intent(getContext(), AddUpdatePatientInfo_staff.class);

        IntentAddUpdatePatient.putExtra("accessToken", currentToken);

        startActivity(IntentAddUpdatePatient);
    }


}