package dashboard_fragment.add_update_patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;

import com.example.nhom08_quanlyphongkham.R;

public class AddUpdatePatientInfo_staff extends AppCompatActivity {

    private Button BtnCreatePatient, BtnUpdatePatient, BtnSavePatientInfo;
    private String currentToken;
    private CreatePatientFragment CurrentCreatePatientFragment;
    private UpdatePatientFragment CurrentUpdatePatientFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_update_patient_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_patient), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
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
    }

    private void setupListeners() {
        BtnCreatePatient.setOnClickListener(v -> CreateNewPatientReplaceFragment());
        BtnUpdatePatient.setOnClickListener(v -> UpdatePatientReplaceFragment());
        BtnSavePatientInfo.setOnClickListener(v -> SavePatientCurrentInfo());
    }

    private void CreateNewPatientReplaceFragment() {
        CurrentCreatePatientFragment = new CreatePatientFragment(currentToken);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.patientFragmentContainer, CurrentCreatePatientFragment)
                .commit();
    }

    private void UpdatePatientReplaceFragment() {
        CurrentUpdatePatientFragment = new UpdatePatientFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.patientFragmentContainer, CurrentUpdatePatientFragment)
                .commit();
    }

    private void SavePatientCurrentInfo() {
        Fragment currentVisibleFrag = getSupportFragmentManager().findFragmentById(R.id.patientFragmentContainer);

        if (currentVisibleFrag instanceof CreatePatientFragment) {
            ((CreatePatientFragment) currentVisibleFrag).saveData();
        } else if (currentVisibleFrag instanceof UpdatePatientFragment) {
            ((UpdatePatientFragment) currentVisibleFrag).saveData();
        }
    }

}