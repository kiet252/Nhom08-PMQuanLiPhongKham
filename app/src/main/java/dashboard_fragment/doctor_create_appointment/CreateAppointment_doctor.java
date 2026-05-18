package dashboard_fragment.doctor_create_appointment;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;

public class CreateAppointment_doctor extends AppCompatActivity {

    private ImageButton BtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doctor_create_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_appointment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupBackButton();
    }
    private void setupBackButton() {
        BtnBack = findViewById(R.id.btnBack);
        BtnBack.setOnClickListener(v -> finish());
    }
}