package dashboard_fragment.staff_add_update_patient.add_patient_logic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRCameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageButton btnClose;
    private ImageButton btnCapture;
    private ImageCapture imageCapture;

    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ocr_camera);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainOCRCamera), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Bạn cần cấp quyền camera để quét CCCD", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );

        btnClose.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        btnCapture.setOnClickListener(v -> takePhoto());

        ensureCameraPermissionAndStart();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        btnClose = findViewById(R.id.btnClose);
        btnCapture = findViewById(R.id.btnCapture);
    }

    private void ensureCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e("OCR_CAMERA", "Cannot start camera", e);
                Toast.makeText(this, "Không thể mở camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCapture.setEnabled(false);

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        android.media.Image mediaImage = imageProxy.getImage();

                        if (mediaImage == null) {
                            imageProxy.close();
                            btnCapture.setEnabled(true);
                            Toast.makeText(OCRCameraActivity.this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        InputImage image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.getImageInfo().getRotationDegrees()
                        );

                        processImage(image, imageProxy);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        btnCapture.setEnabled(true);
                        Toast.makeText(OCRCameraActivity.this, "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void processImage(InputImage image, ImageProxy imageProxy) {
        TextRecognizer recognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String rawText = visionText.getText();
                    Log.d("OCR_RAW", rawText);

                    parseCCCDInfo(rawText);
                    imageProxy.close();
                    btnCapture.setEnabled(true);
                    recognizer.close();
                })
                .addOnFailureListener(e -> {
                    Log.e("OCR_ERROR", "OCR failed", e);
                    imageProxy.close();
                    btnCapture.setEnabled(true);
                    recognizer.close();

                    Toast.makeText(this, "Lỗi OCR", Toast.LENGTH_SHORT).show();
                });
    }

    private void parseCCCDInfo(String rawText) {
        String[] lines = rawText.split("\n");

        String id = extractCCCD(rawText, lines);
        String dob = extractDob(rawText, lines);
        String gender = extractGender(lines);
        String name = extractFullName(lines);
        String address = extractAddress(lines);

        Log.d("OCR_RESULT", "Name = " + name);
        Log.d("OCR_RESULT", "CCCD = " + id);
        Log.d("OCR_RESULT", "DOB = " + dob);
        Log.d("OCR_RESULT", "Gender = " + gender);
        Log.d("OCR_RESULT", "Address = " + address);

        Intent resultIntent = new Intent();
        if (name != null) resultIntent.putExtra("full_name", name);
        if (id != null) resultIntent.putExtra("id_number", id);
        if (dob != null) resultIntent.putExtra("dob", dob);
        if (gender != null) resultIntent.putExtra("gender", gender);
        if (address != null) resultIntent.putExtra("address", address);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private String extractCCCD(String rawText, String[] lines) {
        Pattern pattern = Pattern.compile("(?<!\\d)(\\d{12})(?!\\d)");
        Matcher matcher = pattern.matcher(rawText);
        if (matcher.find()) {
            return matcher.group(1);
        }

        for (String line : lines) {
            String digits = line.replaceAll("[^0-9]", "");
            if (digits.length() == 12) {
                return digits;
            }
        }

        return null;
    }

    private String extractDob(String rawText, String[] lines) {

        Pattern datePattern =
                Pattern.compile("(\\d{2}[/-]\\d{2}[/-]\\d{4})");

        for (int i = 0; i < lines.length; i++) {

            String line = normalizeText(lines[i]);

            if (line.contains("ngay sinh")
                    || line.contains("date of birth")
                    || line.contains("birth")) {

                Matcher matcher =
                        datePattern.matcher(lines[i]);

                if (matcher.find()) {
                    return matcher.group(1).replace('-', '/');
                }

                if (i + 1 < lines.length) {

                    matcher =
                            datePattern.matcher(lines[i + 1]);

                    if (matcher.find()) {
                        return matcher.group(1).replace('-', '/');
                    }
                }
            }
        }

        return null;
    }

    private String extractGender(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = normalizeText(lines[i]);

            if (line.contains("gioi tinh") || line.contains("sex") || line.contains("gender")) {
                if (line.contains("nam")) return "Nam";
                if (line.contains("nu") || line.contains("female")) return "Nữ";

                if (i + 1 < lines.length) {
                    String next = normalizeText(lines[i + 1]);
                    if (next.contains("nam")) return "Nam";
                    if (next.contains("nu") || next.contains("female")) return "Nữ";
                }
            }

            if (line.equals("nam")) return "Nam";
            if (line.equals("nu") || line.equals("nu.")) return "Nữ";
        }

        return null;
    }

    private String extractFullName(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = normalizeText(lines[i]);

            if (line.contains("ho va ten") || line.contains("full name") || line.contains("ho ten")) {
                if (i + 1 < lines.length) {
                    String candidate = cleanText(lines[i + 1]);

                    if (candidate.length() > 5
                            && candidate.length() < 40
                            && !candidate.matches(".*\\d.*")) {

                        return candidate.toUpperCase(Locale.ROOT);
                    }
                }

                String current = cleanText(lines[i]);
                int index = current.indexOf(":");
                if (index >= 0 && index + 1 < current.length()) {
                    String value = current.substring(index + 1).trim();
                    if (!value.isEmpty()) return value.toUpperCase(Locale.ROOT);
                }
            }
        }

        for (String line : lines) {
            String cleaned = cleanText(line);
            String normalized = normalizeText(cleaned);

            if (normalized.contains("cong hoa")) continue;
            if (normalized.contains("doc lap")) continue;
            if (normalized.contains("viet nam")) continue;
            if (normalized.contains("can cuoc")) continue;
            if (normalized.contains("identity")) continue;
            if (normalized.contains("citizen")) continue;
            if (normalized.contains("gioi tinh")) continue;
            if (normalized.contains("ngay sinh")) continue;

            if (cleaned.length() >= 6 && cleaned.length() <= 40) {
                if (normalized.matches("[a-z\\s]+") && !normalized.contains("thue") && !normalized.contains("cccd")) {
                    if (Character.isUpperCase(cleaned.charAt(0)) || cleaned.equals(cleaned.toUpperCase(Locale.ROOT))) {
                        return cleaned.toUpperCase(Locale.ROOT);
                    }
                }
            }
        }

        return null;
    }

    private String extractAddress(String[] lines) {

        for (int i = 0; i < lines.length; i++) {

            String normalized = normalizeText(lines[i]);

            if (normalized.contains("que quan")) {

                StringBuilder address = new StringBuilder();

                String current = cleanText(lines[i]);

                if (current.contains(":")) {
                    String value = current.substring(current.indexOf(":") + 1).trim();

                    if (!value.isEmpty()) {
                        address.append(value);
                    }
                }

                for (int j = i + 1; j < lines.length; j++) {

                    String next = cleanText(lines[j]);
                    String nextNormalized = normalizeText(next);

                    if (next.isEmpty()) {
                        continue;
                    }

                    if (nextNormalized.contains("noi thuong tru")
                            || nextNormalized.contains("ho va ten")
                            || nextNormalized.contains("ngay sinh")
                            || nextNormalized.contains("gioi tinh")
                            || nextNormalized.contains("sex")
                            || nextNormalized.contains("quoc tich")
                            || nextNormalized.contains("dan toc")
                            || nextNormalized.contains("can cuoc")
                            || nextNormalized.contains("cccd")) {
                        break;
                    }

                    if (address.length() > 0) {
                        address.append(", ");
                    }

                    address.append(next);
                }

                String result = address.toString().trim();

                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return null;
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("\\s+", " ").trim();
    }

    private String normalizeText(String text) {
        String normalized = Normalizer.normalize(cleanText(text), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }
}