    package dashboard_fragment.timekeeping;

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
    import android.widget.AdapterView;
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

    import java.nio.ByteBuffer;
    import java.nio.ByteOrder;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.List;
    import java.util.Locale;
    import java.util.concurrent.ExecutionException;

    public class Timekeeping extends BaseActivity {

        private ImageButton btnBack;
        private TextView tvTime, tvDate;
        private Button btnConfirm;
        private PreviewView cameraPreview;
        private ProfileRepository profileRepository;
        private UserProfile fetchedProfile;
        private String fetchedAndroidId;
        private String fetchedFaceId;

        private final Handler timeHandler = new Handler(Looper.getMainLooper());
        private Runnable timeRunnable;
        private Spinner spinnerShift;
        private ArrayAdapter<String> adapterCa;
        private List<ca_lam_viec> caLamList = new ArrayList<>();
        private TimekeepingRepository repository;

        private final ActivityResultLauncher<String> requestPermissionLauncher =
                        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (isGranted) {
                                startCamera();
                            } else {
                                Toast.makeText(this, "Bạn cần cấp quyền Camera để thực hiện chấm công!", Toast.LENGTH_LONG).show();
                            }
                        });

            private FaceDetector detector;
            private Interpreter tflite = null;
            private final int MODEL_INPUT_SIZE = 160;

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

            setupListeners();
            startTimeTracking();


            checkCameraPermission();
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();
            detector = FaceDetection.getClient(options);
            new Thread(() -> {
                try {
                    Interpreter loaded = new Interpreter(FaceEmbeddingUtil.loadModelFile(this, "facenet.tflite"));
                    runOnUiThread(() -> tflite = loaded);
                } catch (Exception ex) {
                    Log.w("Timekeeping", "TFLite not loaded: " + ex.getMessage());
                }
            }).start();

            profileRepository = new ProfileRepository(this);
            scanAndCheckProfile();
        }



        private void initializeViews() {

            btnBack = findViewById(R.id.btnBackAdminExDetail);
            tvTime = findViewById(R.id.tv_time);
            tvDate = findViewById(R.id.tv_date);
            spinnerShift = findViewById(R.id.spinner_shift);
            btnConfirm = findViewById(R.id.btn_confirm);
            cameraPreview = findViewById(R.id.camera_preview);
            repository = new TimekeepingRepository(this);
            adapterCa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(List.of("Đang tải...")));
            adapterCa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerShift.setAdapter(adapterCa);
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
            if (spinnerShift != null) {
                spinnerShift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (caLamList == null || caLamList.isEmpty() || position < 0 || position >= caLamList.size()) {
                            return;
                        }
                        ca_lam_viec ca = caLamList.get(position);
                        String caId = ca.getId();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        }

        private void scanAndCheckProfile() {
            com.example.nhom08_quanlyphongkham.UserProfile local = SharedPrefManager.getInstance(this).getProfile();
            if (local == null || local.getID() == null) {
                Toast.makeText(this, "Không có thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                return;
            }
            String userId = local.getID();
            loadCaLamViec(userId);

            profileRepository.getProfile(userId).enqueue(new Callback<java.util.List<UserProfile>>() {
                @Override
                public void onResponse(Call<java.util.List<UserProfile>> call, Response<java.util.List<UserProfile>> response) {
                    if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                        Toast.makeText(Timekeeping.this, "Lỗi thông tin, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fetchedProfile = response.body().get(0);
                    fetchedAndroidId = fetchedProfile.getAndroid_id();
                    fetchedFaceId = fetchedProfile.getFace_id();



                    android.util.Log.d("TimekeepingCheck", "Fetched profile id=" + fetchedProfile.getID() + " face_id='" + fetchedFaceId + "'");

                    if (fetchedFaceId == null || fetchedFaceId.trim().isEmpty()) {
                        showUnknownFaceIdDialog();
                    }
                }

                @Override
                public void onFailure(Call<java.util.List<UserProfile>> call, Throwable t) {
                    Toast.makeText(Timekeeping.this, "Không có kết nối, vui lòng thử lại", Toast.LENGTH_SHORT).show();
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
                            Intent it = new Intent(Timekeeping.this, FaceSignupActivity.class);
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

                            // Pipeline: crop margin -> resize -> embedding -> L2 normalize (dùng chung)
                            float[] emb = FaceEmbeddingUtil.getEmbedding(tflite, bmp, face.getBoundingBox());
                            if (emb == null) {
                                Toast.makeText(Timekeeping.this, "Không thể xử lý khuôn mặt. Hãy thử lại.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Parse embedding đã lưu trong DB
                            java.util.List<Double> expectedVec = FaceEmbeddingUtil.parseVector(fetchedFaceId);

                            double distance = -1.0;
                            boolean match = false;

                            if (expectedVec != null && expectedVec.size() >= emb.length) {
                                float[] exp = new float[emb.length];
                                for (int i = 0; i < emb.length; i++) exp[i] = expectedVec.get(i).floatValue();
                                FaceEmbeddingUtil.l2Normalize(exp);

                                distance = FaceEmbeddingUtil.euclideanDistance(emb, exp);
                                double THRESH = 0.8; // TODO: test lại threshold sau khi đổi normalization
                                if (distance <= THRESH) match = true;
                            } else {
                                Log.e("FaceVerify", "expectedVec invalid: " + (expectedVec == null ? "null" : "size=" + expectedVec.size()));
                            }

                            Log.d("FaceVerify", "match=" + match + " distance=" + distance);

                            if (match) chamCong(distance);
                            else Toast.makeText(Timekeeping.this, "Xác thực khuôn mặt: thất bại (dist=" + String.format(Locale.US, "%.4f", distance) + ")", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Timekeeping.this, "Lỗi khi phát hiện khuôn mặt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi xác thực khuôn mặt.", Toast.LENGTH_SHORT).show();
            }
        }
        private void chamCong(double distance) {

            // Find selected shift in spinner
            if (spinnerShift == null) {
                Toast.makeText(this, "Lỗi: không tìm thấy spinner ca.", Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spinnerShift.getSelectedItemPosition();
            if (caLamList == null || pos < 0 || pos >= caLamList.size()) {
                Toast.makeText(this, "Vui lòng chọn ca làm việc để chấm công.", Toast.LENGTH_SHORT).show();
                return;
            }

            ca_lam_viec ca = caLamList.get(pos);
            String caId = ca.getId();
            if (caId == null || caId.trim().isEmpty()) {
                Toast.makeText(this, "ID ca không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch latest record from server to read current status
            repository.getTimekeepingById(caId).enqueue(new Callback<java.util.List<java.util.Map<String, Object>>>() {
                @Override
                public void onResponse(Call<java.util.List<java.util.Map<String, Object>>> call, Response<java.util.List<java.util.Map<String, Object>>> response) {
                    if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                        Toast.makeText(Timekeeping.this, "Không thể lấy thông tin ca từ server.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    java.util.Map<String, Object> rec = response.body().get(0);
                    String status = rec.get("status") == null ? "" : rec.get("status").toString();

                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                    java.util.Date now = java.util.Calendar.getInstance().getTime();
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    String nowStr = fmt.format(now);

                    if ("Chưa checkin".equalsIgnoreCase(status)) {
                        updates.put("real_start_time", nowStr);
                        // update status to "Đang trong ca" so next check will close it
                        updates.put("status", "Đang trong ca");
                    } else if ("Đang trong ca".equalsIgnoreCase(status)) {
                        updates.put("real_end_time", nowStr);
                        updates.put("status", "Hoàn thành");
                    } else {
                        Toast.makeText(Timekeeping.this, "Không thể chấm công: trạng thái hiện tại = " + status, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Send PATCH update to server
                    repository.updateTimekeepingById(caId, updates).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                if ("Chưa checkin".equalsIgnoreCase(status))
                                Toast.makeText(Timekeeping.this, "Check in thành công.", Toast.LENGTH_SHORT).show();
                                else if ("Đang trong ca".equalsIgnoreCase(status))
                                Toast.makeText(Timekeeping.this, "Check out thành công.", Toast.LENGTH_SHORT).show();
                                else Toast.makeText(Timekeeping.this, "Chấm công thành công.", Toast.LENGTH_SHORT).show();
                                finish();
                                if (fetchedProfile != null && fetchedProfile.getID() != null) {
                                    loadCaLamViec(fetchedProfile.getID());
                                }
                            } else {
                                Toast.makeText(Timekeeping.this, "Lỗi khi cập nhật ca (code=" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(Timekeeping.this, "Lỗi khi cập nhật ca: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Call<java.util.List<java.util.Map<String, Object>>> call, Throwable t) {
                    Toast.makeText(Timekeeping.this, "Không thể lấy thông tin ca: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
        private void loadCaLamViec(String userId)
        {
            repository.getCaLamViecList(userId).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<List<ca_lam_viec>> call, Response<List<ca_lam_viec>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        caLamList = response.body();

                        // Sort shifts so the nearest/upcoming shift appears first in the spinner
                        java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        java.util.Collections.sort(caLamList, (a, b) -> {
                            String sa = a.getStart_time();
                            String sb = b.getStart_time();
                            if (sa == null && sb == null) return 0;
                            if (sa == null) return 1;
                            if (sb == null) return -1;
                            try {
                                java.util.Date da = inFmt.parse(sa);
                                java.util.Date db = inFmt.parse(sb);
                                if (da == null && db == null) return 0;
                                if (da == null) return 1;
                                if (db == null) return -1;
                                return da.compareTo(db); // earlier start_time => comes first
                            } catch (Exception ex) {
                                // fallback to lexical compare
                                return sa.compareTo(sb);
                            }
                        });

                        List<String> displayList = new ArrayList<>();
                        for (ca_lam_viec ca : caLamList) {
                            displayList.add(ca.getDisplayTime());
                        }
                        adapterCa.clear();
                        adapterCa.addAll(displayList);
                        adapterCa.notifyDataSetChanged();
                        // ensure first item is selected (nearest)
                        if (!caLamList.isEmpty()) {
                            spinnerShift.setSelection(0);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<ca_lam_viec>> call, Throwable t) {
                    Log.e("CaLamViec", "Lỗi tải dữ liệu: " + t.getMessage());
                }
            });

        }
    }
