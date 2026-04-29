package dashboard_fragment.change_password_request;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class set_staff_detail extends AppCompatActivity {

    private ShapeableImageView ivAvatar;
    private TextInputEditText etName, etEmail, etPhone, etAddress;
    private TextInputEditText etBirthday, etCreatedAt;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private UserProfile userProfile;
    private Uri selectedImageUri;
    private ProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_staff_detail);

        profileRepository = new ProfileRepository(this);

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
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivAvatar.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> validateAndSave());
        etBirthday.setOnClickListener(v-> showDatePicker());
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            etBirthday.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivAvatar.setImageURI(selectedImageUri);
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
        String birthday = etBirthday.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "Vui lòng không để trống thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        saveDataToSupabase(name, phone, address, birthday);
    }

    private void saveDataToSupabase(String name, String phone, String address, String birthday) {
        if (userProfile == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("ho_ten", name);
        updates.put("so_dien_thoai", phone);
        updates.put("dia_chi", address);

        try {
            SimpleDateFormat uiFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            updates.put("ngay_sinh", dbFormat.format(uiFormat.parse(birthday)));
        } catch (Exception e) {
            Log.e("DATE_ERROR", "Lỗi format ngày: " + e.getMessage());
        }

        profileRepository.updateProfile(userProfile.getID(), updates).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(set_staff_detail.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(set_staff_detail.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(set_staff_detail.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}