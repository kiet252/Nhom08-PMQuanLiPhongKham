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

    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Locale;
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
            // Start profile/device check and face scan simulation
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
            String[] shifts = {"Ca Sáng (07:30 - 11:30)", "Ca Chiều (13:30 - 17:30)", "Ca Tối (18:00 - 21:00)"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, shifts);
            if (spinnerShift != null) spinnerShift.setAdapter(adapter);
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

                    // Temporary feedback: Đã quét
                    Toast.makeText(Timekeeping.this, "Đã quét", Toast.LENGTH_SHORT).show();

                    if (fetchedAndroidId == null || fetchedAndroidId.trim().isEmpty()) {
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
                            dialog.dismiss();
                            startActivity(it);
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

                dialog.setOnDismissListener(d -> {
                    // after dismiss, show toast and send insert request
                    Toast.makeText(Timekeeping.this, "Đã gửi yêu cầu.", Toast.LENGTH_SHORT).show();
                    sendTimekeepingAuthRequest(deviceAndroidId);
                });

                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleConfirmAction() {
            // Ensure we have fetched profile info
            if (fetchedProfile == null) {
                Toast.makeText(this, "Chưa có thông tin hồ sơ. Vui lòng chờ và thử lại.", Toast.LENGTH_SHORT).show();
                return;
            }

            String deviceAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String profileAndroidId = fetchedAndroidId;

            if (profileAndroidId == null || profileAndroidId.trim().isEmpty()) {
                // Unknown binding: prompt unknown dialog
                showUnknownFaceIdDialog();
                return;
            }

            if (!deviceAndroidId.equals(profileAndroidId)) {
                // Device id mismatch -> show invalid dialog and then send auth request
                showInvalidDeviceDialogAndSendRequest(deviceAndroidId);
            } else {
                // Device matches: normal flow (not specified) -> show success toast
                Toast.makeText(this, "Chấm công thành công.", Toast.LENGTH_SHORT).show();
            }
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
