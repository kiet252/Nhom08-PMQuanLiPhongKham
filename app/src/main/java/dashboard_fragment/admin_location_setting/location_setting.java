package dashboard_fragment.admin_location_setting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class location_setting extends BaseActivity {

    private ClinicLocationRepository repository;
    private FusedLocationProviderClient fusedLocationClient;
    private ClinicLocation currentLocation; // null nếu chưa có

    // Views
    private ProgressBar progressBar;
    private LinearLayout layoutHasLocation;
    private LinearLayout layoutNoLocation;
    private TextView tvLocationName, tvLocationAddress;
    private TextView tvLatitude, tvLongitude, tvRadius, tvRadiusNote, tvLastUpdated;
    private Button btnEditLocation, btnCreateLocation;
    private ImageButton btnBack, btnRefresh;

    // WebView để hiện bản đồ OSM
    private android.webkit.WebView webViewMap;

    private final ActivityResultLauncher<String[]> locationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (Boolean.TRUE.equals(fine)) {
                    fetchCurrentLocationAndSave();
                } else {
                    Toast.makeText(this, "Cần quyền vị trí!", Toast.LENGTH_SHORT).show();
                }
            });

    // Nhận kết quả từ màn hình chọn bản đồ
    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("lat", 0);
                    double lng = result.getData().getDoubleExtra("lng", 0);
                    int radius = result.getData().getIntExtra("radius", 100);
                    saveLocation(lat, lng, radius);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_location_setting);

        repository = new ClinicLocationRepository(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupListeners();
        loadLocation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackLocation);
        btnRefresh = findViewById(R.id.btnRefreshLocation);
        progressBar = findViewById(R.id.progressLocation);
        layoutHasLocation = findViewById(R.id.layoutHasLocation);
        layoutNoLocation = findViewById(R.id.layoutNoLocation);
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvRadius = findViewById(R.id.tvRadius);
        tvRadiusNote = findViewById(R.id.tvRadiusNote);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        btnEditLocation = findViewById(R.id.btnEditLocation);
        btnCreateLocation = findViewById(R.id.btnCreateLocation);
        webViewMap = findViewById(R.id.webViewMap);

        // Enable JS cho WebView OSM
        webViewMap.getSettings().setJavaScriptEnabled(true);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadLocation());

        // Nút tạo mới (khi chưa có vị trí)
        btnCreateLocation.setOnClickListener(v -> showChooseMethodDialog());

        // Nút chỉnh sửa (khi đã có vị trí)
        btnEditLocation.setOnClickListener(v -> showChooseMethodDialog());
    }

    private void loadLocation() {
        showLoading(true);
        repository.getLocation().enqueue(new Callback<List<ClinicLocation>>() {
            @Override
            public void onResponse(Call<List<ClinicLocation>> call, Response<List<ClinicLocation>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentLocation = response.body().get(0);
                    showLocationUI(currentLocation);
                } else {
                    currentLocation = null;
                    showEmptyUI();
                }
            }

            @Override
            public void onFailure(Call<List<ClinicLocation>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(location_setting.this,
                        "Lỗi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyUI();
            }
        });
    }

    private void showLocationUI(ClinicLocation loc) {
        layoutNoLocation.setVisibility(View.GONE);
        layoutHasLocation.setVisibility(View.VISIBLE);

        // 1. Kiểm tra null an toàn cho tọa độ
        double lat = (loc.getLatitude() != null) ? loc.getLatitude() : 0.0;
        double lng = (loc.getLongitude() != null) ? loc.getLongitude() : 0.0;

        tvLatitude.setText(String.format(Locale.getDefault(), "%.4f°N", lat));
        tvLongitude.setText(String.format(Locale.getDefault(), "%.4f°E", lng));

        int radius = loc.getMax_distance_meters() != null ? loc.getMax_distance_meters() : 100;
        tvRadius.setText(radius + "m");
        tvRadiusNote.setText(radius + "m");

        // Geocode để lấy tên địa chỉ
        resolveAddress(lat, lng);
        // Load bản đồ OSM qua WebView
        loadOsmMap(lat, lng, radius);

        // Thời gian cập nhật
        if (loc.getRelated_fixed_time() != null) {
            try {
                String rawTime = loc.getRelated_fixed_time();

                // Cắt bỏ phần thập phân của giây và múi giờ (từ dấu chấm hoặc dấu cộng trở đi)
                // Để đưa về chuẩn cơ bản: yyyy-MM-dd'T'HH:mm:ss
                if (rawTime.contains(".")) {
                    rawTime = rawTime.substring(0, rawTime.indexOf("."));
                } else if (rawTime.contains("+")) {
                    rawTime = rawTime.substring(0, rawTime.indexOf("+"));
                }

                // Parse chuỗi gốc và format lại theo chuẩn Việt Nam
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                Date date = inputFormat.parse(rawTime);
                tvLastUpdated.setText("Cập nhật lần cuối: " + outputFormat.format(date));

            } catch (Exception e) {
                // Nếu có lỗi trong quá trình parse, fallback về chuỗi gốc của API
                tvLastUpdated.setText("Cập nhật lần cuối: " + loc.getRelated_fixed_time());
            }
        } else {
            String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            tvLastUpdated.setText("Cập nhật lần cuối: " + now);
        }
    }

    private void showEmptyUI() {
        layoutHasLocation.setVisibility(View.GONE);
        layoutNoLocation.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showChooseMethodDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chọn vị trí bằng cách nào?")
                .setItems(new String[]{"📍 Lấy vị trí hiện tại", "🗺️ Chọn trên bản đồ"}, (dialog, which) -> {
                    if (which == 0) {
                        checkPermissionAndFetchLocation();
                    } else {
                        openMapPicker();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void checkPermissionAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocationAndSave();
        } else {
            locationPermLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchCurrentLocationAndSave() {
        showLoading(true);
        CurrentLocationRequest req = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(5000)
                .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(req, null)
                .addOnSuccessListener(location -> {
                    showLoading(false);
                    if (location == null) {
                        Toast.makeText(this, "Không lấy được vị trí, bật GPS và thử lại.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Mặc định bán kính 100m, có thể cho user chỉnh
                    showRadiusDialog(location.getLatitude(), location.getLongitude());
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRadiusDialog(double lat, double lng) {
        String[] radiusOptions = {"50m", "100m", "200m", "500m"};
        int[] radiusValues = {50, 100, 200, 500};
        final int[] selected = {1}; // mặc định 100m

        new AlertDialog.Builder(this)
                .setTitle("Chọn bán kính chấm công")
                .setSingleChoiceItems(radiusOptions, selected[0], (dialog, which) -> selected[0] = which)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    saveLocation(lat, lng, radiusValues[selected[0]]);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openMapPicker() {
        Intent intent = new Intent(this, dashboard_fragment.admin_location_setting.map_picker.class);
        if (currentLocation != null) {
            intent.putExtra("lat", currentLocation.getLatitude());
            intent.putExtra("lng", currentLocation.getLongitude());
            intent.putExtra("radius", currentLocation.getMax_distance_meters() != null
                    ? currentLocation.getMax_distance_meters() : 100);
        }
        mapPickerLauncher.launch(intent);
    }

    private void saveLocation(double lat, double lng, int radius) {
        showLoading(true);
        Callback<List<ClinicLocation>> callback = new Callback<List<ClinicLocation>>() {
            @Override
            public void onResponse(Call<List<ClinicLocation>> call, Response<List<ClinicLocation>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(location_setting.this, "Đã lưu vị trí!", Toast.LENGTH_SHORT).show();
                    loadLocation(); // reload lại UI
                } else {
                    Toast.makeText(location_setting.this,
                            "Lỗi lưu vị trí (code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClinicLocation>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(location_setting.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (currentLocation == null) {
            repository.createLocation(lat, lng, radius).enqueue(callback);
        } else {
            repository.updateLocation(currentLocation.getId(), lat, lng, radius).enqueue(callback);
        }
    }

    private void resolveAddress(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
                List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address addr = addresses.get(0);
                    String name = addr.getFeatureName();
                    String address = addr.getAddressLine(0);
                    runOnUiThread(() -> {
                        if (name != null) tvLocationName.setText(name);
                        if (address != null) tvLocationAddress.setText(address);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvLocationName.setText("Vị trí đã chọn");
                    tvLocationAddress.setText(String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng));
                });
            }
        }).start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadOsmMap(double lat, double lng, int radiusMeters) {
        WebSettings settings = webViewMap.getSettings();
        settings.setJavaScriptEnabled(true);

        // Đặt User-Agent tùy chỉnh
        settings.setUserAgentString("Nhom08_QuanLyPhongKham/1.0");

        // 1. RẤT QUAN TRỌNG: Xóa cache cũ đi để nó không hiện lại ảnh lỗi "Access blocked"
        webViewMap.clearCache(true);

        // 2. MÃ HTML (Đã đổi server bản đồ sang CartoDB để chống chặn tuyệt đối)
        String html = "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>html,body,#map{width:100%;height:100%;margin:0;padding:0;}</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl:true, attributionControl:false}).setView([" + lat + "," + lng + "], 16);" +

                // THAY ĐỔI Ở ĐÂY: Dùng server của CartoDB (dữ liệu y hệt OSM, đẹp và không bao giờ block WebView)
                "L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png').addTo(map);" +

                "L.marker([" + lat + "," + lng + "]).addTo(map);" +
                "L.circle([" + lat + "," + lng + "], {radius:" + radiusMeters + ", color:'#2563EB', fillOpacity:0.15}).addTo(map);" +
                "</script></body></html>";

        // 3. THAY ĐỔI Ở ĐÂY: Dùng "http://localhost/" thay vì trang chủ của OSM để không bị bắt lỗi Referer
        webViewMap.loadDataWithBaseURL("http://localhost/", html, "text/html", "UTF-8", null);
    }
}