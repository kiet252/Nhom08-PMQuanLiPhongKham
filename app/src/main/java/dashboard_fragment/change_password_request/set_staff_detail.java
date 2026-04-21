package dashboard_fragment.change_password_request;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class set_staff_detail extends AppCompatActivity {

    private ShapeableImageView ivAvatar;
    private TextInputEditText etName, etEmail, etPhone, etBirthday, etAddress, etCreatedAt;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private UserProfile userProfile;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_staff_detail);

        initViews();
        getDataFromIntent();
        setupListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivEditAvatar);
        etName = findViewById(R.id.etEditName);
        etEmail = findViewById(R.id.etEditEmail);
        etPhone = findViewById(R.id.etEditPhone);
        etBirthday = findViewById(R.id.etEditBirthday);
        etAddress = findViewById(R.id.etEditAddress);
        etCreatedAt = findViewById(R.id.etEditCreatedAt);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnBack = findViewById(R.id.btnBackEdit);
    }

    private void getDataFromIntent() {
        userProfile = (UserProfile) getIntent().getSerializableExtra("user_profile");
        if (userProfile != null) {
            etName.setText(userProfile.getHo_ten());
            etEmail.setText(userProfile.getEmail());
            etPhone.setText(userProfile.getSo_dien_thoai());
            etAddress.setText(userProfile.getDia_chi());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (userProfile.getNgay_sinh() != null) {
                etBirthday.setText(sdf.format(userProfile.getNgay_sinh()));
            }
            
            // Bạn có thể dùng Glide để load avatar từ link Supabase Storage ở đây
            // Glide.with(this).load(userProfile.getAvatarUrl()).into(ivAvatar);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Chọn ảnh đại diện
        ivAvatar.setOnClickListener(v -> openGallery());

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    // Bộ chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivAvatar.setImageURI(selectedImageUri); // Xem trước ảnh
                }
            }
    );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng không để trống thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu bạn muốn check mật khẩu ở đây, cần có thêm ô nhập mật khẩu trong layout
        // Hiện tại layout của bạn chưa có ô mật khẩu, tôi sẽ để hàm trống như yêu cầu
        saveDataToSupabase();
    }

    private void saveDataToSupabase() {
        // TODO: Viết code gọi API update thông tin lên Supabase ở đây
        Toast.makeText(this, "Đang lưu thay đổi...", Toast.LENGTH_SHORT).show();
    }
}