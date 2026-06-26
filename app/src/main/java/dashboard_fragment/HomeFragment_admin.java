package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.admin_reports.ReportApiService;
import com.example.nhom08_quanlyphongkham.admin_reports.ReportItem;
import com.example.nhom08_quanlyphongkham.admin_manage_staff.admin_manage_staff;
import com.example.nhom08_quanlyphongkham.admin_reports.ReportsActivity_Admin;

import dashboard_fragment.account_chatbot.ChatbotBottomSheetFragment;
import dashboard_fragment.admin_manage_device.AdminManageDeviceActivity;
import dashboard_fragment.admin_timekeeping_schedule.AdminTimekeepingScheduleActivity;
import dashboard_fragment.admin_timekeeping_fix_request.AdminTimekeepingFixRequestActivity;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import java.util.ArrayList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import coil.Coil;
import coil.request.ImageRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment_admin extends Fragment {

    private LinearLayout mngStaff;
    private TextView txtName;
    private ViewPager2 viewPagerCharts;
    private LinearLayout layoutIndicators;
    private AdminChartsAdapter adapterCharts;
    private ReportApiService reportApiService;

    public HomeFragment_admin() {
        // Required empty public constructor
    }

    public static HomeFragment_admin newInstance() {
        return new HomeFragment_admin();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Nạp đúng giao diện của màn hình Home
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        // I. Nút XEM BÁO CÁO VÀ THỐNG KÊ
        // 1. Tìm nút bấm XEM BÁO CÁO trên màn hình Home
        View btnXemBaoCao = view.findViewById(R.id.btn_xem_bao_cao);
        // 2. Xử lý khi nhấn nút
        btnXemBaoCao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đang ở Home -> Chuyển sang ReportsActivity_Admin
                Intent intent = new Intent(getActivity(), ReportsActivity_Admin.class);
                startActivity(intent);
            }
        });
        // II. Nút CÀI ĐẶT THÔNG BÁO CHUNG
        // 1. Tìm nút bấm CÀI ĐẶT THÔNG BÁO CHUNG trên màn hình Home
        View btnCaiDatThongBao = view.findViewById(R.id.btn_cai_dat_thong_bao);
        // 2. Xử lí khi nhấn nút
        btnCaiDatThongBao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NotificationSettingsActivity_Admin.class);
                startActivity(intent);
            }
        });
        return view;
    }
    public void SetAvatar(View view, UserProfile userprofile)
    {
        if (userprofile == null) return;
        ImageView avatar = view.findViewById(R.id.home_avatar_admin);
        String avatarUrl = userprofile.getAnh_dai_dien();

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + avatarUrl;
            }

            ImageRequest request = new ImageRequest.Builder(requireContext())
                    .data(avatarUrl)
                    .target(avatar)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build();

            Coil.imageLoader(requireContext()).enqueue(request);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtName = view.findViewById(R.id.admin_home_name);
        mngStaff = view.findViewById(R.id.btn_mngStaff);
        viewPagerCharts = view.findViewById(R.id.view_pager_admin_charts);
        layoutIndicators = view.findViewById(R.id.layout_chart_indicators);
        reportApiService = SupabaseClientProvider.getClient(requireContext()).create(ReportApiService.class);

        View chatbotView = view.findViewById(R.id.chatbot_floating_button);
        if (chatbotView != null) {
            chatbotView.setOnClickListener(v ->
                    ChatbotBottomSheetFragment.newInstance(UserRole.ADMIN.name())
                            .show(getParentFragmentManager(), "chatbot")
            );
        }

        // Lấy tên người dùng từ SharedPrefManager
        SharedPrefManager prefManager = SharedPrefManager.getInstance(requireContext());
        UserProfile profile = prefManager.getProfile();
        SetAvatar(view, profile);
        if (profile != null && profile.getHo_ten() != null) {
            txtName.setText(profile.getHo_ten());
        } else {

        }

        mngStaff.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), admin_manage_staff.class);
            startActivity(intent);
        });

        View btnMngTimekeepingSchedule = view.findViewById(R.id.btn_mngTimekeepingSchedule);
        if (btnMngTimekeepingSchedule != null) {
            btnMngTimekeepingSchedule.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AdminTimekeepingScheduleActivity.class);
                startActivity(intent);
            });
        }

        View btnMngTimekeepingFixRequest = view.findViewById(R.id.btn_mngTimekeepingFixRequest);
        if (btnMngTimekeepingFixRequest != null) {
            btnMngTimekeepingFixRequest.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AdminTimekeepingFixRequestActivity.class);
                startActivity(intent);
            });
        }

        View btnMngDeviceApproval = view.findViewById(R.id.btn_mngDeviceApproval);
        if (btnMngDeviceApproval != null) {
            btnMngDeviceApproval.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AdminManageDeviceActivity.class);
                startActivity(intent);
            });
        }

        View btnMngMedicineClinical = view.findViewById(R.id.btn_mngMedicineClinical);
        if (btnMngMedicineClinical != null) {
            btnMngMedicineClinical.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), dashboard_fragment.admin_manage_medicine_clinical.AdminManageMedicineClinicalActivity.class);
                startActivity(intent);
            });
        }

        taiDuLieuBaoCao();
    }

    private void taiDuLieuBaoCao() {
        khoiTaoBieuDoMacDinh();
        reportApiService.getDanhSachDonKham().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> responseDonKham) {
                if (!isAdded() || !responseDonKham.isSuccessful() || responseDonKham.body() == null) {
                    return;
                }

                reportApiService.getThongKePatient().enqueue(new Callback<List<ReportItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ReportItem>> call2, @NonNull Response<List<ReportItem>> responsePatient) {
                        if (!isAdded() || !responsePatient.isSuccessful() || responsePatient.body() == null) {
                            return;
                        }

                        reportApiService.getThongKeBill().enqueue(new Callback<List<ReportItem>>() {
                            @Override
                            public void onResponse(@NonNull Call<List<ReportItem>> call3, @NonNull Response<List<ReportItem>> responseBill) {
                                if (!isAdded() || !responseBill.isSuccessful() || responseBill.body() == null) {
                                    return;
                                }
                                xuLyVaHienThiBaoCao(responseDonKham.body(), responsePatient.body(), responseBill.body());
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<ReportItem>> call3, @NonNull Throwable t) {
                                if (isAdded()) {
                                    khoiTaoBieuDoMacDinh();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ReportItem>> call2, @NonNull Throwable t) {
                        if (isAdded()) {
                            khoiTaoBieuDoMacDinh();
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    khoiTaoBieuDoMacDinh();
                }
            }
        });
    }

    private void xuLyVaHienThiBaoCao(List<ReportItem> listDonKham, List<ReportItem> listPatients, List<ReportItem> listBills) {
        Calendar startMonth = Calendar.getInstance();
        startMonth.set(Calendar.DAY_OF_MONTH, 1);
        startMonth.add(Calendar.MONTH, -5);

        Map<String, Float> revenueByMonth = new LinkedHashMap<>();
        Map<String, Float> patientByMonth = new LinkedHashMap<>();
        Map<String, Float> formsByMonth = new LinkedHashMap<>();

        String[] labels = new String[6];
        Calendar cursor = (Calendar) startMonth.clone();
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        for (int i = 0; i < 6; i++) {
            String key = keyFormat.format(cursor.getTime());
            revenueByMonth.put(key, 0f);
            patientByMonth.put(key, 0f);
            formsByMonth.put(key, 0f);

            labels[i] = "T" + (cursor.get(Calendar.MONTH) + 1);
            cursor.add(Calendar.MONTH, 1);
        }

        // 1. Phân bổ Doanh thu
        for (ReportItem item : listBills) {
            Calendar date = parseReportDate(item.getCreated_at());
            if (date == null) continue;

            String key = keyFormat.format(date.getTime());
            if (revenueByMonth.containsKey(key)) {
                float currentRev = revenueByMonth.get(key);
                revenueByMonth.put(key, currentRev + (float) (item.getBillTongThanhToan() / 1_000_000f));
            }
        }

        // 2. Phân bổ Phiếu khám
        for (ReportItem item : listDonKham) {
            Calendar date = parseReportDate(item.getNgay_kham());
            if (date == null) continue;

            String key = keyFormat.format(date.getTime());
            if (formsByMonth.containsKey(key)) {
                if (!"Đã hủy".equalsIgnoreCase(item.getTrang_thai()) && !"Da huy".equalsIgnoreCase(item.getTrang_thai())) {
                    float currentForms = formsByMonth.get(key);
                    formsByMonth.put(key, currentForms + 1f);
                }
            }
        }

        // 3. Phân bổ Bệnh nhân mới
        for (ReportItem item : listPatients) {
            Calendar date = parseReportDate(item.getCreated_at());
            if (date == null) continue;

            String key = keyFormat.format(date.getTime());
            if (patientByMonth.containsKey(key)) {
                float currentPatients = patientByMonth.get(key);
                patientByMonth.put(key, currentPatients + 1f);
            }
        }

        float[] valuesRevenue = new float[6];
        float[] valuesPatients = new float[6];
        float[] valuesForms = new float[6];

        int index = 0;
        for (Float value : revenueByMonth.values()) valuesRevenue[index++] = value;

        index = 0;
        for (Float value : patientByMonth.values()) valuesPatients[index++] = value;

        index = 0;
        for (Float value : formsByMonth.values()) valuesForms[index++] = value;

        String changeRevenue = tinhPhanTramTangTruong(valuesRevenue);
        String changePatients = tinhPhanTramTangTruong(valuesPatients);
        String changeForms = tinhPhanTramTangTruong(valuesForms);

        List<AdminChartData> list = new ArrayList<>();
        list.add(new AdminChartData("Doanh thu", "6 tháng gần nhất (triệu đồng)", changeRevenue, labels, valuesRevenue));
        list.add(new AdminChartData("Bệnh nhân", "6 tháng gần nhất (người)", changePatients, labels, valuesPatients));
        list.add(new AdminChartData("Phiếu khám", "6 tháng gần nhất (phiếu)", changeForms, labels, valuesForms));

        adapterCharts = new AdminChartsAdapter(list);
        viewPagerCharts.setAdapter(adapterCharts);
        setupIndicators(list.size());

        viewPagerCharts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position, list.size());
            }
        });
    }

    private String tinhPhanTramTangTruong(float[] values) {
        if (values.length < 2) return "+0.0%";

        float previous = values[values.length - 2];
        float current = values[values.length - 1];
        if (previous <= 0f) {
            return current > 0f ? "+100%" : "+0.0%";
        }

        float percent = (current - previous) * 100f / previous;
        String prefix = percent >= 0 ? "+" : "";
        return String.format(Locale.getDefault(), "%s%.1f%%", prefix, percent);
    }

    private Calendar parseReportDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String cleaned = value.trim();
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(0, 10);
        }
        String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy"};
        for (String pattern : patterns) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat(pattern, Locale.getDefault()).parse(cleaned));
                return calendar;
            } catch (ParseException ignored) {

            }
        }
        return null;
    }

    private void khoiTaoBieuDoMacDinh() {
        String[] labels = new String[6];
        float[] values = new float[6];
        Calendar cursor = Calendar.getInstance();
        cursor.add(Calendar.MONTH, -5);
        for (int i = 0; i < 6; i++) {
            labels[i] = "T" + (cursor.get(Calendar.MONTH) + 1);
            cursor.add(Calendar.MONTH, 1);
        }

        List<AdminChartData> list = new ArrayList<>();
        list.add(new AdminChartData("Doanh thu", "6 tháng gần nhất (triệu đồng)", "+0.0%", labels, values));
        list.add(new AdminChartData("Bệnh nhân", "6 tháng gần nhất (người)", "+0.0%", labels, values));
        list.add(new AdminChartData("Phiếu khám", "6 tháng gần nhất (phiếu)", "+0.0%", labels, values));

        if (viewPagerCharts != null) {
            adapterCharts = new AdminChartsAdapter(list);
            viewPagerCharts.setAdapter(adapterCharts);
            setupIndicators(list.size());
        }
    }

    private void setupIndicators(int count) {
        if (layoutIndicators == null) return;
        layoutIndicators.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == 0 ? dpToPx(16) : dpToPx(6),
                    dpToPx(6)
            );
            params.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.bg_dot_indicator_active : R.drawable.bg_dot_indicator_inactive);
            layoutIndicators.addView(dot);
        }
    }

    private void updateIndicators(int position, int count) {
        if (layoutIndicators == null) return;
        for (int i = 0; i < count; i++) {
            View dot = layoutIndicators.getChildAt(i);
            if (dot == null) continue;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            if (i == position) {
                params.width = dpToPx(16);
                dot.setBackgroundResource(R.drawable.bg_dot_indicator_active);
            } else {
                params.width = dpToPx(6);
                dot.setBackgroundResource(R.drawable.bg_dot_indicator_inactive);
            }
            dot.setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static class AdminChartData {
        String title;
        String subtitle;
        String changePercent;
        String[] labels;
        float[] values;

        public AdminChartData(String title, String subtitle, String changePercent, String[] labels, float[] values) {
            this.title = title;
            this.subtitle = subtitle;
            this.changePercent = changePercent;
            this.labels = labels;
            this.values = values;
        }
    }

    private class AdminChartsAdapter extends RecyclerView.Adapter<AdminChartsAdapter.ChartViewHolder> {
        private final List<AdminChartData> chartDataList;

        public AdminChartsAdapter(List<AdminChartData> chartDataList) {
            this.chartDataList = chartDataList;
        }

        @NonNull
        @Override
        public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_chart_page, parent, false);
            return new ChartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
            AdminChartData data = chartDataList.get(position);
            holder.tvTitle.setText(data.title);
            holder.tvSubtitle.setText(data.subtitle);
            holder.tvChange.setText(data.changePercent);

            if (data.changePercent.startsWith("-")) {
                holder.tvChange.setTextColor(android.graphics.Color.parseColor("#C53243"));
                holder.tvChange.setBackgroundResource(R.drawable.bg_admin_revenue_badge_red);
            } else {
                holder.tvChange.setTextColor(android.graphics.Color.parseColor("#3F7D3A"));
                holder.tvChange.setBackgroundResource(R.drawable.bg_admin_revenue_badge);
            }

            holder.chartView.setData(data.labels, data.values);
        }

        @Override
        public int getItemCount() {
            return chartDataList.size();
        }

        class ChartViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvSubtitle;
            TextView tvChange;
            RevenueChartView chartView;

            public ChartViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvChartTitle);
                tvSubtitle = itemView.findViewById(R.id.tvChartSubtitle);
                tvChange = itemView.findViewById(R.id.tvChartChange);
                chartView = itemView.findViewById(R.id.chartView);
            }
        }
    }
}
