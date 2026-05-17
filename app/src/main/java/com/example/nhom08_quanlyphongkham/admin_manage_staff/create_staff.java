package com.example.nhom08_quanlyphongkham.admin_manage_staff;

import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_URL;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class create_staff extends AppCompatActivity {

    private ShapeableImageView ivAvatar;
    private String currentToken;

    private FloatingActionButton fabSelectAvatar;
    private TextInputEditText etName, etEmail, etPassword, etPhone, etGender, etBirthday, etAddress;
    private AutoCompleteTextView actvRole;
    private MaterialButton btnCreate;
    private ImageButton btnBack;
    private Uri selectedImageUri;

    private String SUPABASE_ANON_KEY;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentToken = SharedPrefManager.getInstance(this).getToken();
        setContentView(R.layout.admin_create_staff);

        SUPABASE_ANON_KEY = getString(R.string.abAIkey);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

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
        String[] roles = new String[]{"Bác sĩ", "Nhân viên"};
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
        String gender = etGender.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String role = actvRole.getText().toString().trim();

        // 1. Kiểm tra điền đầy đủ tất cả thông tin văn bản
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() ||
                gender.isEmpty() || birthday.isEmpty() || address.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ tất cả các thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra đã chọn ảnh đại diện chưa
        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện cho nhân viên!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra email hợp lệ
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ!");
            etEmail.requestFocus();
            return;
        }

        // 4. Kiểm tra mật khẩu (>= 6 ký tự)
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự!");
            etPassword.requestFocus();
            return;
        }

        // 5. Kiểm tra số điện thoại (10 số, bắt đầu bằng số 0)
        if (!phone.matches("^0\\d{9}$")) {
            etPhone.setError("Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0!");
            etPhone.requestFocus();
            return;
        }

        // Định dạng lại ngày sinh từ DD/MM/YYYY sang YYYY-MM-DD cho Supabase
        String formattedBirthday = "";
        try {
            String[] parts = birthday.split("/");
            formattedBirthday = parts[2] + "-" + parts[1] + "-" + parts[0];
        } catch (Exception e) {
            formattedBirthday = birthday;
        }

        progressDialog.show();
        saveNewStaffToSupabase(name, email, password, phone, gender, formattedBirthday, address, role, selectedImageUri);
    }

    private void saveNewStaffToSupabase(String name, String email, String password, String phone,
                                        String gender, String birthday, String address,
                                        String role, Uri selectedImageUri) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // ── BƯỚC 1: Upload ảnh lên Storage ───────────────────────────
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                byte[] imageBytes;
                try {
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    imageBytes = byteBuffer.toByteArray();
                } finally {
                    if (inputStream != null) inputStream.close();
                }

                String mimeType = getContentResolver().getType(selectedImageUri);
                String ext = mimeType != null && mimeType.contains("png") ? "png" : "jpg";
                String tempFileName = "avatar_" + System.currentTimeMillis() + "." + ext;
                String uploadPath = "avatars/" + tempFileName;

                Request uploadRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/" + uploadPath)
                        .post(RequestBody.create(imageBytes, MediaType.parse(mimeType)))
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                        .addHeader("x-upsert", "true")
                        .build();

                Response uploadResponse = client.newCall(uploadRequest).execute();
                if (!uploadResponse.isSuccessful()) {
                    throw new IOException("Lỗi upload ảnh: " + uploadResponse.body().string());
                }

                // ── BƯỚC 2: Tạo user trong Supabase Auth ─────────────────────
                JSONObject signUpBody = new JSONObject();
                signUpBody.put("email", email);
                signUpBody.put("password", password);

                Request signUpRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/auth/v1/signup")
                        .post(RequestBody.create(
                                signUpBody.toString(),
                                MediaType.parse("application/json")))
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response signUpResponse = client.newCall(signUpRequest).execute();
                String signUpResult = signUpResponse.body().string();
                if (!signUpResponse.isSuccessful()) {
                    throw new IOException("Lỗi tạo tài khoản: " + signUpResult);
                }

                JSONObject authData = new JSONObject(signUpResult);
                String userId = authData.getJSONObject("user").getString("id");
                String createdAt = authData.getJSONObject("user").getString("created_at");

                // ── BƯỚC 3: Insert vào bảng profiles ─────────────────────────
                JSONObject profileBody = new JSONObject();
                profileBody.put("id", userId);
                profileBody.put("email", email);
                profileBody.put("user_name", email.split("@")[0]);
                profileBody.put("ho_ten", name);
                profileBody.put("created_at", createdAt);
                profileBody.put("gioitinh", gender);
                profileBody.put("so_dien_thoai", phone);
                profileBody.put("ngay_sinh", birthday);
                profileBody.put("dia_chi", address);
                profileBody.put("chuc_vu", role);
                String anhDaiDienUrl = SUPABASE_URL.replaceAll("/$", "") + "/storage/v1/object/public/" + uploadPath;
                profileBody.put("anh_dai_dien", anhDaiDienUrl);                profileBody.put("trang_thai_hoat_dong", "Đang hoạt động");

                Request profileRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/profiles")
                        .post(RequestBody.create(
                                profileBody.toString(),
                                MediaType.parse("application/json")))
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + currentToken)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation,resolution=merge-duplicates") // ← đổi dòng này
                        .build();

                Response profileResponse = client.newCall(profileRequest).execute();
                Log.d("PROFILE_BODY", profileBody.toString());
                if (!profileResponse.isSuccessful()) {
                    throw new IOException("Lỗi tạo profile: " + profileResponse.body().string());
                }

                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Tạo nhân viên thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SUPABASE", "Lỗi tạo nhân viên", e);
                });
            }

        });

    }

}