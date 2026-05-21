package dashboard_fragment.account_change_password_request;

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
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class set_staff_detail extends BaseActivity {

    private ShapeableImageView ivAvatar;
    private TextInputEditText etName, etEmail, etPhone, etAddress;
    private TextInputEditText etBirthday, etCreatedAt;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private UserProfile userProfile;
    private Uri selectedImageUri;
    private ProfileRepository profileRepository;
    private String SUPABASE_ANON_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_set_staff_detail);

        SUPABASE_ANON_KEY = getString(R.string.abAIkey);
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

            loadAvatar(userProfile.getAnh_dai_dien());
        }
    }

    private void loadAvatar(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            ivAvatar.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        String fullUrl = avatarPath;
        if (!avatarPath.startsWith("http")) {
            fullUrl = SUPABASE_URL + "storage/v1/object/public/avatars/" + avatarPath;
        }

        ImageRequest request = new ImageRequest.Builder(this)
                .data(fullUrl)
                .target(ivAvatar)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .build();
        Coil.imageLoader(this).enqueue(request);
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

    private String uploadImage(Uri uri) throws IOException {
        OkHttpClient client = new OkHttpClient();
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("Can't open image stream");

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        byte[] bytes = byteBuffer.toByteArray();
        inputStream.close();

        String fileName = "avatar_" + userProfile.getID() + "_" + System.currentTimeMillis() + ".jpg";
        String uploadUrl = SUPABASE_URL + "storage/v1/object/avatars/" + fileName;

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(RequestBody.create(bytes, MediaType.parse(getContentResolver().getType(uri))))
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Upload failed: " + response.body().string());
            return fileName;
        }
    }

    private void saveDataToSupabase(String name, String phone, String address, String birthday) {
        if (userProfile == null) return;

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang cập nhật...");
        pd.setCancelable(false);
        pd.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("ho_ten", name);
                updates.put("so_dien_thoai", phone);
                updates.put("dia_chi", address);

                // Nếu có chọn ảnh mới, thực hiện upload trước
                if (selectedImageUri != null) {
                    String newFileName = uploadImage(selectedImageUri);
                    updates.put("anh_dai_dien", newFileName);
                }

                SimpleDateFormat uiFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                updates.put("ngay_sinh", dbFormat.format(uiFormat.parse(birthday)));

                handler.post(() -> {
                    profileRepository.updateProfile(userProfile.getID(), updates).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            pd.dismiss();
                            if (response.isSuccessful()) {
                                Toast.makeText(set_staff_detail.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                setResult(Activity.RESULT_OK); // Trả về kết quả thành công để load lại dữ liệu
                                finish();
                            } else {
                                Toast.makeText(set_staff_detail.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            pd.dismiss();
                            Toast.makeText(set_staff_detail.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                handler.post(() -> {
                    pd.dismiss();
                    Log.e("UPDATE_ERROR", "Error: " + e.getMessage());
                    Toast.makeText(set_staff_detail.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
