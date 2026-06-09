package dashboard_fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceDetection;
import org.tensorflow.lite.Interpreter;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceSignupActivity extends BaseActivity {

    private PreviewView cameraPreview;
    private Button btnConfirm;
    private ImageButton btnBack;
    private TextView tvGuidance;
    private String staffIdFromIntent;
    private FaceDetector detector;
    private Interpreter tflite = null;
    private final int MODEL_INPUT_SIZE = 160; // use 112 or 160 depending on model

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) startCamera();
                else Toast.makeText(this, "Bạn cần cấp quyền Camera để quét khuôn mặt", Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_signup);

        cameraPreview = findViewById(R.id.camera_preview);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnBack = findViewById(R.id.btnBackAdminExDetail);
        tvGuidance = findViewById(R.id.tv_guidance);

        if (getIntent() != null) {
            staffIdFromIntent = getIntent().getStringExtra("staff_id");
        }

        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> {
            // Capture one frame and run ML Kit face detection + model inference asynchronously
            Bitmap bmp = cameraPreview.getBitmap();
            if (bmp == null) {
                Toast.makeText(this, "Không thể lấy ảnh từ camera, thử lại.", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage inputImage = InputImage.fromBitmap(bmp, 0);
            detector.process(inputImage)
                    .addOnSuccessListener(faces -> {
                        if (faces == null || faces.isEmpty()) {
                            Toast.makeText(FaceSignupActivity.this, "Không tìm thấy khuôn mặt. Hãy căn đúng vào khung.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Face face = faces.get(0);
                        Rect box = face.getBoundingBox();
                        int left = Math.max(0, box.left);
                        int top = Math.max(0, box.top);
                        int right = Math.min(bmp.getWidth(), box.right);
                        int bottom = Math.min(bmp.getHeight(), box.bottom);

                        if (right - left <= 0 || bottom - top <= 0) {
                            Toast.makeText(FaceSignupActivity.this, "Khuôn mặt không hợp lệ, thử lại.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bitmap faceBmp = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
                        Bitmap resized = Bitmap.createScaledBitmap(faceBmp, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

                        float[] emb = null;
                        if (tflite != null) {
                            emb = runModel(resized);
                        } else {
                            // fallback deterministic vector if model absent
                            List<Double> fallback = computeFaceVector(resized);
                            if (fallback == null) {
                                Toast.makeText(FaceSignupActivity.this, "Không thể tạo vector khuôn mặt.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // convert List<Double> to float[]
                            emb = new float[128];
                            for (int i = 0; i < Math.min(128, fallback.size()); i++) emb[i] = fallback.get(i).floatValue();
                        }

                        if (emb == null || emb.length != 128) {
                            Toast.makeText(FaceSignupActivity.this, "Không thể xử lý khuôn mặt. Hãy thử lại.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // L2 normalize
                        l2Normalize(emb);

                        List<Double> vector = new ArrayList<>();
                        for (float f : emb) vector.add((double) f);

                        // Determine if we're running in verify mode
                        boolean isVerifyMode = false;
                        String mode = null;
                        try { mode = getIntent().getStringExtra("mode"); } catch (Exception ignored) {}
                        if (mode != null && mode.equals("verify")) isVerifyMode = true;

                        // Determine staff id (make final so it can be used inside inner classes)
                        String staffIdTemp = staffIdFromIntent;
                        if (staffIdTemp == null || staffIdTemp.isEmpty()) {
                            UserProfile local = SharedPrefManager.getInstance(FaceSignupActivity.this).getProfile();
                            if (local != null) staffIdTemp = local.getID();
                        }
                        final String staffId = staffIdTemp;

                        if (staffId == null) {
                            Toast.makeText(FaceSignupActivity.this, "Không có thông tin nhân viên. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String details = "HƯỚNG DẪN QUÉT KHUÔN MẶT\n" +
                                "1. VỊ TRÍ VÀ TƯ THẾ ĐÚNG: Nhìn thẳng vào camera, mặt trong khung oval, đầu thẳng.\n" +
                                "2. KHOẢNG CÁCH: 30–50 cm là tốt nhất.\n" +
                                "3. ÁNH SÁNG: Đảm bảo ánh sáng từ phía trước, tránh ngược sáng.\n" +
                                "4. CỬ ĐỘNG VÀ XOAY: Khi check-in chỉ cần nhìn thẳng và giữ yên 1–2 giây.";
                        // If running in verify mode, compare the captured vector with expected face from profile
                        if (isVerifyMode) {
                            String expectedFaceRaw = null;
                            try { expectedFaceRaw = getIntent().getStringExtra("expected_face"); } catch (Exception ignored) {}

                            List<Double> expectedVec = null;
                            if (expectedFaceRaw != null) {
                                try {
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<Double>>(){}.getType();
                                    expectedVec = gson.fromJson(expectedFaceRaw, type);
                                } catch (Exception ex) {
                                    // try to parse as simple comma separated if not JSON
                                    try {
                                        String s = expectedFaceRaw.replaceAll("[\\[\\]]", "");
                                        String[] parts = s.split(",");
                                        expectedVec = new ArrayList<>();
                                        for (String p : parts) {
                                            if (p.trim().isEmpty()) continue;
                                            expectedVec.add(Double.parseDouble(p.trim()));
                                        }
                                    } catch (Exception ex2) {
                                        expectedVec = null;
                                    }
                                }
                            }

                            double distance = -1.0;
                            boolean match = false;
                            if (expectedVec != null && expectedVec.size() >= vector.size()) {
                                // convert expectedVec to float[] and l2 norm
                                float[] exp = new float[vector.size()];
                                for (int i = 0; i < vector.size(); i++) exp[i] = expectedVec.get(i).floatValue();
                                // normalize both
                                l2Normalize(emb);
                                // convert captured emb to float[] (already emb)
                                // ensure expected is normalized
                                float sum = 0f; for (float f : exp) sum += f*f; double norm = Math.sqrt(sum); if (norm != 0) for (int i=0;i<exp.length;i++) exp[i] /= norm;
                                double s = 0.0; for (int i = 0; i < emb.length && i < exp.length; i++) { double diff = emb[i] - exp[i]; s += diff * diff; }
                                distance = Math.sqrt(s);
                                double THRESH = 0.9; // tunable
                                if (distance <= THRESH) match = true;
                            }

                            android.content.Intent out = new android.content.Intent();
                            out.putExtra("face_match", match);
                            out.putExtra("face_distance", distance);
                            setResult(RESULT_OK, out);
                            finish();
                            return;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FaceSignupActivity.this, "Lỗi khi phát hiện khuôn mặt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FaceSignup", "Face detection failed", e);
                    });
        });

        // Check permission and start camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Initialize ML Kit detector and try to load the TFLite model
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        detector = FaceDetection.getClient(options);

        try {
            tflite = new Interpreter(loadModelFile("facenet.tflite"));
        } catch (Exception ex) {
            Log.w("FaceSignup", "TFLite not loaded: " + ex.getMessage());
            tflite = null;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
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

    private MappedByteBuffer loadModelFile(String modelFile) throws IOException {
        FileInputStream is = new FileInputStream(getAssets().openFd(modelFile).getFileDescriptor());
        FileChannel fileChannel = is.getChannel();
        long startOffset = getAssets().openFd(modelFile).getStartOffset();
        long declaredLength = getAssets().openFd(modelFile).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Simple deterministic 128-d vector extractor: resize to 16x8 grayscale and return normalized values [-1,1]
    private List<Double> computeFaceVector(Bitmap bmp) {
        try {
            Bitmap small = Bitmap.createScaledBitmap(bmp, 16, 8, true);
            List<Double> vec = new ArrayList<>(128);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 16; x++) {
                    int px = small.getPixel(x, y);
                    int r = (px >> 16) & 0xff;
                    int g = (px >> 8) & 0xff;
                    int b = px & 0xff;
                    double gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                    // normalize to [-1,1]
                    vec.add(gray * 2.0 - 1.0);
                }
            }
            return vec;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private float[] runModel(Bitmap bitmap) {
        int inputSize = MODEL_INPUT_SIZE;
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 1 * inputSize * inputSize * 3);
        input.order(ByteOrder.nativeOrder());

        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        for (int i = 0; i < intValues.length; ++i) {
            int val = intValues[i];
            float r = ((val >> 16) & 0xFF);
            float g = ((val >> 8) & 0xFF);
            float b = (val & 0xFF);
            // normalize to [-1,1]
            input.putFloat((r - 128f) / 128f);
            input.putFloat((g - 128f) / 128f);
            input.putFloat((b - 128f) / 128f);
        }
        input.rewind();

        float[][] output = new float[1][128];
        tflite.run(input, output);
        return output[0];
    }

    private void l2Normalize(float[] v) {
        double sum = 0.0;
        for (float f : v) sum += f * f;
        double norm = Math.sqrt(sum);
        if (norm == 0) return;
        for (int i = 0; i < v.length; i++) v[i] /= norm;
    }
}


