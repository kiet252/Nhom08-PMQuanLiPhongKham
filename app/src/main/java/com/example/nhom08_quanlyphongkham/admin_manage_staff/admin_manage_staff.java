package com.example.nhom08_quanlyphongkham.admin_manage_staff;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class admin_manage_staff extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyText;
    private StaffItemAdapter adapter;
    private List<StaffItem> staffList;
    private List<StaffItem> staffListOriginal = new ArrayList<>();
    private boolean isAscending = true;

    ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Nhận kết quả từ staff_detail (Activity.RESULT_OK) để load lại data
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadData(new ProfileRepository(this));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_staff);

        recyclerView = findViewById(R.id.rvEmployee);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        EditText etSearch = findViewById(R.id.etSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffList = new ArrayList<>();
        adapter = new StaffItemAdapter(staffList);
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(admin_manage_staff.this, staff_detail.class);
            intent.putExtra("id", item.getId());
            editLauncher.launch(intent);
        });
        recyclerView.setAdapter(adapter);

        ProfileRepository profileRepository = new ProfileRepository(this);
        loadData(profileRepository);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnSort).setOnClickListener(v -> sortList());
        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(admin_manage_staff.this, create_staff.class);
            editLauncher.launch(intent);
        });
    }

    private void loadData(ProfileRepository profileRepository) {
        profileRepository.getListProfile("neq.Quản trị viên", "eq.Đang hoạt động").enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserProfile> profiles = response.body();
                    staffListOriginal.clear();
                    staffList.clear();

                    if (profiles.isEmpty()) {
                        updateUI(true, "Hiện tại không có nhân viên nào đang hoạt động");
                    } else {
                        updateUI(false, "");
                        for (UserProfile profile : profiles) {
                            StaffItem item = new StaffItem(
                                    profile.getID(),
                                    profile.getHo_ten(),
                                    profile.getChuc_vu(),
                                    profile.getAnh_dai_dien()
                            );
                            staffListOriginal.add(item);
                            staffList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    updateUI(true, "Lỗi hệ thống (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi kết nối: " + t.getMessage());
                updateUI(true, "Không thể kết nối máy chủ.");
            }
        });
    }

    private void filterSearch(String query) {
        List<StaffItem> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (StaffItem item : staffListOriginal) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            String id = item.getId() != null ? item.getId().toLowerCase() : "";
            if (name.contains(lowerQuery) || id.contains(lowerQuery)) {
                filtered.add(item);
            }
        }
        staffList.clear();
        staffList.addAll(filtered);
        adapter.notifyDataSetChanged();
        updateUI(staffList.isEmpty(), "Không tìm thấy kết quả phù hợp");
    }

    private void sortList() {
        Collections.sort(staffList, (s1, s2) -> {
            String n1 = s1.getName() != null ? s1.getName() : "";
            String n2 = s2.getName() != null ? s2.getName() : "";
            if (isAscending) return n1.compareToIgnoreCase(n2);
            else return n2.compareToIgnoreCase(n1);
        });
        isAscending = !isAscending;
        adapter.notifyDataSetChanged();
        Toast.makeText(this, isAscending ? "Sắp xếp: Z-A" : "Sắp xếp: A-Z", Toast.LENGTH_SHORT).show();
    }

    private void showFilterDialog() {
        String[] roles = {"Tất cả", "Bác sĩ", "Nhân viên"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn chức vụ để hiển thị");
        builder.setItems(roles, (dialog, which) -> {
            String selected = roles[which];
            if (selected.equals("Tất cả")) {
                staffList.clear();
                staffList.addAll(staffListOriginal);
            } else {
                List<StaffItem> filtered = new ArrayList<>();
                for (StaffItem item : staffListOriginal) {
                    if (item.getRole() != null && item.getRole().equalsIgnoreCase(selected)) {
                        filtered.add(item);
                    }
                }
                staffList.clear();
                staffList.addAll(filtered);
            }
            adapter.notifyDataSetChanged();
            updateUI(staffList.isEmpty(), "Không có nhân viên chức vụ: " + selected);
        });
        builder.show();
    }

    private void updateUI(boolean isEmpty, String message) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (tvEmptyText != null) tvEmptyText.setText(message);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
