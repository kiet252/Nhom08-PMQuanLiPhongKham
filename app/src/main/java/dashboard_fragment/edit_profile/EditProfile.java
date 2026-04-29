package dashboard_fragment.edit_profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

import dashboard_fragment.edit_profile.update_profile_logic.UpdateProfileRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfile extends AppCompatActivity {

    private ImageButton btnBackEditProfile;
    private MaterialButton btnSaveEditProfile;

    private TextInputEditText edtEditProfileFullName, edtEditProfileEmail, edtEditProfileBirthday, edtEditProfilePhone, edtEditProfileAddress;

    private RadioGroup rgEditProfileGender;

    private UserProfile userProfile;
    private String currentToken;

    private EditProfileRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_edit_profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        getIntentInfo();
        repository = new EditProfileRepository(this);
        fillProfileData();
        setupListeners();
    }

    private void initializeViews() {
        btnBackEditProfile = findViewById(R.id.btnBackEditProfile);
        btnSaveEditProfile = findViewById(R.id.btnSaveEditProfile);

        edtEditProfileFullName = findViewById(R.id.edtEditProfileFullName);
        edtEditProfileEmail = findViewById(R.id.edtEditProfileEmail);
        edtEditProfileBirthday = findViewById(R.id.edtEditProfileBirthday);
        edtEditProfilePhone = findViewById(R.id.edtEditProfilePhone);
        edtEditProfileAddress = findViewById(R.id.edtEditProfileAddress);

        rgEditProfileGender = findViewById(R.id.rgEditProfileGender);
    }

    private void getIntentInfo() {
        Intent intent = getIntent();
        currentToken = intent.getStringExtra("accessToken");
        userProfile = (UserProfile) intent.getSerializableExtra("Userprofile");
    }

    private void fillProfileData() {
        if (userProfile == null) return;

        edtEditProfileFullName.setText(userProfile.getHo_ten());
        edtEditProfileEmail.setText(userProfile.getEmail());
        edtEditProfilePhone.setText(userProfile.getSo_dien_thoai());
        edtEditProfileAddress.setText(userProfile.getDia_chi());

        if (userProfile.getNgay_sinh() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            edtEditProfileBirthday.setText(sdf.format(userProfile.getNgay_sinh()));
        }

        String gender = userProfile.getGioitinh();
        if ("Nam".equalsIgnoreCase(gender)) {
            rgEditProfileGender.check(R.id.rbGenderMale);
        } else if ("Nữ".equalsIgnoreCase(gender) || "Nu".equalsIgnoreCase(gender)) {
            rgEditProfileGender.check(R.id.rbGenderFemale);
        } else {
            rgEditProfileGender.check(R.id.rbGenderOther);
        }
    }

    private void setupListeners() {
        btnBackEditProfile.setOnClickListener(v -> finish());
        btnSaveEditProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        if (userProfile == null) {
            Toast.makeText(this, "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentToken == null || currentToken.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token", Toast.LENGTH_SHORT).show();
            return;
        }

        String hoTen = edtEditProfileFullName.getText() != null
                ? edtEditProfileFullName.getText().toString().trim() : "";

        String soDienThoai = edtEditProfilePhone.getText() != null
                ? edtEditProfilePhone.getText().toString().trim() : "";

        String diaChi = edtEditProfileAddress.getText() != null
                ? edtEditProfileAddress.getText().toString().trim() : "";

        String gioiTinh = getSelectedGender();

        if (hoTen.isEmpty()) {
            edtEditProfileFullName.setError("Vui lòng nhập họ tên");
            edtEditProfileFullName.requestFocus();
            return;
        }

        if (soDienThoai.isEmpty()) {
            edtEditProfilePhone.setError("Vui lòng nhập số điện thoại");
            edtEditProfilePhone.requestFocus();
            return;
        }

        if (!soDienThoai.matches("^0\\d{9}$")) {
            edtEditProfilePhone.setError("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0");
            edtEditProfilePhone.requestFocus();
            return;
        }



        if (diaChi.isEmpty()) {
            edtEditProfileAddress.setError("Vui lòng nhập địa chỉ");
            edtEditProfileAddress.requestFocus();
            return;
        }

        if (gioiTinh.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(
                hoTen,
                soDienThoai,
                diaChi,
                gioiTinh
        );

        repository.updateProfile(currentToken, userProfile.getID(), request)
                .enqueue(new Callback<java.util.List<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<java.util.List<UserProfile>> call,
                                           @NonNull Response<java.util.List<UserProfile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {

                            UserProfile updatedProfile = response.body().get(0);
                            SharedPrefManager.getInstance(EditProfile.this).saveProfile(updatedProfile);

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated_ho_ten", updatedProfile.getHo_ten());
                            resultIntent.putExtra("updated_so_dien_thoai", updatedProfile.getSo_dien_thoai());
                            resultIntent.putExtra("updated_dia_chi", updatedProfile.getDia_chi());
                            resultIntent.putExtra("updated_gioitinh", updatedProfile.getGioitinh());
                            setResult(RESULT_OK, resultIntent);

                            Toast.makeText(EditProfile.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProfile.this, "Cập nhật thất bại hoặc không có dữ liệu trả về", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<java.util.List<UserProfile>> call, @NonNull Throwable t) {
                        Toast.makeText(EditProfile.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private String getSelectedGender() {
        int checkedId = rgEditProfileGender.getCheckedRadioButtonId();

        if (checkedId == R.id.rbGenderMale) {
            return "Nam";
        } else if (checkedId == R.id.rbGenderFemale) {
            return "Nữ";
        } else if (checkedId == R.id.rbGenderOther) {
            return "Khác";
        }

        return "";
    }
}
