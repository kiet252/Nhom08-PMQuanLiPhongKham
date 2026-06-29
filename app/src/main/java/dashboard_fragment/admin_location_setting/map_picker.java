package dashboard_fragment.admin_location_setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;

import java.util.Locale;

public class map_picker extends BaseActivity {

    private WebView webViewMapPicker;
    private Button btnConfirmLocation;
    private ImageButton btnBack;
    private TextView tvSelectedCoords;

    private double selectedLat = 0;
    private double selectedLng = 0;
    private int selectedRadius = 100;
    private boolean hasSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_picker);

        btnBack = findViewById(R.id.btnBackMapPicker);
        webViewMapPicker = findViewById(R.id.webViewMapPicker);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        tvSelectedCoords = findViewById(R.id.tvSelectedCoords);

        // Nhận tọa độ hiện tại nếu có
        double initLat = getIntent().getDoubleExtra("lat", 10.7769); // mặc định HCM
        double initLng = getIntent().getDoubleExtra("lng", 106.7009);
        selectedRadius = getIntent().getIntExtra("radius", 100);

        btnBack.setOnClickListener(v -> finish());

        btnConfirmLocation.setOnClickListener(v -> {
            if (!hasSelected) {
                Toast.makeText(this, "Vui lòng tap vào bản đồ để chọn vị trí.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent result = new Intent();
            result.putExtra("lat", selectedLat);
            result.putExtra("lng", selectedLng);
            result.putExtra("radius", selectedRadius);
            setResult(RESULT_OK, result);
            finish();
        });

        setupWebView(initLat, initLng, selectedRadius);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(double lat, double lng, int radius) {
        WebSettings settings = webViewMapPicker.getSettings();
        settings.setJavaScriptEnabled(true);

        // 1. THÊM USER-AGENT ĐỂ KHÔNG BỊ BLOCKED
        settings.setUserAgentString("Nhom08_QuanLyPhongKham/1.0");

        // 2. Xóa cache ảnh bị block cũ
        webViewMapPicker.clearCache(true);

        webViewMapPicker.addJavascriptInterface(new MapJsInterface(), "Android");

        // 3. ĐỔI SANG SERVER CARTODB VOYAGER (Cực mượt, không bao giờ chặn WebView)
        String html = "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>html,body,#map{width:100%;height:100%;margin:0;padding:0;}</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map').setView([" + lat + "," + lng + "], 16);" +

                // Sửa dòng TileLayer ở đây
                "L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png').addTo(map);" +

                "var marker = null;" +
                "var circle = null;" +
                "map.on('click', function(e) {" +
                "  var lat = e.latlng.lat;" +
                "  var lng = e.latlng.lng;" +
                "  if (marker) map.removeLayer(marker);" +
                "  if (circle) map.removeLayer(circle);" +
                "  marker = L.marker([lat, lng]).addTo(map);" +
                "  circle = L.circle([lat, lng], {radius:" + radius + ", color:'#2563EB', fillOpacity:0.15}).addTo(map);" +
                "  Android.onLocationSelected(lat, lng);" +
                "});" +
                "</script></body></html>";

        // 4. SỬA BASE URL THÀNH localhost
        webViewMapPicker.loadDataWithBaseURL("http://localhost/", html, "text/html", "UTF-8", null);
    }

    private class MapJsInterface {
        @JavascriptInterface
        public void onLocationSelected(double lat, double lng) {
            selectedLat = lat;
            selectedLng = lng;
            hasSelected = true;
            runOnUiThread(() -> {
                tvSelectedCoords.setText(String.format(Locale.getDefault(),
                        "Đã chọn: %.4f°N, %.4f°E", lat, lng));
            });
        }
    }
}