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
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

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

    private Button mngStaff;
    private TextView txtName;
    private TextView tvRevenueChange;
    private RevenueChartView chartRevenue;
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
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        View btnXemBaoCao = view.findViewById(R.id.btn_xem_bao_cao);
        btnXemBaoCao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ReportsActivity_Admin.class);
                startActivity(intent);
            }
        });
        View btnCaiDatThongBao = view.findViewById(R.id.btn_cai_dat_thong_bao);
        btnCaiDatThongBao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationSettingsFragment_Admin notificationsettingsFragment = new NotificationSettingsFragment_Admin();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, notificationsettingsFragment)
                        .addToBackStack(null)
                        .commit();
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
        tvRevenueChange = view.findViewById(R.id.tv_admin_revenue_change);
        chartRevenue = view.findViewById(R.id.chart_admin_revenue);
        reportApiService = SupabaseClientProvider.getClient(requireContext()).create(ReportApiService.class);
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

        View btnMngMedicineClinical = view.findViewById(R.id.btn_mngMedicineClinical);
        if (btnMngMedicineClinical != null) {
            btnMngMedicineClinical.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), dashboard_fragment.admin_manage_medicine_clinical.AdminManageMedicineClinicalActivity.class);
                startActivity(intent);
            });
        }

        taiDuLieuDoanhThu();
    }

    private void taiDuLieuDoanhThu() {
        khoiTaoBieuDoMacDinh();
        reportApiService.getDanhSachDonKham().enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportItem>> call, @NonNull Response<List<ReportItem>> response) {
                if (!isAdded() || chartRevenue == null || !response.isSuccessful() || response.body() == null) {
                    return;
                }
                capNhatBieuDoDoanhThu(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportItem>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    khoiTaoBieuDoMacDinh();
                }
            }
        });
    }

    private void capNhatBieuDoDoanhThu(List<ReportItem> reportItems) {
        Calendar startMonth = Calendar.getInstance();
        startMonth.set(Calendar.DAY_OF_MONTH, 1);
        startMonth.add(Calendar.MONTH, -5);

        Map<String, Float> revenueByMonth = new LinkedHashMap<>();
        String[] labels = new String[6];
        Calendar cursor = (Calendar) startMonth.clone();
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        for (int i = 0; i < 6; i++) {
            String key = keyFormat.format(cursor.getTime());
            revenueByMonth.put(key, 0f);
            labels[i] = "T" + (cursor.get(Calendar.MONTH) + 1);
            cursor.add(Calendar.MONTH, 1);
        }

        for (ReportItem item : reportItems) {
            Calendar date = parseReportDate(item.getNgay_kham());
            if (date == null) continue;

            String key = keyFormat.format(date.getTime());
            if (revenueByMonth.containsKey(key)) {
                float current = revenueByMonth.get(key);
                revenueByMonth.put(key, current + (float) (item.getTongDoanhThu() / 1_000_000f));
            }
        }

        float[] values = new float[6];
        int index = 0;
        for (Float value : revenueByMonth.values()) {
            values[index++] = value;
        }

        chartRevenue.setData(labels, values);
        capNhatPhanTramTangTruong(values);
    }

    private void capNhatPhanTramTangTruong(float[] values) {
        if (tvRevenueChange == null || values.length < 2) return;

        float previous = values[values.length - 2];
        float current = values[values.length - 1];
        if (previous <= 0f) {
            tvRevenueChange.setText(current > 0f ? "+100%" : "+0.0%");
            return;
        }

        float percent = (current - previous) * 100f / previous;
        String prefix = percent >= 0 ? "+" : "";
        tvRevenueChange.setText(String.format(Locale.getDefault(), "%s%.1f%%", prefix, percent));
    }

    private Calendar parseReportDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy"};
        for (String pattern : patterns) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat(pattern, Locale.getDefault()).parse(value.trim()));
                return calendar;
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private void khoiTaoBieuDoMacDinh() {
        if (chartRevenue == null) return;

        String[] labels = new String[6];
        float[] values = new float[6];
        Calendar cursor = Calendar.getInstance();
        cursor.add(Calendar.MONTH, -5);
        for (int i = 0; i < 6; i++) {
            labels[i] = "T" + (cursor.get(Calendar.MONTH) + 1);
            cursor.add(Calendar.MONTH, 1);
        }
        chartRevenue.setData(labels, values);
        if (tvRevenueChange != null) {
            tvRevenueChange.setText("+0.0%");
        }
    }
}
