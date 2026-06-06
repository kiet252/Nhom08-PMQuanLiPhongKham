package com.example.nhom08_quanlyphongkham.admin_reports;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dashboard_fragment.RevenueChartView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity_Admin extends BaseActivity {

    private static final int TIEU_CHI_DOANH_THU = 0;
    private static final int TIEU_CHI_BENH_NHAN_MOI = 1;
    private static final int TIEU_CHI_PHIEU_KHAM = 2;

    private final List<ThongKeThang> danhSachThongKe = new ArrayList<>();
    private final String[] tenTieuChi = {"Doanh thu", "Bệnh nhân mới", "Phiếu khám bệnh"};

    private MonthlyReportsAdapter adapter;
    private ReportApiService apiService;
    private RevenueChartView chartMonthlyReport;

    private TextView tvSelectedCriterion, tvFromMonth, tvFromYear, tvToMonth, tvToYear;
    private TextView tvSummaryLabel, tvSummaryValue, tvChartTitle, tvTableTitle, tvTableValueHeader, tvEmptyState;
    private RecyclerView rvDanhSach;

    private int selectedCriterion = TIEU_CHI_DOANH_THU;
    private int fromMonth = 1;
    private int fromYear;
    private int toMonth;
    private int toYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_reports_fragment_admin);

        Calendar now = Calendar.getInstance();
        fromYear = now.get(Calendar.YEAR);
        toYear = now.get(Calendar.YEAR);
        toMonth = now.get(Calendar.MONTH) + 1;

        apiService = SupabaseClientProvider.getClient(this).create(ReportApiService.class);
        bindViews();
        setupRecyclerView();
        setupListeners();
        updateFilterText();
        fetchReports();
    }

    private void bindViews() {
        ImageButton btnBackCreate = findViewById(R.id.btnBackCreate);
        tvSelectedCriterion = findViewById(R.id.tv_selected_criterion);
        tvFromMonth = findViewById(R.id.tv_from_month);
        tvFromYear = findViewById(R.id.tv_from_year);
        tvToMonth = findViewById(R.id.tv_to_month);
        tvToYear = findViewById(R.id.tv_to_year);
        tvSummaryLabel = findViewById(R.id.tv_summary_label);
        tvSummaryValue = findViewById(R.id.tv_summary_value);
        tvChartTitle = findViewById(R.id.tv_chart_title);
        tvTableTitle = findViewById(R.id.tv_table_title);
        tvTableValueHeader = findViewById(R.id.tv_table_value_header);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        chartMonthlyReport = findViewById(R.id.chart_monthly_report);
        rvDanhSach = findViewById(R.id.rv_danh_sach_doanh_thu);

        if (btnBackCreate != null) {
            btnBackCreate.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        if (rvDanhSach == null) return;
        rvDanhSach.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MonthlyReportsAdapter(danhSachThongKe);
        adapter.setCriterion(selectedCriterion);
        rvDanhSach.setAdapter(adapter);
    }

    private void setupListeners() {
        if (tvSelectedCriterion != null) tvSelectedCriterion.setOnClickListener(v -> showCriterionDialog());
        if (tvFromMonth != null) tvFromMonth.setOnClickListener(v -> showMonthPicker(true));
        if (tvFromYear != null) tvFromYear.setOnClickListener(v -> showYearPicker(true));
        if (tvToMonth != null) tvToMonth.setOnClickListener(v -> showMonthPicker(false));
        if (tvToYear != null) tvToYear.setOnClickListener(v -> showYearPicker(false));
    }

    private void showCriterionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(18), dp(18), dp(18), dp(14));
        container.setBackground(taoNenBoGoc("#FFFFFF", 22));

        TextView title = new TextView(this);
        title.setText("Tiêu chí thống kê");
        title.setTextColor(Color.parseColor("#0D3F6E"));
        title.setTextSize(19);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(title);

        container.addView(createCriterionRow(dialog, TIEU_CHI_DOANH_THU));
        container.addView(createCriterionRow(dialog, TIEU_CHI_BENH_NHAN_MOI));
        container.addView(createCriterionRow(dialog, TIEU_CHI_PHIEU_KHAM));

        dialog.setView(container);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private View createCriterionRow(AlertDialog dialog, int criterion) {
        boolean selected = selectedCriterion == criterion;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(taoNenBoGoc(selected ? "#EAF6FF" : "#FFFFFF", 14));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
        );
        rowParams.topMargin = dp(10);
        row.setLayoutParams(rowParams);

        ImageView icon = new ImageView(this);
        icon.setImageResource(getCriterionIcon(criterion));
        icon.setColorFilter(Color.parseColor(selected ? "#0D5FA8" : "#64748B"));
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(taoNenBoGoc(selected ? "#D8EDFF" : "#F1F5F9", 12));
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView label = new TextView(this);
        label.setText(tenTieuChi[criterion]);
        label.setTextColor(Color.parseColor(selected ? "#0D3F6E" : "#1E293B"));
        label.setTextSize(16);
        label.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        labelParams.leftMargin = dp(14);
        row.addView(label, labelParams);

        TextView check = new TextView(this);
        check.setText(selected ? "\u2713" : "");
        check.setGravity(Gravity.CENTER);
        check.setTextColor(Color.parseColor("#0D5FA8"));
        check.setTextSize(22);
        check.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(check, new LinearLayout.LayoutParams(dp(28), dp(28)));

        row.setOnClickListener(v -> {
            selectedCriterion = criterion;
            dialog.dismiss();
            updateFilterText();
            updateSelectedCriterionView();
        });

        return row;
    }

    private int getCriterionIcon(int criterion) {
        if (criterion == TIEU_CHI_BENH_NHAN_MOI) return R.drawable.ic_manage_patient;
        if (criterion == TIEU_CHI_PHIEU_KHAM) return R.drawable.ic_manage_exform;
        return R.drawable.dashboard_icon_report;
    }

    private void showMonthPicker(boolean isFrom) {
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) months[i] = "Tháng " + (i + 1);
        int current = isFrom ? fromMonth : toMonth;
        new AlertDialog.Builder(this)
                .setTitle(isFrom ? "Chọn tháng bắt đầu" : "Chọn tháng kết thúc")
                .setSingleChoiceItems(months, current - 1, (dialog, which) -> {
                    if (isFrom) fromMonth = which + 1;
                    else toMonth = which + 1;
                    dialog.dismiss();
                    applyRangeChange();
                })
                .show();
    }

    private void showYearPicker(boolean isFrom) {
        NumberPicker picker = new NumberPicker(new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog_Alert));
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        picker.setMinValue(currentYear - 5);
        picker.setMaxValue(currentYear + 1);
        picker.setValue(isFrom ? fromYear : toYear);
        picker.setWrapSelectorWheel(false);

        new AlertDialog.Builder(this)
                .setTitle(isFrom ? "Chọn năm bắt đầu" : "Chọn năm kết thúc")
                .setView(picker)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    if (isFrom) fromYear = picker.getValue();
                    else toYear = picker.getValue();
                    applyRangeChange();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void applyRangeChange() {
        if (getFromKey() > getToKey()) {
            Toast.makeText(this, "Thời gian bắt đầu không được sau thời gian kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }
        updateFilterText();
        fetchReports();
    }

    private void updateFilterText() {
        if (tvSelectedCriterion != null) tvSelectedCriterion.setText(tenTieuChi[selectedCriterion]);
        if (tvFromMonth != null) tvFromMonth.setText("Tháng " + fromMonth);
        if (tvFromYear != null) tvFromYear.setText("Năm " + fromYear);
        if (tvToMonth != null) tvToMonth.setText("Tháng " + toMonth);
        if (tvToYear != null) tvToYear.setText("Năm " + toYear);
    }

    private void fetchReports() {
        ProgressDialog pd = createProgressDialog();
        apiService.getThongKePatient().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> patientResponse) {
                if (!patientResponse.isSuccessful() || patientResponse.body() == null) {
                    pd.dismiss();
                    Toast.makeText(ReportsActivity_Admin.this, "Không thể lấy dữ liệu bệnh nhân", Toast.LENGTH_SHORT).show();
                    return;
                }
                fetchBills(patientResponse.body(), pd);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                pd.dismiss();
                Log.e("REPORT_PATIENT", "Error: " + t.getMessage());
                Toast.makeText(ReportsActivity_Admin.this, "Lỗi kết nối dữ liệu bệnh nhân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBills(List<ReportItem> patients, ProgressDialog pd) {
        apiService.getThongKeBill().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> billResponse) {
                if (!billResponse.isSuccessful() || billResponse.body() == null) {
                    pd.dismiss();
                    Toast.makeText(ReportsActivity_Admin.this, "Không thể lấy dữ liệu hóa đơn", Toast.LENGTH_SHORT).show();
                    return;
                }
                fetchExaminationForms(patients, billResponse.body(), pd);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                pd.dismiss();
                Log.e("REPORT_BILL", "Error: " + t.getMessage());
                Toast.makeText(ReportsActivity_Admin.this, "Lỗi kết nối dữ liệu hóa đơn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchExaminationForms(List<ReportItem> patients, List<ReportItem> bills, ProgressDialog pd) {
        apiService.getThongKePhieuKham().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> formResponse) {
                pd.dismiss();
                if (!formResponse.isSuccessful() || formResponse.body() == null) {
                    Toast.makeText(ReportsActivity_Admin.this, "Không thể lấy dữ liệu phiếu khám", Toast.LENGTH_SHORT).show();
                    return;
                }
                buildMonthlyReport(patients, bills, formResponse.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                pd.dismiss();
                Log.e("REPORT_FORM", "Error: " + t.getMessage());
                Toast.makeText(ReportsActivity_Admin.this, "Lỗi kết nối dữ liệu phiếu khám", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildMonthlyReport(List<ReportItem> patients, List<ReportItem> bills, List<ReportItem> forms) {
        Map<Integer, ThongKeThang> monthlyMap = createEmptyMonthMap();

        for (ReportItem patient : patients) {
            Integer key = getMonthKey(parseServerDate(patient.getCreated_at()));
            if (!isKeyInRange(key)) continue;
            monthlyMap.get(key).soBenhNhanMoi++;
        }

        for (ReportItem bill : bills) {
            Integer key = getMonthKey(parseServerDate(bill.getCreated_at()));
            if (!isKeyInRange(key)) continue;
            monthlyMap.get(key).doanhThu += Math.round(bill.getBillTongThanhToan());
        }

        for (ReportItem form : forms) {
            Integer key = getMonthKey(parseServerDate(form.getNgay_kham()));
            if (!isKeyInRange(key)) continue;
            if (!isCancelledStatus(form.getTrang_thai())) {
                monthlyMap.get(key).soPhieuKham++;
            }
        }

        danhSachThongKe.clear();
        danhSachThongKe.addAll(monthlyMap.values());

        if (adapter != null) {
            adapter.setCriterion(selectedCriterion);
            adapter.notifyDataSetChanged();
        }
        updateSelectedCriterionView();
        updateUIState();
    }

    private Map<Integer, ThongKeThang> createEmptyMonthMap() {
        Map<Integer, ThongKeThang> monthlyMap = new LinkedHashMap<>();
        Calendar cursor = Calendar.getInstance();
        cursor.clear();
        cursor.set(Calendar.YEAR, fromYear);
        cursor.set(Calendar.MONTH, fromMonth - 1);
        cursor.set(Calendar.DAY_OF_MONTH, 1);

        while (true) {
            int month = cursor.get(Calendar.MONTH) + 1;
            int year = cursor.get(Calendar.YEAR);
            int key = year * 100 + month;
            monthlyMap.put(key, new ThongKeThang(key, month, year));
            if (key == getToKey()) break;
            cursor.add(Calendar.MONTH, 1);
        }
        return monthlyMap;
    }

    private void updateSelectedCriterionView() {
        if (adapter != null) {
            adapter.setCriterion(selectedCriterion);
            adapter.notifyDataSetChanged();
        }

        long total = 0;
        String[] labels = new String[danhSachThongKe.size()];
        float[] values = new float[danhSachThongKe.size()];

        for (int i = 0; i < danhSachThongKe.size(); i++) {
            ThongKeThang row = danhSachThongKe.get(i);
            labels[i] = "T" + row.getThangSo() + "/" + String.valueOf(row.getNam()).substring(2);
            if (selectedCriterion == TIEU_CHI_DOANH_THU) {
                total += row.getDoanhThu();
                values[i] = row.getDoanhThu() / 1_000_000f;
            } else if (selectedCriterion == TIEU_CHI_BENH_NHAN_MOI) {
                total += row.getSoBenhNhanMoi();
                values[i] = row.getSoBenhNhanMoi();
            } else {
                total += row.getSoPhieuKham();
                values[i] = row.getSoPhieuKham();
            }
        }

        String title = tenTieuChi[selectedCriterion];
        int monthCount = danhSachThongKe.size();
        if (tvSummaryLabel != null) tvSummaryLabel.setText(getSummaryLabel(title, monthCount));
        if (tvSummaryValue != null) tvSummaryValue.setText(formatSelectedValue(total));
        if (tvChartTitle != null) tvChartTitle.setText("Biểu đồ " + title.toLowerCase(Locale.getDefault()));
        if (tvTableTitle != null) tvTableTitle.setText(title + " theo tháng");
        if (tvTableValueHeader != null) tvTableValueHeader.setText(title);
        if (chartMonthlyReport != null) chartMonthlyReport.setData(labels, values);
    }

    private String getSummaryLabel(String title, int monthCount) {
        if (selectedCriterion == TIEU_CHI_DOANH_THU) {
            return "Tổng doanh thu (" + monthCount + " tháng)";
        }
        return "Tổng " + title.toLowerCase(Locale.getDefault()) + " (" + monthCount + " tháng)";
    }

    private String formatSelectedValue(long value) {
        if (selectedCriterion == TIEU_CHI_DOANH_THU) return formatCurrency(value);
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value);
    }

    private void updateUIState() {
        boolean empty = danhSachThongKe.isEmpty();
        if (rvDanhSach != null) rvDanhSach.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
            tvEmptyState.setText("Chưa có dữ liệu thống kê");
        }
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang truy xuất dữ liệu thống kê...");
        pd.setCancelable(false);
        pd.show();
        return pd;
    }

    private Date parseServerDate(String raw) {
        if (raw == null || raw.length() < 10) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(raw.substring(0, 10));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer getMonthKey(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH) + 1;
    }

    private boolean isKeyInRange(Integer key) {
        return key != null && key >= getFromKey() && key <= getToKey();
    }

    private int getFromKey() {
        return fromYear * 100 + fromMonth;
    }

    private int getToKey() {
        return toYear * 100 + toMonth;
    }

    private String formatCurrency(long amount) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        return fmt.format(amount) + " đ";
    }

    private boolean isCancelledStatus(String status) {
        return "Đã hủy".equalsIgnoreCase(status) || "Da huy".equalsIgnoreCase(status);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    public static class ThongKeThang {
        private final int monthKey;
        private final int thangSo;
        private final int nam;
        private long doanhThu;
        private int soBenhNhanMoi;
        private int soPhieuKham;

        public ThongKeThang(int monthKey, int thangSo, int nam) {
            this.monthKey = monthKey;
            this.thangSo = thangSo;
            this.nam = nam;
        }

        public int getMonthKey() { return monthKey; }
        public int getThangSo() { return thangSo; }
        public int getNam() { return nam; }
        public String getThang() { return "Tháng " + thangSo; }
        public long getDoanhThu() { return doanhThu; }
        public int getSoBenhNhanMoi() { return soBenhNhanMoi; }
        public int getSoPhieuKham() { return soPhieuKham; }
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

    private GradientDrawable taoNenBoGoc(String color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(Math.round(radius * getResources().getDisplayMetrics().density));
        return drawable;
    }
}

