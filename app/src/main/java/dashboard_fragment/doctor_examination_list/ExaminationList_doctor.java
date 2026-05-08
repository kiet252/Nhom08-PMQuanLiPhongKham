package dashboard_fragment.doctor_examination_list;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;

public class ExaminationList_doctor extends AppCompatActivity {
    private enum FilterTab { ALL, WAITING, IN_PROGRESS, DONE}
    private ImageButton btnBack;
    private LinearLayout btnFilterAllDoctorEx, btnFilterWaitingDoctorEx, btnFilterInProgressDoctorEx, btnFilterDoneDoctorEx;
    private TextView tvCountAllDoctorEx, tvCountWaitingDoctorEx, tvCountInProgressDoctorEx, tvCountDoneDoctorEx;
    private FilterTab selectedFilter = FilterTab.ALL;

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

        initializeViews();
        updateFilterSelection(FilterTab.ALL);
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBackDoctorExaminationList);

        btnFilterAllDoctorEx = findViewById(R.id.btnFilterAllDoctorEx);
        btnFilterWaitingDoctorEx = findViewById(R.id.btnFilterWaitingDoctorEx);
        btnFilterInProgressDoctorEx = findViewById(R.id.btnFilterInProgressDoctorEx);
        btnFilterDoneDoctorEx = findViewById(R.id.btnFilterDoneDoctorEx);
        
        tvCountAllDoctorEx = findViewById(R.id.tvCountAllDoctorEx);
        tvCountWaitingDoctorEx = findViewById(R.id.tvCountWaitingDoctorEx);
        tvCountInProgressDoctorEx = findViewById(R.id.tvCountInProgressDoctorEx);
        tvCountDoneDoctorEx = findViewById(R.id.tvCountDoneDoctorEx);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> backToPreviousActivity());
        btnFilterAllDoctorEx.setOnClickListener(v -> getAllExForms());
        btnFilterWaitingDoctorEx.setOnClickListener(v -> getAllWaitingExForms());
        btnFilterInProgressDoctorEx.setOnClickListener(v -> getAllInProgressExForms());
        btnFilterDoneDoctorEx.setOnClickListener(v -> getAllDoneExForms());
    }

    private void getAllExForms() {
        updateFilterSelection(FilterTab.ALL);
        //TODO: Get all examination forms logic here
    }

    private void getAllWaitingExForms() {
        updateFilterSelection(FilterTab.WAITING);
        //TODO: Get all waiting examination forms logic here
    }

    private void getAllInProgressExForms() {
        updateFilterSelection(FilterTab.IN_PROGRESS);
        //TODO: Get all in progress examination forms logic here
    }

    private void getAllDoneExForms() {
        updateFilterSelection(FilterTab.DONE);
        //TODO: Get all done examination forms logic here
    }

    private void updateFilterSelection(FilterTab filter) {
        selectedFilter = filter;

        setFilterSelected(btnFilterAllDoctorEx, tvCountAllDoctorEx, filter == FilterTab.ALL);
        setFilterSelected(btnFilterWaitingDoctorEx, tvCountWaitingDoctorEx, filter == FilterTab.WAITING);
        setFilterSelected(btnFilterInProgressDoctorEx, tvCountInProgressDoctorEx, filter == FilterTab.IN_PROGRESS);
        setFilterSelected(btnFilterDoneDoctorEx, tvCountDoneDoctorEx, filter == FilterTab.DONE);
    }

    private void setFilterSelected(LinearLayout button, TextView badge, boolean isSelected) {
        button.setBackgroundResource(isSelected
                ? R.drawable.bg_filter_tab_selected
                : R.drawable.bg_filter_tab_unselected);

        badge.setBackgroundResource(isSelected
                ? R.drawable.bg_count_badge_selected
                : R.drawable.bg_count_badge);

        int labelColor = ContextCompat.getColor(this, isSelected
                ? android.R.color.white
                : R.color.filter_tab_unselected_text);
        int badgeTextColor = ContextCompat.getColor(this, isSelected
                ? android.R.color.white
                : R.color.filter_tab_unselected_badge_text);

        for (int i = 0; i < button.getChildCount(); i++) {
            if (button.getChildAt(i) instanceof TextView) {
                ((TextView) button.getChildAt(i)).setTextColor(labelColor);
            }
        }

        badge.setTextColor(badgeTextColor);
    }

    private void backToPreviousActivity() {
        finish();
    }
}
