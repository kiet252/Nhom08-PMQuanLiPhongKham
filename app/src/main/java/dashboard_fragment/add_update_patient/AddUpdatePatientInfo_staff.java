package dashboard_fragment.add_update_patient;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;

import dashboard_fragment.add_update_patient.add_patient_logic.CreatePatientFragment;
import dashboard_fragment.add_update_patient.update_patient_logic.UpdatePatientFragment;

public class AddUpdatePatientInfo_staff extends AppCompatActivity {

    private MaterialButton BtnCreatePatient, BtnUpdatePatient, BtnSavePatientInfo, BtnDeleteCurrentFormAddCreatePatient;
    private ImageButton BtnBackAddUpdatePatient;
    private CreatePatientFragment CurrentCreatePatientFragment;
    private UpdatePatientFragment CurrentUpdatePatientFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_add_update_patient_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_patient), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupListeners();
        CreateNewPatientReplaceFragment();
    }

    private void initializeViews() {
        BtnCreatePatient = findViewById(R.id.btnCreatePatient);
        BtnUpdatePatient = findViewById(R.id.btnUpdatePatient);
        BtnSavePatientInfo = findViewById(R.id.btnSavePatientInfo);
        BtnBackAddUpdatePatient = findViewById(R.id.btnBackAddUpdatePatient);
        BtnDeleteCurrentFormAddCreatePatient = findViewById(R.id.btnDeleteCurrentFormAddCreatePatient);
    }

    private void setupListeners() {
        BtnCreatePatient.setOnClickListener(v -> CreateNewPatientReplaceFragment());
        BtnUpdatePatient.setOnClickListener(v -> UpdatePatientReplaceFragment());
        BtnSavePatientInfo.setOnClickListener(v -> SavePatientCurrentInfo());
        BtnBackAddUpdatePatient.setOnClickListener(v -> backToPreviousActivity());
        BtnDeleteCurrentFormAddCreatePatient.setOnClickListener(v -> resetCurrentForm());
    }

    private void CreateNewPatientReplaceFragment() {
        updateTabUI(true);
        CurrentCreatePatientFragment = new CreatePatientFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.patientFragmentContainer, CurrentCreatePatientFragment)
                .commit();
    }

    private void UpdatePatientReplaceFragment() {
        updateTabUI(false);
        CurrentUpdatePatientFragment = new UpdatePatientFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.patientFragmentContainer, CurrentUpdatePatientFragment)
                .commit();
    }

    private void SavePatientCurrentInfo() {
        Fragment currentVisibleFrag = getSupportFragmentManager().findFragmentById(R.id.patientFragmentContainer);

        if (currentVisibleFrag instanceof CreatePatientFragment) {
            ((CreatePatientFragment) currentVisibleFrag).submitPatientData();
        } else if (currentVisibleFrag instanceof UpdatePatientFragment) {
            ((UpdatePatientFragment) currentVisibleFrag).submitPatientData();
        }
    }
    private void updateTabUI(boolean isCreateSelected) {

        int colorWhite = Color.WHITE;
        int colorGray = Color.parseColor("#757575");
        if (isCreateSelected) {

            BtnCreatePatient.setBackgroundResource(R.drawable.bg_tab_selected);
            BtnCreatePatient.setTextColor(colorWhite);
            BtnCreatePatient.setIconTintResource(android.R.color.white);


            BtnUpdatePatient.setBackgroundResource(R.drawable.bg_tab_unselected);
            BtnUpdatePatient.setTextColor(colorGray);
            BtnUpdatePatient.setIconTintResource(android.R.color.darker_gray);
        } else {

            BtnUpdatePatient.setBackgroundResource(R.drawable.bg_tab_selected);
            BtnUpdatePatient.setTextColor(colorWhite);
            BtnUpdatePatient.setIconTintResource(android.R.color.white);


            BtnCreatePatient.setBackgroundResource(R.drawable.bg_tab_unselected);
            BtnCreatePatient.setTextColor(colorGray);
            BtnCreatePatient.setIconTintResource(android.R.color.darker_gray);
        }
    }
    private void backToPreviousActivity() {
        finish();
    }
    private void resetCurrentForm(){
        Fragment currentVisibleFrag = getSupportFragmentManager().findFragmentById(R.id.patientFragmentContainer);

        if (currentVisibleFrag instanceof CreatePatientFragment) {
            ((CreatePatientFragment) currentVisibleFrag).resetForm();
        } else if (currentVisibleFrag instanceof UpdatePatientFragment) {
            ((UpdatePatientFragment) currentVisibleFrag).resetForm();
        }
    }

}