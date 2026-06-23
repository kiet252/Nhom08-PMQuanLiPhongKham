package dashboard_fragment.timekeeping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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
import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_ANON_KEY;
import static com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider.SUPABASE_URL;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

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

                        // Pipeline dùng chung: crop margin + square -> resize -> embedding -> L2 normalize
                        float[] emb = null;
                        try {
                            if (tflite != null) {
                                emb = FaceEmbeddingUtil.getEmbedding(tflite, bmp, face.getBoundingBox());
                            }
                        } catch (Exception ex) {
                            Log.e("FaceSignup", "getEmbedding threw", ex);
                            emb = null;
                        }

                        // Fallback: if tflite missing or embedding failed, compute a simple fallback vector (deterministic)
                        if (emb == null) {
                            try {
                                emb = computeFaceVector(bmp, face.getBoundingBox());
                            } catch (Exception ex) {
                                Log.e("FaceSignup", "fallback embedding failed", ex);
                                Toast.makeText(FaceSignupActivity.this, "Không thể xử lý khuôn mặt. Hãy thử lại.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

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

                        // Verify mode: so sánh embedding vừa quét với embedding lưu trong intent
                        if (isVerifyMode) {
                            String expectedFaceRaw = null;
                            try { expectedFaceRaw = getIntent().getStringExtra("expected_face"); } catch (Exception ignored) {}

                            List<Double> expectedVec = FaceEmbeddingUtil.parseVector(expectedFaceRaw);

                            double distance = -1.0;
                            boolean match = false;

                            if (expectedVec != null && expectedVec.size() >= emb.length) {
                                float[] exp = new float[emb.length];
                                for (int i = 0; i < emb.length; i++) exp[i] = expectedVec.get(i).floatValue();
                                FaceEmbeddingUtil.l2Normalize(exp);

                                distance = FaceEmbeddingUtil.euclideanDistance(emb, exp);
                                double THRESH = 0.8; // TODO: test lại sau khi đổi normalization
                                if (distance <= THRESH) match = true;
                            } else {
                                Log.e("FaceSignup", "expectedVec invalid: " + (expectedVec == null ? "null" : "size=" + expectedVec.size()));
                            }

                            android.content.Intent out = new android.content.Intent();
                            out.putExtra("face_match", match);
                            out.putExtra("face_distance", distance);
                            setResult(RESULT_OK, out);
                            finish();
                            return;
                        }
                        try {
                            btnConfirm.setEnabled(false);
                            btnConfirm.setText("Đang gửi...");
                        } catch (Exception ignore) {}

                        byte[] faceBytes = null;
                        try {
                            Rect box = face.getBoundingBox();
                            int left = Math.max(0, box.left);
                            int top = Math.max(0, box.top);
                            int right = Math.min(bmp.getWidth(), box.right);
                            int bottom = Math.min(bmp.getHeight(), box.bottom);
                            if (right - left > 0 && bottom - top > 0) {
                                Bitmap faceBmp = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
                                Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, 200, 200, true);
                                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                                scaledFaceBmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                                faceBytes = baos.toByteArray();
                            }
                        } catch (Exception e) {
                            Log.e("FaceSignup", "Failed to extract face bitmap bytes", e);
                        }

                        TimekeepingRepository repo = new TimekeepingRepository(FaceSignupActivity.this);
                        Log.e("FaceSignup", "Preparing to send face auth request for staffId=" + staffId);
                        // Final copies for lambda capture
                        final TimekeepingRepository repoFinal = repo;
                        final String staffIdFinal = staffId;
                        final java.util.List<Double> vectorFinal = vector;
                        final String detailsFinal = details;
                        final byte[] faceBytesFinal = faceBytes;

                        // If we have face bytes, upload to Supabase Storage (bucket 'face') in background,
                        // then send the auth request with the public URL stored in `face_image`.
                        if (faceBytes != null) {
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());
                            executor.execute(() -> {
                                String fileName = "face_" + staffIdFinal + "_" + System.currentTimeMillis() + ".jpg";
                                String uploadUrl = SUPABASE_URL + "storage/v1/object/face/" + fileName;
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(uploadUrl)
                                        .post(RequestBody.create(faceBytesFinal, MediaType.parse("image/jpeg")))
                                        .addHeader("apikey", SUPABASE_ANON_KEY)
                                        .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                                        .build();
                                try (okhttp3.Response response = client.newCall(request).execute()) {
                                    if (!response.isSuccessful()) {
                                        String err = null;
                                        try { if (response.body() != null) err = response.body().string(); } catch (Exception ignored) {}
                                        throw new IOException("Upload failed: code=" + response.code() + " body=" + err);
                                    }
                                    String publicUrl = SUPABASE_URL + "storage/v1/object/public/face/" + fileName;
                                    handler.post(() -> {
                                        Log.e("FaceSignup", "Uploaded face image, publicUrl=" + publicUrl);
                                        repoFinal.sendFaceAuthRequest(staffIdFinal, vectorFinal, detailsFinal, publicUrl).enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                try {
                                                    if (response.isSuccessful()) {
                                                        Toast.makeText(FaceSignupActivity.this, "Đã gửi yêu cầu quét khuôn mặt.", Toast.LENGTH_LONG).show();
                                                        finish();
                                                        return;
                                                    } else {
                                                        int code = response.code();
                                                        String err = null;
                                                        try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ex) { }
                                                        Log.e("FaceSignup", "Face auth request failed. code=" + code + " err=" + err);
                                                        if (code == 400) {
                                                            repoFinal.sendFaceAuthRequestAsString(staffIdFinal, vectorFinal, detailsFinal, publicUrl).enqueue(new Callback<ResponseBody>() {
                                                                @Override
                                                                public void onResponse(Call<ResponseBody> call2, Response<ResponseBody> resp2) {
                                                                    if (resp2.isSuccessful()) {
                                                                        Toast.makeText(FaceSignupActivity.this, "Đã gửi yêu cầu (fallback).", Toast.LENGTH_LONG).show();
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(FaceSignupActivity.this, "Gửi yêu cầu thất bại (mã: " + resp2.code() + ").", Toast.LENGTH_LONG).show();
                                                                        btnConfirm.setEnabled(true);
                                                                        btnConfirm.setText("Quét");
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<ResponseBody> call2, Throwable t2) {
                                                                    Log.e("FaceSignup", "Fallback send failed", t2);
                                                                    Toast.makeText(FaceSignupActivity.this, "Lỗi khi gửi yêu cầu: " + t2.getMessage(), Toast.LENGTH_LONG).show();
                                                                    btnConfirm.setEnabled(true);
                                                                    btnConfirm.setText("Quét");
                                                                }
                                                            });
                                                            return;
                                                        }
                                                    }
                                                } finally {
                                                    try { btnConfirm.setEnabled(true); btnConfirm.setText("Quét"); } catch (Exception ignore) {}
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Log.e("FaceSignup", "sendFaceAuthRequest onFailure", t);
                                                Toast.makeText(FaceSignupActivity.this, "Lỗi khi gửi yêu cầu: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                                try { btnConfirm.setEnabled(true); btnConfirm.setText("Quét"); } catch (Exception ignore) {}
                                            }
                                        });
                                    });
                                } catch (Exception uploadEx) {
                                    handler.post(() -> {
                                        Log.e("FaceSignup", "Failed to upload face image", uploadEx);
                                        Toast.makeText(FaceSignupActivity.this, "Không thể tải ảnh lên máy chủ: " + uploadEx.getMessage(), Toast.LENGTH_LONG).show();
                                        try { btnConfirm.setEnabled(true); btnConfirm.setText("Quét"); } catch (Exception ignore) {}
                                    });
                                }
                            });
                            } else {
                            // No face image bytes, send request without image URL
                            repoFinal.sendFaceAuthRequest(staffIdFinal, vectorFinal, detailsFinal, null).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(FaceSignupActivity.this, "Đã gửi yêu cầu quét khuôn mặt.", Toast.LENGTH_LONG).show();
                                            finish();
                                            return;
                                        } else {
                                            int code = response.code();
                                            String err = null;
                                            try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ex) { }
                                            Log.e("FaceSignup", "Face auth request failed. code=" + code + " err=" + err);
                                            if (code == 400) {
                                                repoFinal.sendFaceAuthRequestAsString(staffIdFinal, vectorFinal, detailsFinal, null).enqueue(new Callback<ResponseBody>() {
                                                    @Override
                                                    public void onResponse(Call<ResponseBody> call2, Response<ResponseBody> resp2) {
                                                        if (resp2.isSuccessful()) {
                                                            Toast.makeText(FaceSignupActivity.this, "Đã gửi yêu cầu (fallback).", Toast.LENGTH_LONG).show();
                                                            finish();
                                                        } else {
                                                            Toast.makeText(FaceSignupActivity.this, "Gửi yêu cầu thất bại (mã: " + resp2.code() + ").", Toast.LENGTH_LONG).show();
                                                            btnConfirm.setEnabled(true);
                                                            btnConfirm.setText("Quét");
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<ResponseBody> call2, Throwable t2) {
                                                        Log.e("FaceSignup", "Fallback send failed", t2);
                                                        Toast.makeText(FaceSignupActivity.this, "Lỗi khi gửi yêu cầu: " + t2.getMessage(), Toast.LENGTH_LONG).show();
                                                        btnConfirm.setEnabled(true);
                                                        btnConfirm.setText("Quét");
                                                    }
                                                });
                                                return;
                                            }
                                        }
                                    } finally {
                                        try { btnConfirm.setEnabled(true); btnConfirm.setText("Quét"); } catch (Exception ignore) {}
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e("FaceSignup", "sendFaceAuthRequest onFailure", t);
                                    Toast.makeText(FaceSignupActivity.this, "Lỗi khi gửi yêu cầu: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                    try { btnConfirm.setEnabled(true); btnConfirm.setText("Quét"); } catch (Exception ignore) {}
                                }
                            });
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
            tflite = new Interpreter(FaceEmbeddingUtil.loadModelFile(this, "facenet.tflite"));
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

    private float[] computeFaceVector(Bitmap bmp, Rect box) {
        if (bmp == null || box == null) return null;
        try {
            int left = Math.max(0, box.left);
            int top = Math.max(0, box.top);
            int right = Math.min(bmp.getWidth(), box.right);
            int bottom = Math.min(bmp.getHeight(), box.bottom);
            if (right - left <= 0 || bottom - top <= 0) return null;

            Bitmap faceBmp = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
            Bitmap small = Bitmap.createScaledBitmap(faceBmp, 16, 8, true);
            float[] vec = new float[128];
            int idx = 0;
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 16; x++) {
                    int px = small.getPixel(x, y);
                    int r = (px >> 16) & 0xff;
                    int g = (px >> 8) & 0xff;
                    int b = px & 0xff;
                    double gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                    vec[idx++] = (float) (gray * 2.0 - 1.0);
                }
            }
            return vec;
        } catch (Exception e) {
            Log.e("FaceSignup", "computeFaceVector failed", e);
            return null;
        }
    }
}


