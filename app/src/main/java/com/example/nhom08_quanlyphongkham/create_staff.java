package com.example.nhom08_quanlyphongkham;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class create_staff extends AppCompatActivity {

    private ShapeableImageView ivAvatar;
    private FloatingActionButton fabSelectAvatar;
    private TextInputEditText etName, etEmail, etPassword, etPhone, etGender, etBirthday, etAddress;
    private AutoCompleteTextView actvRole;
    private MaterialButton btnCreate;
    private ImageButton btnBack;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_staff);

        initViews();
        setupRoleDropdown();
        setupListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivCreateAvatar);
        fabSelectAvatar = findViewById(R.id.fabSelectAvatar);
        etName = findViewById(R.id.etCreateName);
        etEmail = findViewById(R.id.etCreateEmail);
        etPassword = findViewById(R.id.etCreatePassword);
        etPhone = findViewById(R.id.etCreatePhone);
        etGender = findViewById(R.id.etCreateGender);
        etBirthday = findViewById(R.id.etCreateBirthday);
        etAddress = findViewById(R.id.etCreateAddress);
        actvRole = findViewById(R.id.actvCreateRole);
        btnCreate = findViewById(R.id.btnCreateStaff);
        btnBack = findViewById(R.id.btnBackCreate);
    }

    private void setupRoleDropdown() {
        String[] roles = new String[]{"Bác sĩ", "Y tá", "Nhân viên", "Tiếp tân"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        actvRole.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Chọn ảnh đại diện
        fabSelectAvatar.setOnClickListener(v -> openGallery());
        ivAvatar.setOnClickListener(v -> openGallery());

        // Chọn ngày sinh
        etBirthday.setOnClickListener(v -> showDatePicker());

        // Chọn giới tính
        etGender.setOnClickListener(v -> showGenderDialog());

        // Nút Tạo nhân viên
        btnCreate.setOnClickListener(v -> validateAndCreate());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            etBirthday.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showGenderDialog() {
        String[] genders = {"Nam", "Nữ", "Khác"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn giới tính");
        builder.setItems(genders, (dialog, which) -> etGender.setText(genders[which]));
        builder.show();
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

    private void validateAndCreate() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = actvRole.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các thông tin bắt buộc!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        saveNewStaffToSupabase();
    }

    private void saveNewStaffToSupabase() {
        // TODO: Viết code gọi API tạo tài khoản và lưu profile lên Supabase tại đây
        Toast.makeText(this, "Đang khởi tạo tài khoản nhân viên...", Toast.LENGTH_SHORT).show();
    }
}