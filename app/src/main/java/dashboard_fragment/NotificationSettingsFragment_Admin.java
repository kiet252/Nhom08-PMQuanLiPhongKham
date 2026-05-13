package dashboard_fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.DoctorBriefDto;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.PatientBriefDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationSettingsFragment_Admin extends Fragment {
    private static final long AUTO_REFRESH_DELAY_MS = 8000L;
    private static final String STATUS_WAITING = "Chờ khám";
    private static final String STATUS_IN_PROGRESS = "Đang khám";
    private static final String STATUS_DONE = "Đã khám";

    private LinearLayout layoutDanhSach;
    private TextView tvEmptyState;
    private TextView tvLoadingState;
    private TextView tvTotal;

    private ExaminationFormRepository repository;
    private FrameLayout rootContainer;
    private View listView;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (listView != null && listView.getVisibility() == View.VISIBLE) {
                taiDuLieu(false);
            }
            refreshHandler.postDelayed(this, AUTO_REFRESH_DELAY_MS);
        }
    };

    public NotificationSettingsFragment_Admin() {
    }

    public static NotificationSettingsFragment_Admin newInstance() {
        return new NotificationSettingsFragment_Admin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new ExaminationFormRepository(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootContainer = new FrameLayout(requireContext());
        listView = inflater.inflate(R.layout.activity_notification_settings_fragment_admin, rootContainer, false);
        rootContainer.addView(listView);

        layoutDanhSach = listView.findViewById(R.id.layout_danh_sach_thong_bao_fragment_admin);
        tvEmptyState = listView.findViewById(R.id.tv_admin_notification_empty);
        tvLoadingState = listView.findViewById(R.id.tv_admin_notification_loading);
        tvTotal = listView.findViewById(R.id.tv_admin_notification_total);

        taiDuLieu(true);
        return rootContainer;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
        refreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_DELAY_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
        layoutDanhSach = null;
        tvEmptyState = null;
        tvLoadingState = null;
        tvTotal = null;
        listView = null;
        rootContainer = null;
    }

    private void taiDuLieu(boolean showLoading) {
        if (showLoading) {
            hienThiDangTai();
        }

        repository.getAllFormsToday().enqueue(new Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call,
                                   @NonNull Response<List<ExaminationFormWithPatientDto>> response) {
                if (!isAdded() || layoutDanhSach == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    hienThiDanhSach(response.body());
                    return;
                }

                String message = "Không tải được thông báo phiếu khám.";
                try {
                    if (response.errorBody() != null) {
                        message += " Mã lỗi: " + response.code() + " - " + response.errorBody().string();
                    }
                } catch (IOException ignored) {
                    message += " Mã lỗi: " + response.code();
                }
                hienThiLoi(message);
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || layoutDanhSach == null) {
                    return;
                }
                hienThiLoi("Lỗi kết nối khi tải thông báo: " + t.getMessage());
            }
        });
    }

    private void hienThiDangTai() {
        if (tvLoadingState != null) {
            tvLoadingState.setVisibility(View.VISIBLE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
        if (layoutDanhSach != null) {
            layoutDanhSach.removeAllViews();
        }
    }

    private void hienThiDanhSach(List<ExaminationFormWithPatientDto> forms) {
        if (tvLoadingState != null) {
            tvLoadingState.setVisibility(View.GONE);
        }
        layoutDanhSach.removeAllViews();

        List<ExaminationFormWithPatientDto> displayForms = sapXepTheoNgayVaGio(forms);
        if (tvTotal != null) {
            tvTotal.setText(String.valueOf(displayForms.size()));
        }

        if (displayForms.isEmpty()) {
            if (tvEmptyState != null) {
                tvEmptyState.setText("Chưa có thông báo phiếu khám.");
                tvEmptyState.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }

        Map<String, List<ExaminationFormWithPatientDto>> formsByDate = nhomTheoNgay(displayForms);
        for (Map.Entry<String, List<ExaminationFormWithPatientDto>> entry : formsByDate.entrySet()) {
            themNhomNgay(entry.getKey(), entry.getValue());
        }
    }

    private List<ExaminationFormWithPatientDto> sapXepTheoNgayVaGio(List<ExaminationFormWithPatientDto> forms) {
        List<ExaminationFormWithPatientDto> result = new ArrayList<>();
        if (forms != null) {
            Date today = new Date();
            for (ExaminationFormWithPatientDto form : forms) {
                if (nenHienThiPhieu(form, today)) {
                    result.add(form);
                }
            }
        }

        Collections.sort(result, new Comparator<ExaminationFormWithPatientDto>() {
            @Override
            public int compare(ExaminationFormWithPatientDto first, ExaminationFormWithPatientDto second) {
                int dateCompare = second.getNgay_kham().compareTo(first.getNgay_kham());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return layChuoi(first.getGio_du_kien(), "").compareTo(layChuoi(second.getGio_du_kien(), ""));
            }
        });
        return result;
    }

    private boolean nenHienThiPhieu(ExaminationFormWithPatientDto form, Date today) {
        if (form == null || form.getNgay_kham() == null) {
            return false;
        }

        if (laCungNgay(form.getNgay_kham(), today)) {
            return true;
        }

        return laNgayQuaKhu(form.getNgay_kham(), today)
                && STATUS_IN_PROGRESS.equalsIgnoreCase(layChuoi(form.getTrang_thai(), ""));
    }

    private boolean laCungNgay(Date first, Date second) {
        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(first);

        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTime(second);

        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)
                && firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean laNgayQuaKhu(Date date, Date today) {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        xoaGio(dateCalendar);

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);
        xoaGio(todayCalendar);

        return dateCalendar.before(todayCalendar);
    }

    private void xoaGio(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private Map<String, List<ExaminationFormWithPatientDto>> nhomTheoNgay(List<ExaminationFormWithPatientDto> forms) {
        Map<String, List<ExaminationFormWithPatientDto>> groups = new LinkedHashMap<>();
        for (ExaminationFormWithPatientDto form : forms) {
            String dateKey = dateFormat.format(form.getNgay_kham());
            if (!groups.containsKey(dateKey)) {
                groups.put(dateKey, new ArrayList<>());
            }
            groups.get(dateKey).add(form);
        }
        return groups;
    }

    private void themNhomNgay(String ngay, List<ExaminationFormWithPatientDto> forms) {
        Context context = requireContext();

        TextView tvNgay = new TextView(context);
        tvNgay.setText(ngay);
        tvNgay.setTextColor(Color.parseColor("#1E90FF"));
        tvNgay.setTextSize(15);
        tvNgay.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tvNgay.setGravity(Gravity.CENTER_VERTICAL);
        tvNgay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_date_exam_wrapper, 0, 0, 0);
        tvNgay.setCompoundDrawablePadding(dp(8));
        tvNgay.setPadding(dp(16), 0, dp(16), 0);
        tvNgay.setBackground(taoNen("#EAF4FF", 26, "#1E90FF", 1));

        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(54)
        );
        dateParams.setMargins(0, dp(8), 0, dp(18));
        layoutDanhSach.addView(tvNgay, dateParams);

        for (ExaminationFormWithPatientDto form : forms) {
            layoutDanhSach.addView(taoCardThongBao(form));
        }
    }

    private View taoCardThongBao(ExaminationFormWithPatientDto form) {
        Context context = requireContext();
        View card = LayoutInflater.from(context).inflate(
                R.layout.admin_item_examination_notification,
                layoutDanhSach,
                false
        );

        View accent = card.findViewById(R.id.viewAdminNotificationAccent);
        TextView tvTime = card.findViewById(R.id.tvAdminNotificationTime);
        TextView tvPatientName = card.findViewById(R.id.tvAdminNotificationPatientName);
        TextView tvStatus = card.findViewById(R.id.tvAdminNotificationStatus);
        TextView tvPatientCode = card.findViewById(R.id.tvAdminNotificationPatientCode);
        TextView tvSymptoms = card.findViewById(R.id.tvAdminNotificationSymptoms);
        ImageView ivMore = card.findViewById(R.id.ivAdminNotificationMore);
        StatusColor statusColor = layMauTrangThai(form.getTrang_thai());

        accent.setBackgroundColor(statusColor.accentColor);
        tvTime.setText(dinhDangGio(form.getGio_du_kien()));
        tvPatientName.setText(layTenBenhNhan(form));
        tvStatus.setText("• " + layChuoi(form.getTrang_thai(), STATUS_WAITING));
        tvStatus.setTextColor(statusColor.textColor);
        tvStatus.setBackgroundResource(layNenTrangThai(form.getTrang_thai()));
        tvPatientCode.setText("Mã BN: " + layMaBenhNhan(form) + " - Số tiếp nhận: " + form.getSo_tiep_nhan());
        tvSymptoms.setText(layChuoi(form.getTrieu_chung_ban_dau(), "Không có triệu chứng ban đầu"));
        ivMore.setColorFilter(statusColor.textColor);

        card.setOnClickListener(v -> hienThiChiTiet(form));

        return card;
    }

    private void hienThiChiTiet(ExaminationFormWithPatientDto form) {
        if (rootContainer == null) return;

        View detailView = LayoutInflater.from(requireContext()).inflate(R.layout.admin_examination_form_detail, rootContainer, false);

        ImageButton btnBack = detailView.findViewById(R.id.btnBackAdminExDetail);
        TextView tvTitleTop = detailView.findViewById(R.id.tvAdminExDetailPatientNameTop);
        TextView tvStatus = detailView.findViewById(R.id.tvAdminExDetailStatus);
        TextView tvDateTime = detailView.findViewById(R.id.tvAdminExDetailDateTime);
        TextView tvPatientCode = detailView.findViewById(R.id.tvAdminExDetailPatientCode);
        TextView tvDoctorName = detailView.findViewById(R.id.tvAdminExDetailDoctorName);
        TextView tvDoctorRole = detailView.findViewById(R.id.tvAdminExDetailDoctorRole);
        TextView tvSymptoms = detailView.findViewById(R.id.tvAdminExDetailSymptoms);
        TextView tvFee = detailView.findViewById(R.id.tvAdminExDetailFee);
        TextView tvPaymentMethod = detailView.findViewById(R.id.tvAdminExDetailPaymentMethod);

        StatusColor sc = layMauTrangThai(form.getTrang_thai());
        PatientBriefDto patient = form.getPatient();
        DoctorBriefDto doctor = form.getDoctor();

        tvTitleTop.setText("Bệnh nhân: " + (patient != null ? patient.getHo_ten() : "N/A"));
        tvStatus.setText("• " + layChuoi(form.getTrang_thai(), STATUS_WAITING));
        tvStatus.setTextColor(sc.textColor);
        tvStatus.setBackgroundResource(layNenTrangThai(form.getTrang_thai()));

        String dateStr = form.getNgay_kham() != null ? dateFormat.format(form.getNgay_kham()) : "--/--/----";
        tvDateTime.setText(dinhDangGio(form.getGio_du_kien()) + " | " + dateStr);

        tvPatientCode.setText("Mã BN: " + layMaBenhNhan(form) + " | STN: " + form.getSo_tiep_nhan());

        if (doctor != null) {
            tvDoctorName.setText("BS. " + layChuoi(doctor.getHo_ten(), "Chưa phân công"));
            tvDoctorRole.setText(layChuoi(doctor.getChuc_vu(), "Bác sĩ chuyên khoa"));
        } else {
            tvDoctorName.setText("Chưa phân công bác sĩ");
            tvDoctorRole.setText("");
        }

        tvSymptoms.setText(layChuoi(form.getTrieu_chung_ban_dau(), "Không có triệu chứng chi tiết."));
        tvFee.setText(String.format("%,d VNĐ", form.getPhi_kham()));
        tvPaymentMethod.setText(layChuoi(form.getPhuong_thuc_thanh_toan(), "Tiền mặt"));

        btnBack.setOnClickListener(v -> {
            rootContainer.removeView(detailView);
            listView.setVisibility(View.VISIBLE);
        });

        listView.setVisibility(View.GONE);
        rootContainer.addView(detailView);
    }

    private String layTenBenhNhan(ExaminationFormWithPatientDto form) {
        PatientBriefDto patient = form.getPatient();
        if (patient != null) {
            return layChuoi(patient.getHo_ten(), "Không rõ bệnh nhân");
        }
        return "Không rõ bệnh nhân";
    }

    private String layMaBenhNhan(ExaminationFormWithPatientDto form) {
        PatientBriefDto patient = form.getPatient();
        if (patient != null) {
            return layChuoi(patient.getId(), layChuoi(form.getPatient_id(), "--"));
        }
        return layChuoi(form.getPatient_id(), "--");
    }

    private String dinhDangGio(String rawTime) {
        String displayTime = layChuoi(rawTime, "--");
        String[] parts = displayTime.split(":");
        if (parts.length >= 2) {
            return parts[0] + ":" + parts[1];
        }
        return displayTime;
    }

    private String layChuoi(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private StatusColor layMauTrangThai(String status) {
        if (STATUS_DONE.equalsIgnoreCase(status)) {
            return new StatusColor(Color.parseColor("#16A34A"), Color.parseColor("#16A34A"), "#E8F7EF");
        }
        if (STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            return new StatusColor(Color.parseColor("#2563EB"), Color.parseColor("#2563EB"), "#EAF4FF");
        }
        return new StatusColor(Color.parseColor("#F59E0B"), Color.parseColor("#E67E22"), "#FFF4DF");
    }

    private int layNenTrangThai(String status) {
        if (STATUS_DONE.equalsIgnoreCase(status)) {
            return R.drawable.bg_status_examined;
        }
        if (STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            return R.drawable.bg_status_examining;
        }
        return R.drawable.bg_status_wait_exam;
    }

    private void hienThiLoi(String message) {
        if (tvLoadingState != null) {
            tvLoadingState.setVisibility(View.GONE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private GradientDrawable taoNen(String solidColor, int radiusDp, String strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(solidColor));
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), Color.parseColor(strokeColor));
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class StatusColor {
        final int accentColor;
        final int textColor;
        final String backgroundHex;

        StatusColor(int accentColor, int textColor, String backgroundHex) {
            this.accentColor = accentColor;
            this.textColor = textColor;
            this.backgroundHex = backgroundHex;
        }
    }
}
