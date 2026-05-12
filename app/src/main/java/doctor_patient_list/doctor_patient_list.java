package doctor_patient_list;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.nhom08_quanlyphongkham.R; // Đảm bảo đúng tên package của bạn
import java.util.ArrayList;
import java.util.List;

public class doctor_patient_list extends AppCompatActivity {

    private RecyclerView rvMedicalRecordList;
    private doctor_patient_adapter adapter;
    private List<doctor_patient_record> fullList;
    private List<doctor_patient_record> displayList;

    private TextInputEditText edtSearch;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_patient_list); // Tên file XML của bạn

        // 1. Ánh xạ View (Phải khớp ID trong XML)
        initViews();

        // 2. Tạo dữ liệu mẫu
        setupData();

        // 3. Cấu hình RecyclerView
        setupRecyclerView();

        // 4. Thiết lập tìm kiếm
        setupSearch();

        // 5. Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        // Lưu ý: Các ID này phải có trong file XML bạn vừa gửi
        rvMedicalRecordList = findViewById(R.id.rvMedicalRecordList);
        edtSearch = findViewById(R.id.edtMedicalRecordSearch);
        btnBack = findViewById(R.id.btnBackMedicalRecordList);

        // XOÁ BỎ các dòng ánh xạ tvCountAll, btnFilter... vì XML của bạn không có.
        // Đó chính là nguyên nhân gây lỗi Crash (NullPointerException).
    }

    private void setupData() {
        fullList = new ArrayList<>();
        // Thêm dữ liệu mẫu phù hợp với class patient_record của bạn
        fullList.add(new doctor_patient_record("Nguyễn Văn A", "0987654321", "Chờ khám"));
        fullList.add(new doctor_patient_record("Trần Thị B", "0123456789", "Đang khám"));
        fullList.add(new doctor_patient_record("Lê Văn C", "0333444555", "Đã khám"));

        displayList = new ArrayList<>(fullList);
    }

    private void setupRecyclerView() {
        adapter = new doctor_patient_adapter(displayList);
        rvMedicalRecordList.setLayoutManager(new LinearLayoutManager(this));
        rvMedicalRecordList.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                displayList.clear();

                for (doctor_patient_record item : fullList) {
                    if (item.getPatientName().toLowerCase().contains(query) ||
                            item.getPatientId().contains(query)) {
                        displayList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}