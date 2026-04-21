package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_staff);

        // 1. Ánh xạ các View từ XML đã sửa
        recyclerView = findViewById(R.id.rvEmployee);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyText = findViewById(R.id.tvEmptyText);

        // 2. Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffList = new ArrayList<>();
        adapter = new StaffItemAdapter(staffList);
        adapter.setOnItemClickListener(new StaffItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(StaffItem item) {
                // Hành động khi click vào item: Chuyển sang màn hình chi tiết
                Intent intent = new Intent(admin_manage_staff.this, staff_detail.class);

                // Truyền ID hoặc dữ liệu nhân viên qua màn hình chi tiết
                intent.putExtra("id", item.getId());

                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        // 3. Khởi tạo Repository và lấy Token
        ProfileRepository profileRepository = new ProfileRepository(this, getString(R.string.abAIkey));
        String currentToken = SharedPrefManager.getInstance(this).getToken();

        // 4. Gọi API với xử lý UI mới
        profileRepository.getListProfile("neq.Quản trị viên").enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserProfile> profiles = response.body();

                    if (profiles.isEmpty()) {
                        // HIỆN THÔNG BÁO: Danh sách trống
                        updateUI(true, "Hiện tại chưa có nhân viên nào trong hệ thống");
                    } else {
                        // HIỆN DANH SÁCH: Có dữ liệu
                        updateUI(false, "");
                        staffList.clear();
                        for (UserProfile profile : profiles) {
                            staffList.add(new StaffItem(
                                    profile.getID(),
                                    profile.getHo_ten(),
                                    profile.getChuc_vu()
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    // HIỆN THÔNG BÁO: Lỗi từ Server (VD: 401, 404)
                    updateUI(true, "Lỗi hệ thống (" + response.code() + "). Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                // HIỆN THÔNG BÁO: Lỗi kết nối mạng
                Log.e("API_ERROR", "Lỗi kết nối: " + t.getMessage());
                updateUI(true, "Không thể kết nối máy chủ. Vui lòng kiểm tra Internet.");
            }
        });

        // Xử lý các nút điều hướng cơ bản
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    /**
     * Hàm dùng để hoán đổi hiển thị giữa RecyclerView và Layout thông báo
     * @param isEmpty true nếu muốn hiện thông báo lỗi, false nếu có dữ liệu
     * @param message Nội dung chữ muốn hiển thị giữa màn hình
     */
    private void updateUI(boolean isEmpty, String message) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (tvEmptyText != null) {
                tvEmptyText.setText(message);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}