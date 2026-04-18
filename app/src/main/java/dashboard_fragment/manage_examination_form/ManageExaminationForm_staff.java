package dashboard_fragment.manage_examination_form;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dashboard_fragment.add_update_patient.PatientProfile;
import dashboard_fragment.create_examination_form.CreateExaminationForm_staff;
import dashboard_fragment.create_examination_form.ExaminationForm;
import dashboard_fragment.create_examination_form.ExaminationFormRepository;
import retrofit2.Call;

public class ManageExaminationForm_staff extends AppCompatActivity {
    Button BtnSearch, BtnFilter, BtnSort;
    EditText EdtSearch;
    RecyclerView RvExaminationsList;
    ExaminationFormGroupAdapter groupAdapter;
    String currentToken;
    ExaminationFormRepository repository;

    List<ExaminationForm> ResultExaminationForms;
    Map<String, List<ExaminationForm>> formsByDate = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.staff_manage_examination_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_examination), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        getIntentInfo();
        setupListeners();
        setupRecycler();

        repository = new ExaminationFormRepository(getString(R.string.abAIkey));
    }

    private void initializeViews() {
        BtnSearch = findViewById(R.id.btnManageExFormSearch);
        BtnFilter = findViewById(R.id.btnManageExFormFilterDoctor);
        BtnSort = findViewById(R.id.btnManageExFormSort);

        EdtSearch = findViewById(R.id.edtManageExFormSearch);

        RvExaminationsList = findViewById(R.id.rvManageExFormExaminationsList);
    }

    private void setupListeners() {
        BtnSearch.setOnClickListener(v -> findExaminationFormsByPatientCCCDOrID());
    }

    private void getIntentInfo() {
        Intent currentIntent = getIntent();
        currentToken = currentIntent.getStringExtra("accessToken");
    }

    private void setupRecycler() {
        RvExaminationsList.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new ExaminationFormGroupAdapter(this, currentToken);
        RvExaminationsList.setAdapter(groupAdapter);
    }

    private void findExaminationFormsByPatientCCCDOrID() {
        if (EdtSearch == null) return;

        String searchValue = EdtSearch.getText().toString().trim();

        if (searchValue.isEmpty()) {
            EdtSearch.setError("Vui lòng nhập CCCD hoặc ID cần tìm!");
            EdtSearch.requestFocus();
            return;
        }

        repository.getAllFormsByPatientCCCDOrID(currentToken, searchValue).enqueue(new retrofit2.Callback<List<ExaminationForm>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationForm>> call, @NonNull retrofit2.Response<List<ExaminationForm>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        Toast.makeText(ManageExaminationForm_staff.this, "Tìm thành công!", Toast.LENGTH_SHORT).show();

                        ResultExaminationForms = response.body();
                        refreshExaminationFormsList();
                    } else {
                        Toast.makeText(ManageExaminationForm_staff.this, "Không tìm thấy bệnh nhân!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Toast.makeText(ManageExaminationForm_staff.this, "Lỗi: " + response.code() + (response.errorBody()).string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationForm>> call, @NonNull Throwable t) {
                Toast.makeText(ManageExaminationForm_staff.this, "Lỗi kết nối khi tải bệnh nhân: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshExaminationFormsList() {
        formsByDate.clear();

        listExaminationFormsByDates();

        List<ExaminationFormGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<ExaminationForm>> entry : formsByDate.entrySet()) {
            groups.add(new ExaminationFormGroup(entry.getKey(), entry.getValue()));
        }

        groupAdapter.submitList(groups);
    }

    private void listExaminationFormsByDates() {
        for (ExaminationForm form : ResultExaminationForms) {
            String key = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(form.getNgay_kham());
            formsByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(form);
        }
    }
}