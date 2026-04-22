package com.example.nhom08_quanlyphongkham;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.imageview.ShapeableImageView;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import dashboard_fragment.change_password_request.set_staff_detail;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class staff_detail extends AppCompatActivity {
    private UserProfile userProfile;
    private TextView name;
    private TextView email;
    private TextView date_created;
    private TextView gender;
    private TextView phone;
    private TextView birthday;
    private TextView address;
    private TextView role;
    private TextView password;
    private ShapeableImageView picture_link;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_detail);

        String id = getIntent().getStringExtra("id");
        name = findViewById(R.id.tvDetailName);
        email = findViewById(R.id.tvDetailEmail);
        date_created = findViewById(R.id.tvDetailCreatedAt);
        gender = findViewById(R.id.tvDetailGender);
        phone = findViewById(R.id.tvDetailPhone);
        birthday = findViewById(R.id.tvDetailBirthday);
        address = findViewById(R.id.tvDetailAddress);
        role = findViewById(R.id.tvDetailRole);
        picture_link = findViewById(R.id.ivStaffAvatar);

        ImageButton btnBackDetail = findViewById(R.id.btnBackDetail);
        ImageButton btnEdit = findViewById(R.id.btnEdit);

        ProfileRepository profile = new ProfileRepository(this, getString(R.string.abAIkey));
        profile.getProfile(id).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Lấy profile đầu tiên trong danh sách trả về
                    userProfile = response.body().get(0);

                    // Đổ dữ liệu vào các TextView
                    name.setText(userProfile.getHo_ten());
                    email.setText(userProfile.getEmail());
                    gender.setText(userProfile.getGioitinh());
                    phone.setText(userProfile.getSo_dien_thoai());
                    address.setText(userProfile.getDia_chi());
                    role.setText(userProfile.getChuc_vu());

                    // Xử lý ngày sinh (vì là kiểu Date nên cần format lại nếu muốn đẹp)
                    if (userProfile.getNgay_sinh() != null) {
                        // Định dạng ngày/tháng/năm
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        String formattedDate = sdf.format(userProfile.getNgay_sinh());
                        birthday.setText(formattedDate);
                    }
                    if (userProfile.getCreated_at() != null) {
                        // Định dạng ngày/tháng/năm
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        String formattedDate = sdf.format(userProfile.getCreated_at());
                        date_created.setText(formattedDate);
                    }

                    // Với hình ảnh, bạn nên dùng thư viện Glide hoặc Picasso để load link
//                     Glide.with(staff_detail.this).load(userProfile.getPictureLink()).into(picture_link);
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
            return;
            }
        });

        btnBackDetail.setOnClickListener(v -> {
            finish();
        });


        btnEdit.setOnClickListener(v -> {
            if (userProfile != null) {
                Intent intent = new Intent(staff_detail.this, set_staff_detail.class);
                intent.putExtra("user_profile", userProfile); // Truyền cả Object (UserProfile đã có Serializable)
                startActivity(intent);
            }
        });
        // Tìm nút xóa (đã có id trong layout staff_detail.xml)
        com.google.android.material.button.MaterialButton btnDelete = findViewById(R.id.btnDeleteStaff);

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }
    private void showDeleteConfirmationDialog() {
        // 1. Khởi tạo Dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.delete_staff_dialog, null);
        builder.setView(view);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // 2. Ánh xạ các nút trong Dialog
        com.google.android.material.button.MaterialButton btnConfirm = view.findViewById(R.id.btnDeleteConfirm);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnDeleteCancel);
        android.widget.TextView tvMessage = view.findViewById(R.id.tvDeleteConfirmMessage);

        if (userProfile != null) {
            tvMessage.setText("Bạn có chắc chắn muốn xóa nhân viên " + userProfile.getHo_ten() + " khỏi hệ thống không?");
        }

        // 3. Xử lý sự kiện nút Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 4. Xử lý sự kiện nút Xóa
        btnConfirm.setOnClickListener(v -> {
            deleteStaffFromDatabase(); // Gọi hàm xóa
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteStaffFromDatabase() {
        // TODO: Viết lệnh xóa trên database (Supabase) tại đây
        // Ví dụ: profileRepository.deleteProfile(userProfile.getID()).enqueue(...)
        android.widget.Toast.makeText(this, "Đang thực hiện xóa nhân viên...", android.widget.Toast.LENGTH_SHORT).show();
    }
}
