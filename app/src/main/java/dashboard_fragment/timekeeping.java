package dashboard_fragment;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
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
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class timekeeping extends BaseActivity {

    private ImageButton btnBack;
    private TextView tvTime, tvDate;
    private Spinner spinnerShift;
    private Button btnConfirm;
    private PreviewView cameraPreview;

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    // Đăng ký launcher để yêu cầu quyền Camera
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

        // Kiểm tra quyền camera ngay khi vào màn hình
        checkCameraPermission();
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

                // Cấu hình Preview (Luồng hiển thị lên màn hình)
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                // Chọn Camera trước (Front Camera) để chấm công
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                try {
                    // Huỷ liên kết cũ trước khi bind mới
                    cameraProvider.unbindAll();

                    // Bind camera vào vòng đời của Activity
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
                // Kiểm tra lại quyền trước khi thực hiện logic chấm công
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                } else {
                    Toast.makeText(this, "Đang tiến hành nhận diện và chấm công...", Toast.LENGTH_SHORT).show();
                    // Logic nhận diện khuôn mặt thực tế sẽ gọi ở đây
                }
            });
        }
    }

    private void startTimeTracking() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                SimpleDateFormat tFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
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
