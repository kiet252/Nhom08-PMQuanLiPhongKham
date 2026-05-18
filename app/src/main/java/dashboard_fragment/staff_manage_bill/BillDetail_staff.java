package dashboard_fragment.staff_manage_bill;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;


public class BillDetail_staff extends AppCompatActivity {

    private ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_bill_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_bill_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnStaffBillBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> backToPreviousActivity());
    }

    private void backToPreviousActivity() {
        finish();
    }
}