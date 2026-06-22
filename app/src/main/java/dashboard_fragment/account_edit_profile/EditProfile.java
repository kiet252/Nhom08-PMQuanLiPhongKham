package dashboard_fragment.account_edit_profile;

import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_URL;
import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_ANON_KEY;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;
import dashboard_fragment.account_edit_profile.update_profile_logic.UpdateProfileRequest;
import dashboard_fragment.staff_add_update_patient.PatientDatabaseConstraintsChecker;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class EditProfile extends BaseActivity {

    private ImageButton btnBackEditProfile;
    private MaterialButton btnSaveEditProfile;
    private TextInputEditText edtEditProfileFullName, edtEditProfileEmail, edtEditProfileBirthday, edtEditProfilePhone, edtEditProfileAddress;
    private RadioGroup rgEditProfileGender;
    private UserProfile userProfile;
    private String currentToken;
    private ImageView avatar;
    private Uri selectedImageUri;
    private EditProfileRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_edit_profile);

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
        avatar = findViewById(R.id.imgEditProfileAvatar);
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

        // Gọi hàm riêng để load ảnh
        loadAvatar(userProfile.getAnh_dai_dien());
    }

    /**
     * Hàm riêng biệt để tải ảnh vào ImageView imgAvatar
     */
    private void loadAvatar(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            avatar.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        String fullUrl = avatarPath;
        if (!avatarPath.startsWith("http")) {
            // Nối Base URL của Supabase Storage
            fullUrl = "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + avatarPath;
        }

        ImageRequest request = new ImageRequest.Builder(this)
                .data(fullUrl)
                .target(avatar)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .build();

        Coil.imageLoader(this).enqueue(request);
    }

    private void setupListeners() {
        btnBackEditProfile.setOnClickListener(v -> finish());
        btnSaveEditProfile.setOnClickListener(v -> saveProfile());
        avatar.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    avatar.setImageURI(selectedImageUri);
                }
            }
    );

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

        if (!PatientDatabaseConstraintsChecker.isValidPhoneNumInDB(soDienThoai)) {
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

        // Nếu có ảnh mới được chọn, upload trước; nếu không, gửi request ngay
        if (selectedImageUri != null) {
            uploadAvatarAndUpdateProfile(hoTen, soDienThoai, diaChi, gioiTinh);
        } else {
            sendUpdateProfileRequest(hoTen, soDienThoai, diaChi, gioiTinh, userProfile.getAnh_dai_dien());
        }
    }

    private void uploadAvatarAndUpdateProfile(String hoTen, String soDienThoai, String diaChi, String gioiTinh) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang thay đổi thông tin...");
        pd.setCancelable(false);
        pd.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            String uploadedAvatarUrl = null;

            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                if (is == null) {
                    throw new IOException("Không thể mở file ảnh");
                }

                byte[] imageBytes = getBytes(is);
                is.close();

                UserProfile profile = SharedPrefManager.getInstance(this).getProfile();
                String userId = profile != null ? profile.getID() : "user";
                String fileName = userId + "_" + System.currentTimeMillis() + ".jpg";
                String uploadUrl = SUPABASE_URL + "storage/v1/object/avatars/" + fileName;

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String body = response.body() != null ? response.body().string() : "null";

                    Log.e("SUPABASE_AVATAR",
                            "URL = " + uploadUrl +
                                    "\nCODE = " + response.code() +
                                    "\nMESSAGE = " + response.message() +
                                    "\nBODY = " + body);

                    if (response.isSuccessful()) {
                        uploadedAvatarUrl = SUPABASE_URL + "storage/v1/object/public/avatars/" + fileName;
                        Log.e("SUPABASE_AVATAR", "Public URL: " + uploadedAvatarUrl);
                    } else {
                        throw new IOException("Lỗi upload: " + response.code() + "\n" + body);
                    }
                }

                final String avatarUrl = uploadedAvatarUrl;
                handler.post(() -> {
                    pd.dismiss();
                    sendUpdateProfileRequest(hoTen, soDienThoai, diaChi, gioiTinh, avatarUrl);
                });

            } catch (Exception e) {
                handler.post(() -> {
                    pd.dismiss();
                    Toast.makeText(EditProfile.this, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("EditProfile", "Avatar upload error", e);
                });
            }
        });
    }

    private void sendUpdateProfileRequest(String hoTen, String soDienThoai, String diaChi, String gioiTinh, String anhDaiDien) {
        UpdateProfileRequest request = new UpdateProfileRequest(
                hoTen,
                soDienThoai,
                diaChi,
                gioiTinh,
                anhDaiDien
        );

        repository.updateProfile(currentToken, userProfile.getID(), request)
                .enqueue(new Callback<java.util.List<UserProfile>>() {
                @Override
                public void onResponse(@NonNull Call<java.util.List<UserProfile>> call,
                                       @NonNull retrofit2.Response<java.util.List<UserProfile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            UserProfile updatedProfile = response.body().get(0);
                            SharedPrefManager.getInstance(EditProfile.this).saveProfile(updatedProfile);

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated_ho_ten", updatedProfile.getHo_ten());
                            resultIntent.putExtra("updated_so_dien_thoai", updatedProfile.getSo_dien_thoai());
                            resultIntent.putExtra("updated_dia_chi", updatedProfile.getDia_chi());
                            resultIntent.putExtra("updated_gioitinh", updatedProfile.getGioitinh());
                            resultIntent.putExtra("updated_anh_dai_dien", updatedProfile.getAnh_dai_dien());
                            setResult(RESULT_OK, resultIntent);

                            Toast.makeText(EditProfile.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProfile.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<java.util.List<UserProfile>> call, @NonNull Throwable t) {
                        Toast.makeText(EditProfile.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        Log.d("Error", "Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
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
