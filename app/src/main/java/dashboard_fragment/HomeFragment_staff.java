package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;

import coil.Coil;
import coil.request.ImageRequest;
import dashboard_fragment.staff_add_update_patient.AddUpdatePatientInfo_staff;
import dashboard_fragment.staff_create_examination_form.CreateExaminationForm_staff;
import dashboard_fragment.staff_manage_bill.ManageBill_staff;
import dashboard_fragment.staff_manage_examination_form.ManageExaminationForm_staff;

public class HomeFragment_staff extends Fragment {

    private static final String ARG_TOKEN = "token";

    private String currentToken;
    private CardView BtnCreateExForm, BtnManageMedReport, BtnAddUpdatePatientInfo, BtnManageBill;

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
        // Trong HomeFragment_staff.java -> onCreateView
        TextView tvName = view.findViewById(R.id.staff_home_name);
        SetAvatar(view, SharedPrefManager.getInstance(requireContext()).getProfile());
        UserProfile profile = SharedPrefManager.getInstance(requireContext()).getProfile();
        if (profile != null && profile.getHo_ten() != null) {
            tvName.setText(profile.getHo_ten()); // Thay chữ "Lễ tân" bằng "Khoa"
        }
        return view;
    }

    private void initializeViews(View view) {
        BtnCreateExForm = view.findViewById(R.id.CardViewCreateExaminationForm);
        BtnManageMedReport = view.findViewById(R.id.CardViewManageMedReport);
        BtnAddUpdatePatientInfo = view.findViewById(R.id.CardViewAddUpdatePatientInfo);
        BtnManageBill = view.findViewById(R.id.CardViewManageBill);
    }

    private void setupListeners() {
        BtnAddUpdatePatientInfo.setOnClickListener(v -> startAddUpdatePatientIntent());
        BtnCreateExForm.setOnClickListener(v -> startCreateExaminationFormIntent());
        BtnManageMedReport.setOnClickListener(v -> startManageExaminationFormIntent());
        BtnManageBill.setOnClickListener(v-> startManageBillIntent());
    }
    public void SetAvatar(View view, UserProfile userprofile)
    {
        ImageView avatar;
        avatar = view.findViewById(R.id.home_avatar_staff);
        ImageRequest request = new ImageRequest.Builder(getContext())
                .data(userprofile.getAnh_dai_dien())
                .target(avatar)
                .crossfade(true) // Hiệu ứng mờ dần khi hiện ảnh
                .build();

        Coil.imageLoader(getContext()).enqueue(request);
    }
    private void startAddUpdatePatientIntent() {
        Intent IntentAddUpdatePatient = new Intent(getContext(), AddUpdatePatientInfo_staff.class);

        IntentAddUpdatePatient.putExtra("accessToken", currentToken);

        startActivity(IntentAddUpdatePatient);
    }
    private void startCreateExaminationFormIntent()
    {
        Intent IntentCreateExaminationForm = new Intent(getContext(), CreateExaminationForm_staff.class);

        IntentCreateExaminationForm.putExtra("accessToken", currentToken);

        startActivity(IntentCreateExaminationForm);
    }

    private void startManageExaminationFormIntent() {
        Intent IntentManageExaminationForm = new Intent(getContext(), ManageExaminationForm_staff.class);

        IntentManageExaminationForm.putExtra("accessToken", currentToken);

        startActivity(IntentManageExaminationForm);
    }

    private void startManageBillIntent() {
        Intent IntentManageBill= new Intent(getContext(), ManageBill_staff.class);

        IntentManageBill.putExtra("accessToken", currentToken);

        startActivity(IntentManageBill);
    }
}