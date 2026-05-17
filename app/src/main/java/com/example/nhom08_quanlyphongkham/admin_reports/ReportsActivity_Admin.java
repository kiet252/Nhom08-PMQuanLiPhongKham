package com.example.nhom08_quanlyphongkham.admin_reports;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity_Admin extends AppCompatActivity {

    private List<DonKham> danhSachGoc = new ArrayList<>(); 
    private List<DonKham> danhSachKetQua = new ArrayList<>();
    private ReportsAdapter adapter;
    private ReportApiService apiService;

    private boolean isAscNgay = true;
    private boolean isAscTenBN = true;
    private boolean isAscTrangThai = true;
    private boolean isAscTongTien = true;

    private TextView tvTongSoHoaDon, tvTongDoanhThu, tvEmptyState;
    private RecyclerView rvDanhSach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_reports_fragment_admin);

        apiService = SupabaseClientProvider.getClient(this).create(ReportApiService.class);

        ImageButton btnBackCreate    = findViewById(R.id.btnBackCreate);
        EditText    etTenBN          = findViewById(R.id.et_ten_benh_nhan);
        TextView    tvNgayBD         = findViewById(R.id.tv_tu_ngay);
        TextView    tvNgayKT         = findViewById(R.id.tv_den_ngay);
        Button      btnTimKiem       = findViewById(R.id.btn_tim_kiem);
        tvEmptyState                 = findViewById(R.id.tv_empty_state);
        rvDanhSach                   = findViewById(R.id.rv_danh_sach_doanh_thu);
        tvTongSoHoaDon               = findViewById(R.id.tv_tong_so_hoa_don);
        tvTongDoanhThu               = findViewById(R.id.tv_tong_doanh_thu);

        TextView thNgay       = findViewById(R.id.th_ngay);
        TextView thBenhNhan   = findViewById(R.id.th_benh_nhan);
        TextView thTrangThai  = findViewById(R.id.th_trang_thai);
        TextView thTongTien   = findViewById(R.id.th_tong_tien);

        if (rvDanhSach != null) {
            rvDanhSach.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ReportsAdapter(danhSachKetQua);
            rvDanhSach.setAdapter(adapter);
        }

        // ĐÃ XOÁ: Không gọi fetchDataFromServer() ở đây nữa để tránh tải trước

        btnBackCreate.setOnClickListener(v -> finish());
        tvNgayBD.setOnClickListener(v -> hienThiLich(tvNgayBD));
        tvNgayKT.setOnClickListener(v -> hienThiLich(tvNgayKT));

        if (thNgay != null) thNgay.setOnClickListener(v -> sapXepTheoCot("NGAY"));
        if (thBenhNhan != null) thBenhNhan.setOnClickListener(v -> sapXepTheoCot("TEN_BN"));
        if (thTrangThai != null) thTrangThai.setOnClickListener(v -> sapXepTheoCot("TRANG_THAI"));
        if (thTongTien != null) thTongTien.setOnClickListener(v -> sapXepTheoCot("TONG_TIEN"));

        if (btnTimKiem != null) {
            btnTimKiem.setOnClickListener(v -> {
                String tenBN = etTenBN.getText().toString().trim();
                String tuNgay = tvNgayBD.getText().toString().trim();
                String denNgay = tvNgayKT.getText().toString().trim();

                // Bấm nút mới bắt đầu tải và lọc
                fetchDataAndFilter(tenBN, tuNgay, denNgay);
            });
        }
    }

    /**
     * Hàm thực hiện tải dữ liệu mới từ Server và áp dụng bộ lọc ngay sau đó
     */
    private void fetchDataAndFilter(String tenBN, String tuNgay, String denNgay) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang tải dữ liệu...");
        pd.setCancelable(false);
        pd.show();

        apiService.getDanhSachDonKham().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> response) {
                pd.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    danhSachGoc.clear();
                    SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat uiSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    for (ReportItem item : response.body()) {
                        String dateStr = item.getNgay_kham();
                        try {
                            Date date = dbSdf.parse(item.getNgay_kham());
                            if (date != null) dateStr = uiSdf.format(date);
                        } catch (Exception ignored) {}

                        danhSachGoc.add(new DonKham(
                                item.getId(),
                                item.getPatientName(),
                                dateStr,
                                item.getTrang_thai(),
                                item.getPhi_kham()
                        ));
                    }
                    
                    // Sau khi tải xong dữ liệu gốc, thực hiện lọc tại máy local
                    thucHienLocDuLieu(tenBN, tuNgay, denNgay);
                } else {
                    Toast.makeText(ReportsActivity_Admin.this, "Lỗi tải dữ liệu từ máy chủ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                pd.dismiss();
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(ReportsActivity_Admin.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void thucHienLocDuLieu(String tenBN, String tuNgay, String denNgay) {
        // Kiểm tra logic ngày tháng cơ bản
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateBD = null, dateKT = null;

        try {
            if (!tuNgay.contains("ngày") && !tuNgay.isEmpty()) dateBD = sdf.parse(tuNgay);
            if (!denNgay.contains("ngày") && !denNgay.isEmpty()) dateKT = sdf.parse(denNgay);
            
            if (dateBD != null && dateKT != null && dateBD.after(dateKT)) {
                Toast.makeText(this, "Ngày bắt đầu không được sau ngày kết thúc!", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ngày không đúng định dạng!", Toast.LENGTH_SHORT).show();
            return;
        }

        danhSachKetQua.clear();
        String query = tenBN.toLowerCase();

        for (DonKham dk : danhSachGoc) {
            String nameInList = dk.getTenBN().toLowerCase();
            Date dateInList = null;
            try { dateInList = sdf.parse(dk.getNgayKham()); } catch (Exception ignored) {}

            boolean matchTen = query.isEmpty() || nameInList.contains(query);
            boolean matchBD = dateBD == null || (dateInList != null && !dateInList.before(dateBD));
            boolean matchKT = dateKT == null || (dateInList != null && !dateInList.after(dateKT));

            if (matchTen && matchBD && matchKT) {
                danhSachKetQua.add(dk);
            }
        }

        adapter.notifyDataSetChanged();
        capNhatTongKet(danhSachKetQua, tvTongSoHoaDon, tvTongDoanhThu);
        updateUIState();
    }

    private void updateUIState() {
        if (danhSachKetQua.isEmpty()) {
            rvDanhSach.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvDanhSach.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void sapXepTheoCot(String loaiCot) {
        if (danhSachKetQua.isEmpty()) return;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Collections.sort(danhSachKetQua, (dk1, dk2) -> {
            switch (loaiCot) {
                case "NGAY":
                    try {
                        Date n1 = sdf.parse(dk1.getNgayKham()), n2 = sdf.parse(dk2.getNgayKham());
                        return isAscNgay ? n1.compareTo(n2) : n2.compareTo(n1);
                    } catch (Exception e) { return 0; }
                case "TEN_BN":
                    return isAscTenBN ? dk1.getTenBN().compareToIgnoreCase(dk2.getTenBN()) : dk2.getTenBN().compareToIgnoreCase(dk1.getTenBN());
                case "TRANG_THAI":
                    return isAscTrangThai ? dk1.getTrangThai().compareToIgnoreCase(dk2.getTrangThai()) : dk2.getTrangThai().compareToIgnoreCase(dk1.getTrangThai());
                case "TONG_TIEN":
                    return isAscTongTien ? Long.compare(dk1.getTongTien(), dk2.getTongTien()) : Long.compare(dk2.getTongTien(), dk1.getTongTien());
                default: return 0;
            }
        });

        if (loaiCot.equals("NGAY")) isAscNgay = !isAscNgay;
        if (loaiCot.equals("TEN_BN")) isAscTenBN = !isAscTenBN;
        if (loaiCot.equals("TRANG_THAI")) isAscTrangThai = !isAscTrangThai;
        if (loaiCot.equals("TONG_TIEN")) isAscTongTien = !isAscTongTien;
        adapter.notifyDataSetChanged();
    }

    private void capNhatTongKet(List<DonKham> danhSach, TextView tvTongSoHoaDon, TextView tvTongDoanhThu) {
        int soHoaDon = danhSach.size();
        long tongDoanhThu = 0;
        for (DonKham dk : danhSach) {
            if (!"Đã huỷ".equalsIgnoreCase(dk.getTrangThai())) {
                tongDoanhThu += dk.getTongTien();
            }
        }
        if (tvTongSoHoaDon != null) tvTongSoHoaDon.setText(String.valueOf(soHoaDon));
        if (tvTongDoanhThu != null) {
            NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTongDoanhThu.setText(fmt.format(tongDoanhThu) + " đ");
        }
    }

    public static class DonKham {
        private final String MaDonKham, TenBN, NgayKham, TrangThai;
        private final long TongTien;

        public DonKham(String maHoaDon, String tenBenhNhan, String ngayKham, String trangThai, long tongTien) {
            this.MaDonKham = maHoaDon; this.TenBN = tenBenhNhan; this.NgayKham = ngayKham; this.TrangThai = trangThai; this.TongTien = tongTien;
        }

        public String getMaDonKham() { return MaDonKham; }
        public String getTenBN()     { return TenBN; }
        public String getNgayKham()  { return NgayKham; }
        public String getTrangThai() { return TrangThai; }
        public long   getTongTien()  { return TongTien; }
    }

    private void hienThiLich(TextView tv) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            String txt = tv.getText().toString().trim();
            if (txt.contains("/")) calendar.setTime(sdf.parse(txt));
        } catch (Exception ignored) {}

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(22), dp(24), dp(18));
        layout.setBackground(taoNenBoGoc("#FFFFFF", 24));

        DatePicker datePicker = new DatePicker(new ContextThemeWrapper(this, R.style.ReportDatePickerTheme));
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        layout.addView(datePicker);

        TextView btnApDung = taoNutLich("Áp dụng");
        btnApDung.setOnClickListener(v -> {
            tv.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear()));
            tv.setTextColor(Color.parseColor("#1E293B"));
            dialog.dismiss();
        });
        layout.addView(btnApDung);

        dialog.setView(layout);
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private TextView taoNutLich(String text) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        button.setBackground(taoNenBoGoc("#0D3F6E", 12));
        return button;
    }

    private GradientDrawable taoNenBoGoc(String color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }
}
