package dashboard_fragment.add_update_patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

import dashboard_fragment.add_update_patient.add_patient_logic.CreatePatientFragment;
import dashboard_fragment.add_update_patient.update_patient_logic.UpdatePatientFragment;

public class AddUpdatePatientInfo_staff extends AppCompatActivity {

    private Button BtnCreatePatient, BtnUpdatePatient, BtnSavePatientInfo, BtnExitAddCreatePatient;
    private ImageButton BtnBackAddUpdatePatient;
    private String currentToken;
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
        getIntentInfo();
        CreateNewPatientReplaceFragment();
        setupListeners();
    }

    private void getIntentInfo() {
        Intent currentIntent = getIntent();
        currentToken = currentIntent.getStringExtra("accessToken");
    }
    private void initializeViews() {
        BtnCreatePatient = findViewById(R.id.btnCreatePatient);
        BtnUpdatePatient = findViewById(R.id.btnUpdatePatient);
        BtnSavePatientInfo = findViewById(R.id.btnSavePatientInfo);
        BtnBackAddUpdatePatient = findViewById(R.id.btnBackAddUpdatePatient);
        BtnExitAddCreatePatient = findViewById(R.id.btnExitAddCreatePatient);
    }

    private void setupListeners() {
        BtnCreatePatient.setOnClickListener(v -> CreateNewPatientReplaceFragment());
        BtnUpdatePatient.setOnClickListener(v -> UpdatePatientReplaceFragment());
        BtnSavePatientInfo.setOnClickListener(v -> SavePatientCurrentInfo());
        BtnBackAddUpdatePatient.setOnClickListener(v -> backToPreviousActivity());
        BtnExitAddCreatePatient.setOnClickListener(v -> backToPreviousActivity());
    }

    private void CreateNewPatientReplaceFragment() {
        CurrentCreatePatientFragment = new CreatePatientFragment(currentToken);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.patientFragmentContainer, CurrentCreatePatientFragment)
                .commit();
    }

    private void UpdatePatientReplaceFragment() {
        CurrentUpdatePatientFragment = new UpdatePatientFragment(currentToken);
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

    private void backToPreviousActivity() {
        finish();
    }

}