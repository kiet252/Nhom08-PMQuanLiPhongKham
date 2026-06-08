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

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMedicineDetailActivity extends BaseActivity {

    private TextInputEditText etMedName;
    private TextInputEditText etMedActive;
    private AutoCompleteTextView actvMedUnit;
    private TextInputEditText etMedStock;
    private TextInputEditText etMedPrice;
    private MaterialButton btnSave;

    private AdminMedClsApiService apiService;
    private int medicineId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medicine_detail);

        // Bind Views
        etMedName = findViewById(R.id.etMedName);
        etMedActive = findViewById(R.id.etMedActive);
        actvMedUnit = findViewById(R.id.actvMedUnit);
        etMedStock = findViewById(R.id.etMedStock);
        etMedPrice = findViewById(R.id.etMedPrice);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Dropdown for units
        String[] units = {"Viên", "Chai", "Hộp", "Ống", "Vỉ", "Gói", "Tuýp", "Cái", "Viên nén", "Viên sủi"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, units);
        actvMedUnit.setAdapter(unitAdapter);

        // Setup API Service
        apiService = SupabaseClientProvider.getClient(this).create(AdminMedClsApiService.class);

        // Check Intent for Edit mode
        if (getIntent().hasExtra("id")) {
            medicineId = getIntent().getIntExtra("id", -1);
            if (medicineId != -1) {
                isEditMode = true;
                setupEditMode();
            }
        }

        btnSave.setOnClickListener(v -> saveMedicine());
    }

    private void setupEditMode() {
        // Change text titles for update form
        findViewById(R.id.tvHeaderTitle).post(() -> {
            ((android.widget.TextView) findViewById(R.id.tvHeaderTitle)).setText("Cập nhật thông tin");
            ((android.widget.TextView) findViewById(R.id.tvBannerTitle)).setText("Cập nhật thông tin");
            ((android.widget.TextView) findViewById(R.id.tvBannerSubtitle)).setText("Cập nhật đơn giá và tồn kho");
        });

        // Load medicine details
        apiService.getMedicineById("eq." + medicineId, "*").enqueue(new Callback<List<MedicineItem>>() {
            @Override
            public void onResponse(Call<List<MedicineItem>> call, Response<List<MedicineItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    MedicineItem medicine = response.body().get(0);
                    populateFields(medicine);
                } else {
                    Toast.makeText(AdminMedicineDetailActivity.this, "Không thể tải chi tiết thuốc", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<MedicineItem>> call, Throwable t) {
                Toast.makeText(AdminMedicineDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(MedicineItem medicine) {
        etMedName.setText(medicine.getTen_thuoc());
        etMedActive.setText(medicine.getHoat_chat());
        actvMedUnit.setText(medicine.getDon_vi(), false);
        etMedStock.setText(String.valueOf(medicine.getTon_kho()));

        // Format to show plain numbers in input field
        etMedPrice.setText(String.format(java.util.Locale.US, "%.0f", medicine.getDon_gia()));
    }

    private void saveMedicine() {
        String name = etMedName.getText() != null ? etMedName.getText().toString().trim() : "";
        String active = etMedActive.getText() != null ? etMedActive.getText().toString().trim() : "";
        String unit = actvMedUnit.getText() != null ? actvMedUnit.getText().toString().trim() : "";
        String stockStr = etMedStock.getText() != null ? etMedStock.getText().toString().trim() : "";
        String priceStr = etMedPrice.getText() != null ? etMedPrice.getText().toString().trim() : "";

        // Validations
        if (name.isEmpty()) {
            etMedName.setError("Tên thuốc không được để trống");
            etMedName.requestFocus();
            return;
        }
        if (active.isEmpty()) {
            etMedActive.setError("Hoạt chất không được để trống");
            etMedActive.requestFocus();
            return;
        }
        if (unit.isEmpty()) {
            actvMedUnit.setError("Đơn vị tính không được để trống");
            actvMedUnit.requestFocus();
            return;
        }
        if (stockStr.isEmpty()) {
            etMedStock.setError("Tồn kho không được để trống");
            etMedStock.requestFocus();
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                etMedStock.setError("Số lượng tồn kho phải >= 0");
                etMedStock.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etMedStock.setError("Tồn kho phải là số nguyên hợp lệ");
            etMedStock.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etMedPrice.setError("Đơn giá không được để trống");
            etMedPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                etMedPrice.setError("Đơn giá phải >= 0");
                etMedPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etMedPrice.setError("Đơn giá phải là số hợp lệ");
            etMedPrice.requestFocus();
            return;
        }

        // Prepare request body
        Map<String, Object> body = new HashMap<>();
        body.put("ten_thuoc", name);
        body.put("hoat_chat", active);
        body.put("don_vi", unit);
        body.put("ton_kho", stock);
        body.put("don_gia", price);

        if (isEditMode) {
            // Update
            apiService.updateMedicine("eq." + medicineId, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminMedicineDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AdminMedicineDetailActivity.this, "Cập nhật thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AdminMedicineDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new
            apiService.createMedicine(body).enqueue(new Callback<List<MedicineItem>>() {
                @Override
                public void onResponse(Call<List<MedicineItem>> call, Response<List<MedicineItem>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminMedicineDetailActivity.this, "Thêm mới thuốc thành công", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AdminMedicineDetailActivity.this, "Thêm mới thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<MedicineItem>> call, Throwable t) {
                    Toast.makeText(AdminMedicineDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
