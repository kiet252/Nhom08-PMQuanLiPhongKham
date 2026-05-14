package dashboard_fragment.doctor_examination_list;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.DoctorExaminationStatus;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.ExaminationFormDetail_doctor;
import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;

public class ExaminationList_doctor extends AppCompatActivity {
    private enum FilterTab { ALL, WAITING, IN_PROGRESS, DONE }

    private ImageButton btnBack;
    private LinearLayout btnFilterAllDoctorEx;
    private LinearLayout btnFilterWaitingDoctorEx;
    private LinearLayout btnFilterInProgressDoctorEx;
    private LinearLayout btnFilterDoneDoctorEx;
    private TextView tvCountAllDoctorEx;
    private TextView tvCountWaitingDoctorEx;
    private TextView tvCountInProgressDoctorEx;
    private TextView tvCountDoneDoctorEx;
    private TextInputEditText edtDoctorExaminationSearch;
    private RecyclerView rvDoctorExaminationList;
    private FilterTab selectedFilter = FilterTab.ALL;
    private ExaminationFormRepository repository;
    private DoctorExaminationGroupAdapter groupAdapter;
    private List<ExaminationFormWithPatientDto> allForms = new ArrayList<>();
    private List<ExaminationFormWithPatientDto> resultExaminationFormsAndPatients = new ArrayList<>();
    private final Map<String, List<ExaminationFormWithPatientDto>> formsByDate = new LinkedHashMap<>();
    private String currentSearchQuery = "";

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

        repository = new ExaminationFormRepository(this);
        loadAllFormsAndPatientDto();
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

        edtDoctorExaminationSearch = findViewById(R.id.edtDoctorExaminationSearch);

        rvDoctorExaminationList = findViewById(R.id.rvDoctorExaminationList);
        rvDoctorExaminationList.setLayoutManager(new LinearLayoutManager(this));

