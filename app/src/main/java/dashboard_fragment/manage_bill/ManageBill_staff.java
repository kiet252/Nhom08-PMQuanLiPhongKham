package dashboard_fragment.manage_bill;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;

public class ManageBill_staff extends AppCompatActivity {

    ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_manage_bill);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_bill), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();
        setupListeners();
    }
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
    }
    private void setupListeners() {
        btnBack.setOnClickListener(v -> backToPreviousActivity());
    }
    private void backToPreviousActivity() {
        finish();
    }
}