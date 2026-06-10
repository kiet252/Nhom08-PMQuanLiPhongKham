    package dashboard_fragment;

    import android.Manifest;
    import android.content.pm.PackageManager;
    import android.provider.Settings;
    import android.app.AlertDialog;
        import android.view.LayoutInflater;
        import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Looper;
    import android.util.Log;
    import android.view.View;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.camera.core.CameraSelector;
    import androidx.camera.core.Preview;
    import androidx.camera.lifecycle.ProcessCameraProvider;
    import androidx.camera.view.PreviewView;
    import androidx.core.content.ContextCompat;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    import com.example.nhom08_quanlyphongkham.BaseActivity;
    import com.example.nhom08_quanlyphongkham.R;
    import com.example.nhom08_quanlyphongkham.UserProfile;
    import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
    import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
    import okhttp3.ResponseBody;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import com.google.common.util.concurrent.ListenableFuture;
    import android.graphics.Bitmap;
    import com.google.mlkit.vision.common.InputImage;
    import com.google.mlkit.vision.face.Face;
    import com.google.mlkit.vision.face.FaceDetector;
    import com.google.mlkit.vision.face.FaceDetectorOptions;
    import com.google.mlkit.vision.face.FaceDetection;
    import org.tensorflow.lite.Interpreter;

    import java.io.FileInputStream;
    import java.io.IOException;
    import java.nio.ByteBuffer;
    import java.nio.ByteOrder;
    import java.nio.MappedByteBuffer;
    import java.nio.channels.FileChannel;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    import java.util.concurrent.ExecutionException;

    public class Timekeeping extends BaseActivity {

        private ImageButton btnBack;
        private TextView tvTime, tvDate;
        private Spinner spinnerShift;
        private Button btnConfirm;
        private PreviewView cameraPreview;
        private ProfileRepository profileRepository;
        private UserProfile fetchedProfile;
        private String fetchedAndroidId;
        private String fetchedFaceId;

        // Khai báo ở đầu class — cùng chỗ với fetchedProfile, fetchedAndroidId
        private List<Map<String, Object>> shiftList = new ArrayList<>();
        private final Handler timeHandler = new Handler(Looper.getMainLooper());
        private Runnable timeRunnable;

            private final ActivityResultLauncher<String> requestPermissionLauncher =
                        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (isGranted) {
                                startCamera();
                            } else {
                                Toast.makeText(this, "Bạn cần cấp quyền Camera để thực hiện chấm công!", Toast.LENGTH_LONG).show();
                            }
                        });

            // ML / model fields for in-place verification
            private FaceDetector detector;
            private Interpreter tflite = null;
            private final int MODEL_INPUT_SIZE = 160; // adjust to model

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.face_checking);

            View mainView = findViewById(android.R.id.content);
            if (mainView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }

            initializeViews();
            setupShiftOptions();
            setupListeners();
            startTimeTracking();


            checkCameraPermission();
            // Initialize ML Kit detector and try to load TFLite model for in-place verification
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();
            detector = FaceDetection.getClient(options);
            try {
                tflite = new Interpreter(loadModelFile("facenet.tflite"));
            } catch (Exception ex) {
                Log.w("Timekeeping", "TFLite not loaded: " + ex.getMessage());
                tflite = null;
            }

            // Start profile/device check
            profileRepository = new ProfileRepository(this);
            scanAndCheckProfile();
        }

        private java.nio.MappedByteBuffer loadModelFile(String modelFile) throws java.io.IOException {
            java.io.FileInputStream is = null;
            try {
                android.content.res.AssetFileDescriptor afd = getAssets().openFd(modelFile);
                is = new java.io.FileInputStream(afd.getFileDescriptor());
                java.nio.channels.FileChannel fileChannel = is.getChannel();
                long startOffset = afd.getStartOffset();
                long declaredLength = afd.getDeclaredLength();
                return fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            } finally {
                if (is != null) try { is.close(); } catch (Exception ignored) {}
            }
        }

        private float[] runModel(Bitmap bitmap) {
            int inputSize = MODEL_INPUT_SIZE;
            ByteBuffer input = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
            input.order(ByteOrder.nativeOrder());

            int[] intValues = new int[inputSize * inputSize];
            bitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

            for (int i = 0; i < intValues.length; ++i) {
                int val = intValues[i];
                float r = ((val >> 16) & 0xFF);
                float g = ((val >> 8) & 0xFF);
                float b = (val & 0xFF);
                input.putFloat((r - 128f) / 128f);
                input.putFloat((g - 128f) / 128f);
                input.putFloat((b - 128f) / 128f);
            }
            input.rewind();

            float[][] output = new float[1][128];
            if (tflite != null) tflite.run(input, output);
            return output[0];
        }

        private java.util.List<Double> computeFaceVector(Bitmap bmp) {
            try {
                Bitmap small = Bitmap.createScaledBitmap(bmp, 16, 8, true);
                java.util.List<Double> vec = new java.util.ArrayList<>(128);
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 16; x++) {
                        int px = small.getPixel(x, y);
                        int r = (px >> 16) & 0xff;
                        int g = (px >> 8) & 0xff;
                        int b = px & 0xff;
                        double gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                        vec.add(gray * 2.0 - 1.0);
                    }
                }
                return vec;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private void l2Normalize(float[] v) {
            double sum = 0.0;
            for (float f : v) sum += f * f;
            double norm = Math.sqrt(sum);
            if (norm == 0) return;
            for (int i = 0; i < v.length; i++) v[i] /= norm;
        }

        private void initializeViews() {
            btnBack = findViewById(R.id.btnBackAdminExDetail);
            tvTime = findViewById(R.id.tv_time);
            tvDate = findViewById(R.id.tv_date);
            spinnerShift = findViewById(R.id.spinner_shift);
            btnConfirm = findViewById(R.id.btn_confirm);
            cameraPreview = findViewById(R.id.camera_preview);
        }

        private void checkCameraPermission() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }

        private void startCamera() {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                    ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                    try {
                        cameraProvider.unbindAll();

                        cameraProvider.bindToLifecycle(this, cameraSelector, preview);

                    } catch (Exception exc) {
                        Log.e("CAMERA_X", "Binding failed", exc);
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.e("CAMERA_X", "Error starting camera", e);
                }
            }, ContextCompat.getMainExecutor(this));
        }

        private void setupShiftOptions() {
            if (fetchedProfile == null || fetchedProfile.getID() == null) return;
            TimekeepingRepository repo = new TimekeepingRepository(this);
            repo.getShifts(fetchedProfile.getID()).enqueue(new Callback<List<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                    shiftList = response.body();

                    List<String> displayItems = new ArrayList<>();
                    for (Map<String, Object> row : shiftList) {
                        String start = formatTime((String) row.get("start_time"));
                        String end   = formatTime((String) row.get("end_time"));
                        displayItems.add(start + " - " + end);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            Timekeeping.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            displayItems
                    );
                    if (spinnerShift != null) spinnerShift.setAdapter(adapter);
                }
                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                    Toast.makeText(Timekeeping.this, "Không thể tải ca làm việc", Toast.LENGTH_SHORT).show();
                }
            });
        }
        private String formatTime(String timestamp) {
            if (timestamp == null) return "--:--";
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return output.format(input.parse(timestamp));
            } catch (Exception e) {
                return timestamp;
            }
        }
        private void setupListeners() {
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                    } else {
                        // When user presses confirm, validate device id against profile
                        handleConfirmAction();
                    }
                });
            }
        }

        private void scanAndCheckProfile() {
            // Simulate scanning face and retrieve profile from server by current user id
            com.example.nhom08_quanlyphongkham.UserProfile local = SharedPrefManager.getInstance(this).getProfile();
            if (local == null || local.getID() == null) {
                Toast.makeText(this, "Không có thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                return;
            }
            String userId = local.getID();
            profileRepository.getProfile(userId).enqueue(new Callback<java.util.List<UserProfile>>() {
                @Override
                public void onResponse(Call<java.util.List<UserProfile>> call, Response<java.util.List<UserProfile>> response) {
                    if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                        Toast.makeText(Timekeeping.this, "Không tìm thấy profile trên server", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fetchedProfile = response.body().get(0);
                    fetchedAndroidId = fetchedProfile.getAndroid_id();
                    // Read face_id from profile and decide whether to show dialog
                    fetchedFaceId = fetchedProfile.getFace_id();
                    android.util.Log.d("TimekeepingCheck", "Fetched profile id=" + fetchedProfile.getID() + " face_id='" + fetchedFaceId + "'");

                    // Only show the "Chưa thiết lập khuôn mặt" dialog when face_id is null/empty
                    if (fetchedFaceId == null || fetchedFaceId.trim().isEmpty()) {
                        // Temporary feedback: Đã quét
                        showUnknownFaceIdDialog();
                    }
                }

                @Override
                public void onFailure(Call<java.util.List<UserProfile>> call, Throwable t) {
                    Toast.makeText(Timekeeping.this, "Lỗi khi lấy profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showUnknownFaceIdDialog() {
            try {
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogView = inflater.inflate(R.layout.unknown_faceid_notfication, null);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

                // Make dialog window transparent so only our card is visible (same as logout dialog)
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                // If the dialog is dismissed (back button or touch outside), end the activity as requested
                dialog.setOnDismissListener(d -> finish());
                dialog.setOnCancelListener(d -> finish());

                // Wire up the "Quét" button inside the dialog to open face signup (scan) flow
                android.view.View btnSend = dialogView.findViewById(R.id.btnSendRequest);
                if (btnSend != null) {
                    btnSend.setOnClickListener(v -> {
                        try {
                            Intent it = new Intent(Timekeeping.this, dashboard_fragment.FaceSignupActivity.class);
                            // pass user id if available
                            if (fetchedProfile != null && fetchedProfile.getID() != null) {
                                it.putExtra("staff_id", fetchedProfile.getID());
                            }

                            // Before opening FaceSignupActivity, check if there's already a pending request with status 'Chưa duyệt'
                            if (fetchedProfile != null && fetchedProfile.getID() != null) {
                                btnSend.setEnabled(false);
                                TimekeepingRepository repo = new TimekeepingRepository(Timekeeping.this);
                                // Check for pending Face requests specifically
                                repo.getRequestsByStaffIdStatusAndType(fetchedProfile.getID(), "Chưa duyệt", "Face").enqueue(new retrofit2.Callback<java.util.List<java.util.Map<String, Object>>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, retrofit2.Response<java.util.List<java.util.Map<String, Object>>> response) {
                                        btnSend.setEnabled(true);
                                        boolean hasPending = false;
                                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                            hasPending = true;
                                        }

                                        if (hasPending) {
                                            Toast.makeText(Timekeeping.this, "Bạn đã gửi yêu cầu trước đó, vui lòng chờ quản trị viên xét duyệt.", Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                            finish();
                                        } else {
                                            dialog.dismiss();
                                            startActivity(it);
                                        }
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, Throwable t) {
                                        btnSend.setEnabled(true);
                                        // Could not check; inform user and do not open scan to avoid duplicates
                                        Toast.makeText(Timekeeping.this, "Không thể kiểm tra trạng thái yêu cầu, thử lại sau.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                                return;
                            } else {
                                // No profile id available: just open scanner (previous behavior)
                                dialog.dismiss();
                                startActivity(it);
                                return;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(Timekeeping.this, "Không thể mở chức năng quét khuôn mặt.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void showInvalidDeviceDialogAndSendRequest(String deviceAndroidId) {
            try {
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogView = inflater.inflate(R.layout.invalid_device_notfication, null);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

                // Make dialog window transparent so only our card is visible (same as unknown_faceid)
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                // Do NOT send request automatically on dismiss. Only send when user explicitly taps the send button.
                View btnSend = dialogView.findViewById(R.id.btnSendRequest);
                if (btnSend != null) {
                    btnSend.setOnClickListener(v -> {
                        try {
                            // disable to avoid double taps while we check
                            v.setEnabled(false);

                            // If we have a fetched profile id, check for existing pending requests first
                            if (fetchedProfile != null && fetchedProfile.getID() != null) {
                                 TimekeepingRepository repo = new TimekeepingRepository(Timekeeping.this);
                                 // Check for pending DeviceID requests specifically
                                 repo.getRequestsByStaffIdStatusAndType(fetchedProfile.getID(), "Chưa duyệt", "DeviceID")
                                         .enqueue(new retrofit2.Callback<java.util.List<java.util.Map<String, Object>>>() {
                                            @Override
                                            public void onResponse(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, retrofit2.Response<java.util.List<java.util.Map<String, Object>>> response) {
                                                v.setEnabled(true);
                                                boolean hasPending = false;
                                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                                    hasPending = true;
                                                }

                                                if (hasPending) {
                                                    Toast.makeText(Timekeeping.this, "Bạn đã gửi yêu cầu trước đó, vui lòng chờ quản trị viên xét duyệt.", Toast.LENGTH_LONG).show();
                                                    dialog.dismiss();
                                                    finish();
                                                } else {
                                                    // no pending -> send request
                                                    Toast.makeText(Timekeeping.this, "Đã gửi yêu cầu.", Toast.LENGTH_SHORT).show();
                                                    sendTimekeepingAuthRequest(deviceAndroidId);
                                                    dialog.dismiss();
                                                }
                                            }

                                            @Override
                                            public void onFailure(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, Throwable t) {
                                                v.setEnabled(true);
                                                // Could not check; inform user and avoid sending to prevent duplicates
                                                Toast.makeText(Timekeeping.this, "Không thể kiểm tra trạng thái yêu cầu, thử lại sau.", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        });
                            } else {
                                // No profile id available: send request directly (fallback)
                                Toast.makeText(Timekeeping.this, "Đã gửi yêu cầu.", Toast.LENGTH_SHORT).show();
                                sendTimekeepingAuthRequest(deviceAndroidId);
                                dialog.dismiss();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            v.setEnabled(true);
                            Toast.makeText(Timekeeping.this, "Không thể gửi yêu cầu lúc này.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleConfirmAction() {
            // Ensure we have fetched profile info
            if (fetchedProfile == null) {
                Toast.makeText(this, "Chưa có thông tin hồ sơ. Vui lòng chờ và thử lại.", Toast.LENGTH_SHORT).show();
                android.util.Log.d("TimekeepingAction", "handleConfirmAction: no fetchedProfile yet");
                return;
            }

            String deviceAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String profileAndroidId = fetchedAndroidId;

            // First, check device binding: if profile.android_id missing or different -> invalid device flow
            android.util.Log.d("TimekeepingAction", "handleConfirmAction: deviceAndroidId=" + deviceAndroidId + " profileAndroidId=" + profileAndroidId);
            if (profileAndroidId == null || profileAndroidId.trim().isEmpty() || !deviceAndroidId.equals(profileAndroidId)) {
                android.util.Log.d("TimekeepingAction", "handleConfirmAction: device mismatch or not bound -> showInvalidDeviceDialogAndSendRequest");
                showInvalidDeviceDialogAndSendRequest(deviceAndroidId);
                return;
            }

            // Device check passed. Now ensure the staff has a face registered; if not, prompt setup dialog
            if (fetchedFaceId == null || fetchedFaceId.trim().isEmpty()) {
                android.util.Log.d("TimekeepingAction", "handleConfirmAction: profile has no face_id -> showUnknownFaceIdDialog");
                showUnknownFaceIdDialog();
                return;
            }

            // Device matches and face_id exists -> proceed to face verification
            android.util.Log.d("TimekeepingAction", "handleConfirmAction: device matches and face_id present -> performing in-place face verification");
            // perform verification inline in this activity
            verifyFaceNow();
        }

        private void sendTimekeepingAuthRequest(String deviceAndroidId) {
            if (fetchedProfile == null || fetchedProfile.getID() == null) return;
            TimekeepingRepository repo = new TimekeepingRepository(this);
            Call<ResponseBody> call = repo.sendAuthRequest(deviceAndroidId, "DeviceID", fetchedProfile.getID());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(Timekeeping.this, "Không thể gửi yêu cầu (mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(Timekeeping.this, "Lỗi khi gửi yêu cầu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // In-place face verification: capture a frame from cameraPreview, detect face, compute embedding and compare
        private void verifyFaceNow() {
            try {
                Bitmap bmp = cameraPreview.getBitmap();
                if (bmp == null) {
                    Toast.makeText(this, "Không thể lấy ảnh từ camera, thử lại.", Toast.LENGTH_SHORT).show();
                    return;
                }

                InputImage inputImage = InputImage.fromBitmap(bmp, 0);
                detector.process(inputImage)
                        .addOnSuccessListener(faces -> {
                            if (faces == null || faces.isEmpty()) {
                                Toast.makeText(Timekeeping.this, "Không tìm thấy khuôn mặt. Hãy căn đúng vào khung.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Face face = faces.get(0);
                            android.graphics.Rect box = face.getBoundingBox();
                            int left = Math.max(0, box.left);
                            int top = Math.max(0, box.top);
                            int right = Math.min(bmp.getWidth(), box.right);
                            int bottom = Math.min(bmp.getHeight(), box.bottom);

                            if (right - left <= 0 || bottom - top <= 0) {
                                Toast.makeText(Timekeeping.this, "Khuôn mặt không hợp lệ, thử lại.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Bitmap faceBmp = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
                            Bitmap resized = Bitmap.createScaledBitmap(faceBmp, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

                            float[] emb;
                            if (tflite != null) emb = runModel(resized);
                            else {
                                java.util.List<Double> fallback = computeFaceVector(resized);
                                if (fallback == null) { Toast.makeText(Timekeeping.this, "Không thể tạo vector khuôn mặt.", Toast.LENGTH_SHORT).show(); return; }
                                emb = new float[128];
                                for (int i = 0; i < Math.min(128, fallback.size()); i++) emb[i] = fallback.get(i).floatValue();
                            }

                            if (emb == null || emb.length != 128) { Toast.makeText(Timekeeping.this, "Không thể xử lý khuôn mặt. Hãy thử lại.", Toast.LENGTH_SHORT).show(); return; }
                            l2Normalize(emb);

                            // parse expected face vector from fetchedFaceId
                            java.util.List<Double> expectedVec = null;
                            if (fetchedFaceId != null) {
                                try {
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<Double>>(){}.getType();
                                    expectedVec = gson.fromJson(fetchedFaceId, type);
                                } catch (Exception ex) {
                                    try {
                                        String s = fetchedFaceId.replaceAll("[\\[\\]]", "");
                                        String[] parts = s.split(",");
                                        expectedVec = new java.util.ArrayList<>();
                                        for (String p : parts) { if (p.trim().isEmpty()) continue; expectedVec.add(Double.parseDouble(p.trim())); }
                                    } catch (Exception ex2) { expectedVec = null; }
                                }
                            }

                            double distance = -1.0;
                            boolean match = false;
                            if (expectedVec != null && expectedVec.size() >= emb.length) {
                                float[] exp = new float[emb.length];
                                for (int i = 0; i < emb.length; i++) exp[i] = expectedVec.get(i).floatValue();
                                // normalize expected
                                double sum = 0.0; for (float f : exp) sum += f*f; double norm = Math.sqrt(sum); if (norm != 0) for (int i=0;i<exp.length;i++) exp[i] /= norm;
                                double s = 0.0; for (int i = 0; i < emb.length && i < exp.length; i++) { double diff = emb[i] - exp[i]; s += diff*diff; }
                                distance = Math.sqrt(s);
                                double THRESH = 0.8; if (distance <= THRESH) match = true;
                            }

                            android.util.Log.d("TimekeepingAction", "verifyFaceNow: match=" + match + " distance=" + distance);
                            if (match) Toast.makeText(Timekeeping.this, "Xác thực khuôn mặt: thành công (dist=" + String.format(Locale.US, "%.4f", distance) + ")", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(Timekeeping.this, "Xác thực khuôn mặt: thất bại (dist=" + String.format(Locale.US, "%.4f", distance) + ")", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Timekeeping.this, "Lỗi khi phát hiện khuôn mặt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Timekeeping", "Face detection failed", e);
                        });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi xác thực khuôn mặt.", Toast.LENGTH_SHORT).show();
            }
        }

        private void startTimeTracking() {
            timeRunnable = new Runnable() {
                @Override
                public void run() {
                    Calendar now = Calendar.getInstance();
                    SimpleDateFormat tFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    if (tvTime != null) tvTime.setText(tFmt.format(now.getTime()));

                    SimpleDateFormat dFmt = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
                    if (tvDate != null) tvDate.setText(dFmt.format(now.getTime()));

                    timeHandler.postDelayed(this, 1000);
                }
            };
            timeHandler.post(timeRunnable);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (timeHandler != null && timeRunnable != null) timeHandler.removeCallbacks(timeRunnable);
        }
    }