        groupAdapter = new DoctorExaminationGroupAdapter(this, this::openExaminationFormDetails);
        rvDoctorExaminationList.setAdapter(groupAdapter);
    }

    private final ActivityResultLauncher<Intent> ExaminationFormLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadAllFormsAndPatientDto();
                }
            }
    );

    private void setupListeners() {
        btnBack.setOnClickListener(v -> backToPreviousActivity());
        btnFilterAllDoctorEx.setOnClickListener(v -> getAllExForms());
        btnFilterWaitingDoctorEx.setOnClickListener(v -> getAllWaitingExForms());
        btnFilterInProgressDoctorEx.setOnClickListener(v -> getAllInProgressExForms());
        btnFilterDoneDoctorEx.setOnClickListener(v -> getAllDoneExForms());

        edtDoctorExaminationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s == null ? "" : s.toString().trim();
                updateCounts(applySearchFilter(new ArrayList<>(allForms)));
                applySelectedFilter();
            }
        });
    }

    public void loadAllFormsAndPatientDto() {
        UserProfile doctorProfile = SharedPrefManager.getInstance(this).getProfile();
        String currentDoctorId = doctorProfile.getID();

        repository.getAllFormsToday(currentDoctorId).enqueue(new retrofit2.Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call,
                                   @NonNull retrofit2.Response<List<ExaminationFormWithPatientDto>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        allForms = buildDisplayForms(response.body());
                        resultExaminationFormsAndPatients = new ArrayList<>(allForms);
                        updateCounts(allForms);
                        applySelectedFilter();
                    } else {
                        allForms.clear();
                        resultExaminationFormsAndPatients.clear();
                        updateCounts(resultExaminationFormsAndPatients);
                        refreshExaminationFormsList();
                        Toast.makeText(ExaminationList_doctor.this, "Khong tim thay phieu kham!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        Toast.makeText(ExaminationList_doctor.this, "Loi: " + response.code() + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull Throwable t) {
                Toast.makeText(ExaminationList_doctor.this, "Loi ket noi khi tai phieu kham: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<ExaminationFormWithPatientDto> buildDisplayForms(List<ExaminationFormWithPatientDto> forms) {
        List<ExaminationFormWithPatientDto> displayForms = new ArrayList<>();
        for (ExaminationFormWithPatientDto form : forms) {
            if (shouldIncludeForm(form)) {
                displayForms.add(form);
            }
        }

        Collections.sort(displayForms, new Comparator<ExaminationFormWithPatientDto>() {
            @Override
            public int compare(ExaminationFormWithPatientDto first, ExaminationFormWithPatientDto second) {
                int dateComparison = compareDatesDesc(first.getNgay_kham(), second.getNgay_kham());
                if (dateComparison != 0) {
                    return dateComparison;
                }
                return compareTimesAsc(first.getGio_du_kien(), second.getGio_du_kien());
            }
        });

        return displayForms;
    }

    private boolean shouldIncludeForm(ExaminationFormWithPatientDto form) {
        Date formDate = form.getNgay_kham();
        if (formDate == null) {
            return false;
        }

        if (isSameDay(formDate, new Date())) {
            return true;
        }

        return isBeforeToday(formDate) && hasStatus(form, DoctorExaminationStatus.IN_PROGRESS);
    }

    private boolean isSameDay(Date first, Date second) {
        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(first);

        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTime(second);

        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)
                && firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isBeforeToday(Date date) {
        Calendar formCalendar = Calendar.getInstance();
        formCalendar.setTime(date);
        clearTime(formCalendar);

        Calendar todayCalendar = Calendar.getInstance();
        clearTime(todayCalendar);

        return formCalendar.before(todayCalendar);
    }

    private int compareDatesDesc(Date first, Date second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return second.compareTo(first);
    }

    private int compareTimesAsc(String firstTime, String secondTime) {
        return normalizeTime(firstTime).compareTo(normalizeTime(secondTime));
    }

    private String normalizeTime(String time) {
        return time == null ? "" : time;
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void getAllExForms() {
        updateFilterSelection(FilterTab.ALL);
        resultExaminationFormsAndPatients = applySearchFilter(new ArrayList<>(allForms));
        refreshExaminationFormsList();
    }

    private void getAllWaitingExForms() {
        updateFilterSelection(FilterTab.WAITING);
        resultExaminationFormsAndPatients = applySearchFilter(filterFormsByStatus(DoctorExaminationStatus.WAITING));
        refreshExaminationFormsList();
    }

    private void getAllInProgressExForms() {
        updateFilterSelection(FilterTab.IN_PROGRESS);
        resultExaminationFormsAndPatients = applySearchFilter(filterFormsByStatus(DoctorExaminationStatus.IN_PROGRESS));
        refreshExaminationFormsList();
    }

    private void getAllDoneExForms() {
        updateFilterSelection(FilterTab.DONE);
        resultExaminationFormsAndPatients = applySearchFilter(filterFormsByStatus(DoctorExaminationStatus.DONE));
        refreshExaminationFormsList();
    }

    private List<ExaminationFormWithPatientDto> filterFormsByStatus(DoctorExaminationStatus status) {
        List<ExaminationFormWithPatientDto> filteredForms = new ArrayList<>();
        for (ExaminationFormWithPatientDto form : allForms) {
            if (hasStatus(form, status)) {
                filteredForms.add(form);
            }
        }
        return filteredForms;
    }

    private List<ExaminationFormWithPatientDto> applySearchFilter(List<ExaminationFormWithPatientDto> sourceForms) {
        if (currentSearchQuery.isEmpty()) {
            return sourceForms;
        }

        String normalizedQuery = currentSearchQuery.toLowerCase(Locale.getDefault());
        List<ExaminationFormWithPatientDto> filteredForms = new ArrayList<>();

        for (ExaminationFormWithPatientDto form : sourceForms) {
            String patientId = form.getPatient_id();
            String patientName = form.getPatient() != null ? form.getPatient().getHo_ten() : null;

            boolean matchesPatientId = patientId != null
                    && patientId.toLowerCase(Locale.getDefault()).contains(normalizedQuery);
            boolean matchesPatientName = patientName != null
                    && patientName.toLowerCase(Locale.getDefault()).contains(normalizedQuery);

            if (matchesPatientId || matchesPatientName) {
                filteredForms.add(form);
            }
        }

        return filteredForms;
    }

    private void applySelectedFilter() {
        switch (selectedFilter) {
            case WAITING:
                getAllWaitingExForms();
                break;
            case IN_PROGRESS:
                getAllInProgressExForms();
                break;
            case DONE:
                getAllDoneExForms();
                break;
            case ALL:
            default:
                getAllExForms();
                break;
        }
    }

    private void refreshExaminationFormsList() {
        formsByDate.clear();
        listExaminationFormsByDates();

        List<DoctorExaminationDateGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<ExaminationFormWithPatientDto>> entry : formsByDate.entrySet()) {
            groups.add(new DoctorExaminationDateGroup(entry.getKey(), entry.getValue()));
        }

        groupAdapter.submitList(groups);
    }

    private void listExaminationFormsByDates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (ExaminationFormWithPatientDto form : resultExaminationFormsAndPatients) {
            if (form.getNgay_kham() == null) {
                continue;
            }
            String key = dateFormat.format(form.getNgay_kham());
            formsByDate.computeIfAbsent(key, unused -> new ArrayList<>()).add(form);
        }
    }

    private void updateCounts(List<ExaminationFormWithPatientDto> sourceForms) {
        int waitingCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;

        for (ExaminationFormWithPatientDto form : sourceForms) {
            if (hasStatus(form, DoctorExaminationStatus.WAITING)) {
                waitingCount++;
            } else if (hasStatus(form, DoctorExaminationStatus.IN_PROGRESS)) {
                inProgressCount++;
            } else if (hasStatus(form, DoctorExaminationStatus.DONE)) {
                doneCount++;
            }
        }

        tvCountAllDoctorEx.setText(String.valueOf(sourceForms.size()));
        tvCountWaitingDoctorEx.setText(String.valueOf(waitingCount));
        tvCountInProgressDoctorEx.setText(String.valueOf(inProgressCount));
        tvCountDoneDoctorEx.setText(String.valueOf(doneCount));
    }

    private boolean hasStatus(ExaminationFormWithPatientDto form, DoctorExaminationStatus status) {
        return DoctorExaminationStatus.fromValue(form.getTrang_thai()) == status;
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

    private void openExaminationFormDetails(ExaminationFormWithPatientDto form) {
        Intent intent = ExaminationFormDetail_doctor.createIntent(this, form);
        ExaminationFormLauncher.launch(intent);
    }
}
