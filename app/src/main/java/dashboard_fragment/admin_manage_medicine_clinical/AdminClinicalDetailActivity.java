package dashboard_fragment.admin_manage_medicine_clinical;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminClinicalDetailActivity extends BaseActivity {

    private TextInputEditText etClinicalName;
    private AutoCompleteTextView actvClinicalType;
    private TextInputEditText etClinicalDesc;
    private TextInputEditText etClinicalPrice;
    private MaterialButton btnSave;

    private AdminMedClsApiService apiService;
    private int clinicalId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_clinical_detail);

        // Bind Views
        etClinicalName = findViewById(R.id.etClinicalName);
        actvClinicalType = findViewById(R.id.actvClinicalType);
        etClinicalDesc = findViewById(R.id.etClinicalDesc);
        etClinicalPrice = findViewById(R.id.etClinicalPrice);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Dropdown for clinical types
        String[] clinicalTypes = {"Xét nghiệm máu", "Siêu âm", "Chẩn đoán hình ảnh", "Nội soi", "Thính học"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, clinicalTypes);
        actvClinicalType.setAdapter(typeAdapter);

        // Setup API Service
        apiService = SupabaseClientProvider.getClient(this).create(AdminMedClsApiService.class);

        // Check Intent for Edit mode
        if (getIntent().hasExtra("id")) {
            clinicalId = getIntent().getIntExtra("id", -1);
            if (clinicalId != -1) {
                isEditMode = true;
                setupEditMode();
            }
        }

        btnSave.setOnClickListener(v -> saveClinicalItem());
    }

    private void setupEditMode() {
        // Change text titles for update form
        findViewById(R.id.tvHeaderTitle).post(() -> {
            ((android.widget.TextView) findViewById(R.id.tvHeaderTitle)).setText("Cập nhật dịch vụ CLS");
            ((android.widget.TextView) findViewById(R.id.tvBannerTitle)).setText("Cập nhật dịch vụ CLS");
            ((android.widget.TextView) findViewById(R.id.tvBannerSubtitle)).setText("Cập nhật đơn giá và thông tin");
        });

        // Load clinical item details
        apiService.getClinicalItemById("eq." + clinicalId, "*").enqueue(new Callback<List<ClinicalItem>>() {
            @Override
            public void onResponse(Call<List<ClinicalItem>> call, Response<List<ClinicalItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ClinicalItem item = response.body().get(0);
                    populateFields(item);
                } else {
                    Toast.makeText(AdminClinicalDetailActivity.this, "Không thể tải chi tiết dịch vụ", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<ClinicalItem>> call, Throwable t) {
                Toast.makeText(AdminClinicalDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(ClinicalItem item) {
        etClinicalName.setText(item.getTen_dich_vu());
        actvClinicalType.setText(item.getLoai_dich_vu(), false);
        etClinicalDesc.setText(item.getMo_ta());

        double price = item.getDon_gia() != null ? item.getDon_gia() : 0.0;
        etClinicalPrice.setText(String.format(java.util.Locale.US, "%.0f", price));
    }

    private void saveClinicalItem() {
        String name = etClinicalName.getText() != null ? etClinicalName.getText().toString().trim() : "";
        String type = actvClinicalType.getText() != null ? actvClinicalType.getText().toString().trim() : "";
        String desc = etClinicalDesc.getText() != null ? etClinicalDesc.getText().toString().trim() : "";
        String priceStr = etClinicalPrice.getText() != null ? etClinicalPrice.getText().toString().trim() : "";

        // Validations
        if (name.isEmpty()) {
            etClinicalName.setError("Tên dịch vụ không được để trống");
            etClinicalName.requestFocus();
            return;
        }
        if (type.isEmpty()) {
            actvClinicalType.setError("Loại dịch vụ không được để trống");
            actvClinicalType.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            etClinicalPrice.setError("Đơn giá không được để trống");
            etClinicalPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                etClinicalPrice.setError("Đơn giá phải >= 0");
                etClinicalPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etClinicalPrice.setError("Đơn giá phải là số hợp lệ");
            etClinicalPrice.requestFocus();
            return;
        }

        // Prepare request body
        Map<String, Object> body = new HashMap<>();
        body.put("ten_dich_vu", name);
        body.put("loai_dich_vu", type);
        body.put("mo_ta", desc);
        body.put("don_gia", price);

        if (isEditMode) {
            // Update
            apiService.updateClinicalItem("eq." + clinicalId, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminClinicalDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AdminClinicalDetailActivity.this, "Cập nhật thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AdminClinicalDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new
            apiService.createClinicalItem(body).enqueue(new Callback<List<ClinicalItem>>() {
                @Override
                public void onResponse(Call<List<ClinicalItem>> call, Response<List<ClinicalItem>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminClinicalDetailActivity.this, "Thêm mới dịch vụ thành công", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AdminClinicalDetailActivity.this, "Thêm mới thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<ClinicalItem>> call, Throwable t) {
                    Toast.makeText(AdminClinicalDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
