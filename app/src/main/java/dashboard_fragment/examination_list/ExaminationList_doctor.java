package dashboard_fragment.examination_list;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;



public class ExaminationList_doctor extends AppCompatActivity {

    private ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_examination_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDoctorExaminationList), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        btnBack = findViewById(R.id.btnBackDoctorExaminationList);
        btnBack.setOnClickListener(v -> {
            finish();
        });

    }
}