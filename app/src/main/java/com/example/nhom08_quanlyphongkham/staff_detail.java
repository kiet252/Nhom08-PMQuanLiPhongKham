package com.example.nhom08_quanlyphongkham;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.imageview.ShapeableImageView;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class staff_detail extends AppCompatActivity {
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

        ProfileRepository profile = new ProfileRepository(this, getString(R.string.abAIkey));
        profile.getProfile(id).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Lấy profile đầu tiên trong danh sách trả về
                    UserProfile userProfile = response.body().get(0);

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
                    if (userProfile.getNgay_sinh() != null) {
                        // Định dạng ngày/tháng/năm
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        String formattedDate = sdf.format(userProfile.getNgay_sinh());
                        date_created.setText(formattedDate);
                    }

                    // Với hình ảnh, bạn nên dùng thư viện Glide hoặc Picasso để load link
                    // Glide.with(staff_detail.this).load(userProfile.getPictureLink()).into(picture_link);
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
            return;
            }
        });

    }
}
