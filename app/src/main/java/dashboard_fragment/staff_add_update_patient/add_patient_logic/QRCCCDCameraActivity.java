package dashboard_fragment.staff_add_update_patient.add_patient_logic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExperimentalGetImage
public class QRCCCDCameraActivity extends BaseActivity {
    private ImageButton btnBack;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;

    private boolean scanned = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_cccd_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();
        setupListeners();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
        else {
            requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }
    private void setupListeners() {
        btnBack.setOnClickListener(v -> backToPreviousActivity());
    }
    private void backToPreviousActivity() {
        finish();
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startCamera();
                        } else {
                            finish();
                        }
                    });
    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                ResolutionSelector selector =
                        new ResolutionSelector.Builder()
                                .setResolutionStrategy(
                                        new ResolutionStrategy(
                                                new Size(1280, 720),
                                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                        )
                                )
                                .build();
                Preview preview = new Preview.Builder()
                        .setResolutionSelector(selector)
                        .build();

                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider()
                );

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setResolutionSelector(selector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                BarcodeScannerOptions options =
                        new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .build();

                BarcodeScanner scanner =
                        BarcodeScanning.getClient(options);

                imageAnalysis.setAnalyzer(
                        cameraExecutor,
                        imageProxy -> analyzeImage(imageProxy, scanner)
                );
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void analyzeImage(ImageProxy imageProxy, BarcodeScanner scanner) {
        if (scanned) {
            imageProxy.close();
            return;
        }
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        InputImage inputImage =
                InputImage.fromMediaImage(
                        imageProxy.getImage(),
                        imageProxy.getImageInfo().getRotationDegrees()
                );
        scanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String raw = barcode.getRawValue();

                        if (raw != null && !raw.isEmpty()) {
                            scanned = true;
                            returnResult(raw);
                            break;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }
    private void returnResult(String raw) {
        Intent intent = new Intent();
        intent.putExtra("qr_raw", raw);

        if (raw != null && !raw.isEmpty()) {
            String[] parts = raw.split("\\|", -1);
            if (parts.length >= 6) {
                String idNumber = parts[0].trim();
                String fullName = parts[2].trim();
                String dobRaw = parts[3].trim();
                String gender = parts[4].trim();
                String address = parts[5].trim();

                String dob = dobRaw;
                if (dobRaw.length() == 8) {
                    dob = dobRaw.substring(0, 2) + "/" + dobRaw.substring(2, 4) + "/" + dobRaw.substring(4, 8);
                }

                intent.putExtra("id_number", idNumber);
                intent.putExtra("full_name", fullName);
                intent.putExtra("dob", dob);
                intent.putExtra("gender", gender);
                intent.putExtra("address", address);
            } else {
                android.widget.Toast.makeText(this, "QR không đúng cấu trúc CCCD: " + raw, android.widget.Toast.LENGTH_LONG).show();
            }
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (cameraExecutor != null) {

            cameraExecutor.shutdown();

        }

    }
}