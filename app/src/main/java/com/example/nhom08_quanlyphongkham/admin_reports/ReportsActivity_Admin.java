package com.example.nhom08_quanlyphongkham.admin_reports;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity_Admin extends BaseActivity {

    private static final int TAB_PHIEU_KHAM = 0;
    private static final int TAB_BILL = 1;

    private final List<DonKham> danhSachGoc = new ArrayList<>();
    private final List<DonKham> danhSachKetQua = new ArrayList<>();
    private final List<BillThongKe> danhSachBillGoc = new ArrayList<>();
    private final List<BillThongKe> danhSachBillKetQua = new ArrayList<>();

    private ReportsAdapter adapter;
    private BillReportsAdapter billAdapter;
    private ReportApiService apiService;

    private int currentTab = TAB_PHIEU_KHAM;
    private boolean isAscNgay = true;
    private boolean isAscTenBN = true;
    private boolean isAscTrangThai = true;
    private boolean isAscTongTien = true;
    private boolean isAscBillId = true;
    private boolean isAscBillDate = true;
    private boolean isAscBillStatus = true;
    private boolean isAscBillTotal = true;

    private EditText etSearchInput;
    private TextView tvNgayBD, tvNgayKT, tvTongSoHoaDon, tvTongDoanhThu, tvEmptyState;
    private TextView tvSummaryCountLabel, tvSummaryRevenueLabel;
    private TextView thNgay, thBenhNhan, thTrangThai, thTongTien;
    private MaterialButton btnTabPhieuKham, btnTabBill;
    private RecyclerView rvDanhSach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_reports_fragment_admin);

        apiService = SupabaseClientProvider.getClient(this).create(ReportApiService.class);
        bindViews();
        setupRecyclerView();
        setupListeners();
        switchReportTab(TAB_PHIEU_KHAM);
    }

    private void bindViews() {
        ImageButton btnBackCreate = findViewById(R.id.btnBackCreate);
        etSearchInput = findViewById(R.id.et_ten_benh_nhan);
        tvNgayBD = findViewById(R.id.tv_tu_ngay);
        tvNgayKT = findViewById(R.id.tv_den_ngay);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        rvDanhSach = findViewById(R.id.rv_danh_sach_doanh_thu);
        tvTongSoHoaDon = findViewById(R.id.tv_tong_so_hoa_don);
        tvTongDoanhThu = findViewById(R.id.tv_tong_doanh_thu);
        tvSummaryCountLabel = findViewById(R.id.tv_summary_count_label);
        tvSummaryRevenueLabel = findViewById(R.id.tv_summary_revenue_label);
        btnTabPhieuKham = findViewById(R.id.btn_tab_phieu_kham);
        btnTabBill = findViewById(R.id.btn_tab_bill);
        thNgay = findViewById(R.id.th_ngay);
        thBenhNhan = findViewById(R.id.th_benh_nhan);
        thTrangThai = findViewById(R.id.th_trang_thai);
        thTongTien = findViewById(R.id.th_tong_tien);

        if (btnBackCreate != null) {
            btnBackCreate.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        if (rvDanhSach == null) return;
        rvDanhSach.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportsAdapter(danhSachKetQua);
        billAdapter = new BillReportsAdapter(danhSachBillKetQua);
        rvDanhSach.setAdapter(adapter);
    }

    private void setupListeners() {
        if (tvNgayBD != null) tvNgayBD.setOnClickListener(v -> hienThiLich(tvNgayBD));
        if (tvNgayKT != null) tvNgayKT.setOnClickListener(v -> hienThiLich(tvNgayKT));
        if (btnTabPhieuKham != null) btnTabPhieuKham.setOnClickListener(v -> switchReportTab(TAB_PHIEU_KHAM));
        if (btnTabBill != null) btnTabBill.setOnClickListener(v -> switchReportTab(TAB_BILL));

        if (thNgay != null) thNgay.setOnClickListener(v -> sapXepTheoCot("NGAY"));
        if (thBenhNhan != null) thBenhNhan.setOnClickListener(v -> sapXepTheoCot("TEN_BN"));
        if (thTrangThai != null) thTrangThai.setOnClickListener(v -> sapXepTheoCot("TRANG_THAI"));
        if (thTongTien != null) thTongTien.setOnClickListener(v -> sapXepTheoCot("TONG_TIEN"));

        Button btnTimKiem = findViewById(R.id.btn_tim_kiem);
        if (btnTimKiem != null) {
            btnTimKiem.setOnClickListener(v -> {
                String input = etSearchInput != null ? etSearchInput.getText().toString().trim() : "";
                String tuNgay = tvNgayBD != null ? tvNgayBD.getText().toString().trim() : "";
                String denNgay = tvNgayKT != null ? tvNgayKT.getText().toString().trim() : "";
                if (currentTab == TAB_BILL) {
                    fetchBillDataAndFilter(input, tuNgay, denNgay);
                } else {
                    fetchDataAndFilter(input, tuNgay, denNgay);
                }
            });
        }
    }

    private void switchReportTab(int tab) {
        currentTab = tab;
        boolean isBillTab = tab == TAB_BILL;

        bindTabButton(btnTabPhieuKham, !isBillTab);
        bindTabButton(btnTabBill, isBillTab);

        if (rvDanhSach != null) {
            rvDanhSach.setAdapter(isBillTab ? billAdapter : adapter);
        }
        if (etSearchInput != null) {
            etSearchInput.setText("");
            etSearchInput.setHint(isBillTab ? "Mã hóa đơn" : "Mã BN/Tên BN/CCCD");
        }
        if (thNgay != null) thNgay.setText(isBillTab ? "Mã hóa đơn" : "Ngày");
        if (thBenhNhan != null) thBenhNhan.setText(isBillTab ? "Ngày tạo" : "Bệnh nhân");
        if (thTrangThai != null) thTrangThai.setText("Trạng thái");
        if (thTongTien != null) thTongTien.setText("Tổng tiền");
        if (tvSummaryCountLabel != null) tvSummaryCountLabel.setText(isBillTab ? "Số hóa đơn" : "Số phiếu khám");
        if (tvSummaryRevenueLabel != null) tvSummaryRevenueLabel.setText(isBillTab ? "Tổng thanh toán" : "Tổng thu");

        if (isBillTab) {
            capNhatTongKetBill(danhSachBillKetQua);
        } else {
            capNhatTongKet(danhSachKetQua, tvTongSoHoaDon, tvTongDoanhThu);
        }
        updateUIState();
    }

    private void bindTabButton(MaterialButton button, boolean selected) {
        if (button == null) return;
        button.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#0D3F6E"));
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(selected ? "#0D3F6E" : "#FFFFFF")));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#0D3F6E")));
        button.setStrokeWidth(dp(selected ? 0 : 1));
    }

    private void fetchDataAndFilter(String input, String tuNgay, String denNgay) {
        ProgressDialog pd = createProgressDialog();
        apiService.getDanhSachDonKham().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> response) {
                pd.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    danhSachGoc.clear();
                    for (ReportItem item : response.body()) {
                        danhSachGoc.add(new DonKham(
                                item.getId(),
                                item.getPatientName(),
                                item.getPatientId(),
                                item.getPatientCccd(),
                                formatDateString(item.getNgay_kham()),
                                item.getTrang_thai(),
                                item.getPhi_kham()
                        ));
                    }
                    thucHienLocDuLieu(input, tuNgay, denNgay);
                } else {
                    Toast.makeText(ReportsActivity_Admin.this, "Không thể lấy dữ liệu mới nhất!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                pd.dismiss();
                Log.e("REPORTS_API", "Error: " + t.getMessage());
                Toast.makeText(ReportsActivity_Admin.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBillDataAndFilter(String input, String tuNgay, String denNgay) {
        ProgressDialog pd = createProgressDialog();
        apiService.getDanhSachBill().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> response) {
                pd.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    danhSachBillGoc.clear();
                    for (ReportItem item : response.body()) {
                        ReportItem.BillInfo bill = item.getBillInfo();
                        if (bill == null || bill.getId() == null) continue;

                        String displayDate = formatDateString(bill.getCreated_at());
                        if (displayDate == null || displayDate.isEmpty()) {
                            displayDate = formatDateString(item.getNgay_kham());
                        }
                        long total = bill.getTong_thanh_toan() != null ? Math.round(bill.getTong_thanh_toan()) : 0L;
                        danhSachBillGoc.add(new BillThongKe(
                                String.valueOf(bill.getId()),
                                displayDate,
                                bill.getTrang_thai_thanh_toan(),
                                bill.getPhuong_thuc_thanh_toan(),
                                total
                        ));
                    }
                    thucHienLocBill(input, tuNgay, denNgay);
                } else {
                    Toast.makeText(ReportsActivity_Admin.this, "Không thể lấy dữ liệu từ bill", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                pd.dismiss();
                Log.e("REPORTS_BILL_API", "Error: " + t.getMessage());
                Toast.makeText(ReportsActivity_Admin.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang truy xất dữ liệu từ máy chủ...");
        pd.setCancelable(false);
        pd.show();
        return pd;
    }

    private void thucHienLocDuLieu(String input, String tuNgay, String denNgay) {
        DateRange range = parseDateRange(tuNgay, denNgay);
        if (!range.valid) return;

        danhSachKetQua.clear();
        String queryLower = input.toLowerCase(Locale.getDefault());

        for (DonKham dk : danhSachGoc) {
            boolean matchInput = input.isEmpty()
                    || input.equalsIgnoreCase(dk.getPatientId())
                    || input.equals(dk.getPatientCccd())
                    || dk.getTenBN().toLowerCase(Locale.getDefault()).contains(queryLower);

            if (matchInput && isDateInRange(dk.getNgayKham(), range)) {
                danhSachKetQua.add(dk);
            }
        }

        adapter.notifyDataSetChanged();
        capNhatTongKet(danhSachKetQua, tvTongSoHoaDon, tvTongDoanhThu);
        updateUIState();
    }

    private void thucHienLocBill(String input, String tuNgay, String denNgay) {
        DateRange range = parseDateRange(tuNgay, denNgay);
        if (!range.valid) return;

        danhSachBillKetQua.clear();
        for (BillThongKe bill : danhSachBillGoc) {
            boolean matchInput = input.isEmpty() || bill.getMaBill().equalsIgnoreCase(input);
            if (matchInput && isDateInRange(bill.getNgayTao(), range)) {
                danhSachBillKetQua.add(bill);
            }
        }

        billAdapter.notifyDataSetChanged();
        capNhatTongKetBill(danhSachBillKetQua);
        updateUIState();
    }

    private DateRange parseDateRange(String tuNgay, String denNgay) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        DateRange range = new DateRange();
        try {
            if (isSelectedDate(tuNgay)) range.from = sdf.parse(tuNgay);
            if (isSelectedDate(denNgay)) range.to = sdf.parse(denNgay);
            if (range.from != null && range.to != null && range.from.after(range.to)) {
                Toast.makeText(this, "Ngày bắt đầu không được sau ngày kết thúc!", Toast.LENGTH_LONG).show();
                range.valid = false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Định dạng ngày chưa chuẩn!", Toast.LENGTH_SHORT).show();
            range.valid = false;
        }
        return range;
    }

    private boolean isSelectedDate(String value) {
        return value != null && value.contains("/");
    }

    private boolean isDateInRange(String displayDate, DateRange range) {
        Date dateInList = null;
        try {
            dateInList = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(displayDate);
        } catch (Exception ignored) {}

        boolean matchFrom = range.from == null || (dateInList != null && !dateInList.before(range.from));
        boolean matchTo = range.to == null || (dateInList != null && !dateInList.after(range.to));
        return matchFrom && matchTo;
    }

    private void updateUIState() {
        boolean empty = currentTab == TAB_BILL ? danhSachBillKetQua.isEmpty() : danhSachKetQua.isEmpty();
        if (rvDanhSach != null) rvDanhSach.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
            tvEmptyState.setText(currentTab == TAB_BILL ? "Chưa có dữ liệu thống kê" : "Chưa có dữ liệu thống kê");
        }
    }

    private void sapXepTheoCot(String loaiCot) {
        if (currentTab == TAB_BILL) {
            sapXepBillTheoCot(loaiCot);
            return;
        }
        if (danhSachKetQua.isEmpty()) return;
        Collections.sort(danhSachKetQua, (dk1, dk2) -> {
            switch (loaiCot) {
                case "NGAY":
                    return compareDisplayDate(dk1.getNgayKham(), dk2.getNgayKham(), isAscNgay);
                case "TEN_BN":
                    return isAscTenBN ? dk1.getTenBN().compareToIgnoreCase(dk2.getTenBN()) : dk2.getTenBN().compareToIgnoreCase(dk1.getTenBN());
                case "TRANG_THAI":
                    return isAscTrangThai ? dk1.getTrangThai().compareToIgnoreCase(dk2.getTrangThai()) : dk2.getTrangThai().compareToIgnoreCase(dk1.getTrangThai());
                case "TONG_TIEN":
                    return isAscTongTien ? Long.compare(dk1.getTongTien(), dk2.getTongTien()) : Long.compare(dk2.getTongTien(), dk1.getTongTien());
                default:
                    return 0;
            }
        });

        if (loaiCot.equals("NGAY")) isAscNgay = !isAscNgay;
        if (loaiCot.equals("TEN_BN")) isAscTenBN = !isAscTenBN;
        if (loaiCot.equals("TRANG_THAI")) isAscTrangThai = !isAscTrangThai;
        if (loaiCot.equals("TONG_TIEN")) isAscTongTien = !isAscTongTien;
        adapter.notifyDataSetChanged();
    }

    private void sapXepBillTheoCot(String loaiCot) {
        if (danhSachBillKetQua.isEmpty()) return;
        Collections.sort(danhSachBillKetQua, (b1, b2) -> {
            switch (loaiCot) {
                case "NGAY":
                    return isAscBillId ? compareLongString(b1.getMaBill(), b2.getMaBill()) : compareLongString(b2.getMaBill(), b1.getMaBill());
                case "TEN_BN":
                    return compareDisplayDate(b1.getNgayTao(), b2.getNgayTao(), isAscBillDate);
                case "TRANG_THAI":
                    return isAscBillStatus ? b1.getTrangThai().compareToIgnoreCase(b2.getTrangThai()) : b2.getTrangThai().compareToIgnoreCase(b1.getTrangThai());
                case "TONG_TIEN":
                    return isAscBillTotal ? Long.compare(b1.getTongTien(), b2.getTongTien()) : Long.compare(b2.getTongTien(), b1.getTongTien());
                default:
                    return 0;
            }
        });

        if (loaiCot.equals("NGAY")) isAscBillId = !isAscBillId;
        if (loaiCot.equals("TEN_BN")) isAscBillDate = !isAscBillDate;
        if (loaiCot.equals("TRANG_THAI")) isAscBillStatus = !isAscBillStatus;
        if (loaiCot.equals("TONG_TIEN")) isAscBillTotal = !isAscBillTotal;
        billAdapter.notifyDataSetChanged();
    }

    private int compareLongString(String left, String right) {
        try {
            return Long.compare(Long.parseLong(left), Long.parseLong(right));
        } catch (NumberFormatException e) {
            return left.compareToIgnoreCase(right);
        }
    }

    private int compareDisplayDate(String left, String right, boolean ascending) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date n1 = sdf.parse(left);
            Date n2 = sdf.parse(right);
            return ascending ? n1.compareTo(n2) : n2.compareTo(n1);
        } catch (Exception e) {
            return 0;
        }
    }

    private void capNhatTongKet(List<DonKham> danhSach, TextView tvTongSoHoaDon, TextView tvTongDoanhThu) {
        int soPhieuKham = danhSach.size();
        long tongDoanhThu = 0;
        for (DonKham dk : danhSach) {
            if (!isCancelledStatus(dk.getTrangThai())) {
                tongDoanhThu += dk.getTongTien();
            }
        }
        if (tvTongSoHoaDon != null) tvTongSoHoaDon.setText(String.valueOf(soPhieuKham));
        if (tvTongDoanhThu != null) tvTongDoanhThu.setText(formatCurrency(tongDoanhThu));
    }

    private void capNhatTongKetBill(List<BillThongKe> danhSach) {
        long tongBill = 0;
        for (BillThongKe bill : danhSach) {
            tongBill += bill.getTongTien();
        }
        if (tvTongSoHoaDon != null) tvTongSoHoaDon.setText(String.valueOf(danhSach.size()));
        if (tvTongDoanhThu != null) tvTongDoanhThu.setText(formatCurrency(tongBill));
    }

    private String formatCurrency(long amount) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        return fmt.format(amount) + " \u0111";
    }

    private boolean isCancelledStatus(String status) {
        return "\u0110\u00e3 h\u1ee7y".equalsIgnoreCase(status)
                || "Da huy".equalsIgnoreCase(status);
    }

    private String formatDateString(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                Date parsed = new SimpleDateFormat(pattern, Locale.getDefault()).parse(raw);
                if (parsed != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed);
                }
            } catch (Exception ignored) {}
        }
        if (raw.length() >= 10 && raw.charAt(4) == '-' && raw.charAt(7) == '-') {
            String[] parts = raw.substring(0, 10).split("-");
            if (parts.length == 3) return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return raw;
    }

    public static class DonKham {
        private final String MaDonKham, TenBN, PatientId, PatientCccd, NgayKham, TrangThai;
        private final long TongTien;

        public DonKham(String maHoaDon, String tenBenhNhan, String patientId, String patientCccd, String ngayKham, String trangThai, long tongTien) {
            this.MaDonKham = maHoaDon;
            this.TenBN = tenBenhNhan;
            this.PatientId = patientId;
            this.PatientCccd = patientCccd;
            this.NgayKham = ngayKham;
            this.TrangThai = trangThai;
            this.TongTien = tongTien;
        }

        public String getMaDonKham() { return MaDonKham; }
        public String getTenBN() { return TenBN; }
        public String getPatientId() { return PatientId; }
        public String getPatientCccd() { return PatientCccd; }
        public String getNgayKham() { return NgayKham; }
        public String getTrangThai() { return TrangThai; }
        public long getTongTien() { return TongTien; }
    }

    public static class BillThongKe {
        private final String MaBill, NgayTao, TrangThai, PhuongThucThanhToan;
        private final long TongTien;

        public BillThongKe(String maBill, String ngayTao, String trangThai, String phuongThucThanhToan, long tongTien) {
            this.MaBill = maBill;
            this.NgayTao = ngayTao != null ? ngayTao : "";
            this.TrangThai = trangThai != null ? trangThai : "";
            this.PhuongThucThanhToan = phuongThucThanhToan != null ? phuongThucThanhToan : "";
            this.TongTien = tongTien;
        }

        public String getMaBill() { return MaBill; }
        public String getNgayTao() { return NgayTao; }
        public String getTrangThai() { return TrangThai; }
        public String getPhuongThucThanhToan() { return PhuongThucThanhToan; }
        public long getTongTien() { return TongTien; }
    }

    private static class DateRange {
        private Date from;
        private Date to;
        private boolean valid = true;
    }

    private void hienThiLich(TextView tv) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
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
        datePicker.init(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH), null);
        layout.addView(datePicker);

        TextView btnApDung = taoNutLich("Ap dung");
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

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
