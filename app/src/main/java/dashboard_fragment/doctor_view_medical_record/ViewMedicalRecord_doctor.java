package dashboard_fragment.doctor_view_medical_record;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

public class ViewMedicalRecord_doctor extends AppCompatActivity {
    private EditText edtMedicalRecordSearch;
    private MaterialButton btnMedicalRecordSearch;
    private ChipGroup chipGroupRecentSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_view_medical_record);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_medical_record), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();
        setupBackButton();
    }
    private void initializeViews() {
        edtMedicalRecordSearch = findViewById(R.id.edtMedicalRecordSearch);
        btnMedicalRecordSearch = findViewById(R.id.btnMedicalRecordSearch);
        chipGroupRecentSearch = findViewById(R.id.chipGroupRecentSearch);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBackDoctorMedicalRecord);
        btnBack.setOnClickListener(v -> finish());
    }
}