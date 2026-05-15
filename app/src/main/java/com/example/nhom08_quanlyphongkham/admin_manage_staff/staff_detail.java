package com.example.nhom08_quanlyphongkham.admin_manage_staff;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import dashboard_fragment.account_change_password_request.set_staff_detail;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class staff_detail extends AppCompatActivity {
    private UserProfile userProfile;
    private String id;
    private TextView name;
    private TextView email;
    private TextView date_created;
    private TextView gender;
    private TextView phone;
    private TextView birthday;
    private TextView address;
    private TextView role;
    private ShapeableImageView picture_link;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile_detail);

        id = getIntent().getStringExtra("id");
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

        loadData(id);

        btnBackDetail.setOnClickListener(v -> {
            finish();
        });


        btnEdit.setOnClickListener(v -> {
            if (userProfile != null) {
                Intent intent = new Intent(staff_detail.this, set_staff_detail.class);

                intent.putExtra("user_profile", userProfile);
                editLauncher.launch(intent);
            }
        });

        com.google.android.material.button.MaterialButton btnDelete = findViewById(R.id.btnDeleteStaff);

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }
    ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadData(id);
                }
            }
    );
    private void loadData(String id)
    {
        ProfileRepository profile = new ProfileRepository(this);
        profile.getProfile(id).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    userProfile = response.body().get(0);

                    name.setText(userProfile.getHo_ten());
                    email.setText(userProfile.getEmail());
                    gender.setText(userProfile.getGioitinh());
                    phone.setText(userProfile.getSo_dien_thoai());
                    address.setText(userProfile.getDia_chi());
                    role.setText(userProfile.getChuc_vu());

                    if (userProfile.getNgay_sinh() != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        String formattedDate = sdf.format(userProfile.getNgay_sinh());
                        birthday.setText(formattedDate);
                    }
                    if (userProfile.getCreated_at() != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        String formattedDate = sdf.format(userProfile.getCreated_at());
                        date_created.setText(formattedDate);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
            }
        });
    }
    private void showDeleteConfirmationDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.admin_delete_staff_dialog, null);
        builder.setView(view);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        com.google.android.material.button.MaterialButton btnConfirm = view.findViewById(R.id.btnDeleteConfirm);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnDeleteCancel);
        android.widget.TextView tvMessage = view.findViewById(R.id.tvDeleteConfirmMessage);

        if (userProfile != null) {
            tvMessage.setText("Bạn có chắc chắn muốn xóa nhân viên " + userProfile.getHo_ten() + " khỏi hệ thống không?");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            deleteStaffFromDatabase(userProfile.getID());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteStaffFromDatabase(String staffId) {
        ProfileRepository profile = new ProfileRepository(this);
        profile.deactivateStaff(staffId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(staff_detail.this, "Đã vô hiệu hoá nhân viên thành công!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(staff_detail.this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(staff_detail.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}