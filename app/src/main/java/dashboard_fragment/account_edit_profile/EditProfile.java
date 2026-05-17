package dashboard_fragment.account_edit_profile;

import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_URL;

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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class EditProfile extends AppCompatActivity {

    private ImageButton btnBackEditProfile;
    private MaterialButton btnSaveEditProfile;
    private TextInputEditText edtEditProfileFullName, edtEditProfileEmail, edtEditProfileBirthday, edtEditProfilePhone, edtEditProfileAddress;
    private RadioGroup rgEditProfileGender;
    private UserProfile userProfile;
    private String SUPABASE_ANON_KEY;
    private String currentToken;
    ImageView avatar;
    private Uri selectedImageUri;
    private EditProfileRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SUPABASE_ANON_KEY = getString(R.string.abAIkey);
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

        String avatarUrl = userProfile.getAnh_dai_dien();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = SUPABASE_URL.replaceAll("/$", "") + "/storage/v1/object/public/avatars/" + avatarUrl;
            }
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(avatarUrl)
                    .target(avatar)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build();
            Coil.imageLoader(this).enqueue(request);
        }
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

    private String UploadAvatarInStorage(Uri uri) throws IOException {
        OkHttpClient client = new OkHttpClient();
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("Can't open image stream");

        byte[] imageBytes;
        try {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            imageBytes = byteBuffer.toByteArray();
        } finally {
            inputStream.close();
        }

        String mimeType = getContentResolver().getType(uri);
        String ext = (mimeType != null && mimeType.contains("png")) ? "png" : "jpg";
        String fileName = "avatar_" + userProfile.getID() + "_" + System.currentTimeMillis() + "." + ext;
        String uploadPath = "avatars/" + fileName;

        Request uploadRequest = new Request.Builder()
                .url(SUPABASE_URL.replaceAll("/$", "") + "/storage/v1/object/" + uploadPath)
                .post(RequestBody.create(imageBytes, MediaType.parse(mimeType != null ? mimeType : "image/jpeg")))
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .addHeader("x-upsert", "true")
                .build();

        try (Response response = client.newCall(uploadRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed with code " + response.code() + ": " + response.body().string());
            }
            return fileName;
        }
    }

    private void saveProfile() {
        if (userProfile == null || currentToken == null) return;

        String hoTen = edtEditProfileFullName.getText().toString().trim();
        String soDienThoai = edtEditProfilePhone.getText().toString().trim();
        String diaChi = edtEditProfileAddress.getText().toString().trim();
        String gioiTinh = getSelectedGender();

        if (hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang lưu thông tin...");
        pd.setCancelable(false);
        pd.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String avatarToSubmit = userProfile.getAnh_dai_dien();
                if (selectedImageUri != null) {
                    avatarToSubmit = UploadAvatarInStorage(selectedImageUri);
                }

                String finalAvatar = avatarToSubmit;
                handler.post(() -> {
                    UpdateProfileRequest request = new UpdateProfileRequest(hoTen, soDienThoai, diaChi, gioiTinh, finalAvatar);
                    repository.updateProfile(currentToken, userProfile.getID(), request)
                            .enqueue(new Callback<java.util.List<UserProfile>>() {
                                @Override
                                public void onResponse(@NonNull Call<java.util.List<UserProfile>> call, @NonNull retrofit2.Response<java.util.List<UserProfile>> response) {
                                    pd.dismiss();
                                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                        UserProfile updated = response.body().get(0);
                                        SharedPrefManager.getInstance(EditProfile.this).saveProfile(updated);
                                        
                                        Intent res = new Intent();
                                        res.putExtra("updated_ho_ten", updated.getHo_ten());
                                        res.putExtra("updated_so_dien_thoai", updated.getSo_dien_thoai());
                                        res.putExtra("updated_dia_chi", updated.getDia_chi());
                                        res.putExtra("updated_gioitinh", updated.getGioitinh());
                                        res.putExtra("updated_anh_dai_dien", updated.getAnh_dai_dien());
                                        setResult(RESULT_OK, res);
                                        
                                        Toast.makeText(EditProfile.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        String errorMsg = "Lỗi cập nhật hồ sơ";
                                        if (response.errorBody() != null) {
                                            try {
                                                errorMsg += ": " + response.errorBody().string();
                                            } catch (IOException e) { e.printStackTrace(); }
                                        } else if (response.body() != null && response.body().isEmpty()) {
                                            errorMsg += ": Không tìm thấy ID người dùng trong Database";
                                        }
                                        Log.e("EDIT_PROFILE", errorMsg);
                                        Toast.makeText(EditProfile.this, errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<java.util.List<UserProfile>> call, @NonNull Throwable t) {
                                    pd.dismiss();
                                    Toast.makeText(EditProfile.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            } catch (Exception e) {
                handler.post(() -> {
                    pd.dismiss();
                    Log.e("EDIT_PROFILE", "Lỗi xử lý", e);
                    Toast.makeText(EditProfile.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String getSelectedGender() {
        int checkedId = rgEditProfileGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbGenderMale) return "Nam";
        if (checkedId == R.id.rbGenderFemale) return "Nữ";
        return "Khác";
    }
}
