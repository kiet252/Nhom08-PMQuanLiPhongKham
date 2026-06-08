package dashboard_fragment.admin_manage_medicine_clinical;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageMedicineClinicalActivity extends BaseActivity {

    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView rvItems;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyText;
    private FloatingActionButton fabAdd;

    private AdminMedClsApiService apiService;
    private int currentTab = 0; // 0 = Medicine, 1 = CLS

    // Medicine lists
    private List<MedicineItem> medicineListOriginal = new ArrayList<>();
    private List<MedicineItem> medicineListFiltered = new ArrayList<>();
    private AdminMedicineAdapter medicineAdapter;

    // CLS lists
    private List<ClinicalItem> clinicalListOriginal = new ArrayList<>();
    private List<ClinicalItem> clinicalListFiltered = new ArrayList<>();
    private AdminClinicalAdapter clinicalAdapter;

    // Activity result launcher to reload lists after add/update
    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (currentTab == 0) {
                        loadMedicines();
                    } else {
                        loadClinicalItems();
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_medicine_clinical);

        // Bind Views
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
        rvItems = findViewById(R.id.rvItems);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        fabAdd = findViewById(R.id.fabAdd);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup API Service
        apiService = SupabaseClientProvider.getClient(this).create(AdminMedClsApiService.class);

        // Setup RecyclerView
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        // Setup Adapters
        medicineAdapter = new AdminMedicineAdapter(medicineListFiltered);
        medicineAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AdminManageMedicineClinicalActivity.this, AdminMedicineDetailActivity.class);
            intent.putExtra("id", item.getId());
            formLauncher.launch(intent);
        });

        clinicalAdapter = new AdminClinicalAdapter(clinicalListFiltered);
        clinicalAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AdminManageMedicineClinicalActivity.this, AdminClinicalDetailActivity.class);
            intent.putExtra("id", item.getId());
            formLauncher.launch(intent);
        });

        // Default setup: Medicine tab active
        rvItems.setAdapter(medicineAdapter);
        loadMedicines();
        loadClinicalItems(); // Pre-load CLS items in background

        // Setup Tab Layout Switcher
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                etSearch.setText(""); // clear search on tab change
                if (currentTab == 0) {
                    etSearch.setHint("Tìm theo tên thuốc, hoạt chất...");
                    rvItems.setAdapter(medicineAdapter);
                    filterItems("");
                } else {
                    etSearch.setHint("Tìm theo tên, loại dịch vụ...");
                    rvItems.setAdapter(clinicalAdapter);
                    filterItems("");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // FAB Clicked
        fabAdd.setOnClickListener(v -> {
            if (currentTab == 0) {
                Intent intent = new Intent(AdminManageMedicineClinicalActivity.this, AdminMedicineDetailActivity.class);
                formLauncher.launch(intent);
            } else {
                Intent intent = new Intent(AdminManageMedicineClinicalActivity.this, AdminClinicalDetailActivity.class);
                formLauncher.launch(intent);
            }
        });
    }

    private void loadMedicines() {
        apiService.getMedicines(
                "id,ten_thuoc,hoat_chat,ham_luong,don_vi,ton_kho,chuc_nang,don_gia",
                "id.asc"
        ).enqueue(new Callback<List<MedicineItem>>() {
            @Override
            public void onResponse(Call<List<MedicineItem>> call, Response<List<MedicineItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicineListOriginal.clear();
                    medicineListOriginal.addAll(response.body());
                    if (currentTab == 0) {
                        filterItems(etSearch.getText().toString());
                    }
                } else {
                    Toast.makeText(AdminManageMedicineClinicalActivity.this, "Không thể tải danh sách thuốc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MedicineItem>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi tải thuốc: " + t.getMessage());
                Toast.makeText(AdminManageMedicineClinicalActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClinicalItems() {
        apiService.getClinicalItems(
                "id,ten_dich_vu,loai_dich_vu,mo_ta,don_gia",
                "id.asc"
        ).enqueue(new Callback<List<ClinicalItem>>() {
            @Override
            public void onResponse(Call<List<ClinicalItem>> call, Response<List<ClinicalItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clinicalListOriginal.clear();
                    clinicalListOriginal.addAll(response.body());
                    if (currentTab == 1) {
                        filterItems(etSearch.getText().toString());
                    }
                } else {
                    Toast.makeText(AdminManageMedicineClinicalActivity.this, "Không thể tải danh sách dịch vụ CLS", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClinicalItem>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi tải dịch vụ CLS: " + t.getMessage());
            }
        });
    }

    private void filterItems(String query) {
        String queryNorm = normalizeString(query);

        if (currentTab == 0) {
            medicineListFiltered.clear();
            if (queryNorm.isEmpty()) {
                medicineListFiltered.addAll(medicineListOriginal);
            } else {
                for (MedicineItem item : medicineListOriginal) {
                    String name = normalizeString(item.getTen_thuoc());
                    String active = normalizeString(item.getHoat_chat());
                    if (name.contains(queryNorm) || active.contains(queryNorm)) {
                        medicineListFiltered.add(item);
                    }
                }
            }
            medicineAdapter.notifyDataSetChanged();
            updateUI(medicineListFiltered.isEmpty(), "Không tìm thấy thuốc phù hợp");
        } else {
            clinicalListFiltered.clear();
            if (queryNorm.isEmpty()) {
                clinicalListFiltered.addAll(clinicalListOriginal);
            } else {
                for (ClinicalItem item : clinicalListOriginal) {
                    String name = normalizeString(item.getTen_dich_vu());
                    String type = normalizeString(item.getLoai_dich_vu());
                    if (name.contains(queryNorm) || type.contains(queryNorm)) {
                        clinicalListFiltered.add(item);
                    }
                }
            }
            clinicalAdapter.notifyDataSetChanged();
            updateUI(clinicalListFiltered.isEmpty(), "Không tìm thấy dịch vụ CLS phù hợp");
        }
    }

    private String normalizeString(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
    }

    private void updateUI(boolean isEmpty, String emptyMessage) {
        if (isEmpty) {
            rvItems.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyText.setText(emptyMessage);
        } else {
            rvItems.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
