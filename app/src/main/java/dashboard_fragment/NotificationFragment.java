package dashboard_fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.ThongBao;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private LinearLayout layoutDanhSachThongBao;
    private FrameLayout rootContainer;
    private View listView;
    private AuthRepository authRepository;
    private UserRole userRole = UserRole.NHAN_VIEN;
    private AlertDialog dialogChiTietThongBao;
    private SharedPreferences notificationReadPrefs;
    private Set<String> readNotificationKeys = new HashSet<>();
    private String currentUserId = "guest";

    public NotificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootContainer = new FrameLayout(requireContext());
        listView = inflater.inflate(R.layout.fragment_notification, rootContainer, false);
        rootContainer.addView(listView);
        cauHinhManHinhDanhSach(listView);
        return rootContainer;
    }

    private void cauHinhManHinhDanhSach(View view) {
        layoutDanhSachThongBao = view.findViewById(R.id.layout_danh_sach_thong_bao);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String roleSaved = prefs.getString("ROLE", UserRole.NHAN_VIEN.name());
        try {
            userRole = UserRole.valueOf(roleSaved);
        } catch (Exception e) {
            userRole = UserRole.NHAN_VIEN;
        }

        UserProfile profile = SharedPrefManager.getInstance(requireContext()).getProfile();
        if (profile != null && profile.getID() != null && !profile.getID().trim().isEmpty()) {
            currentUserId = profile.getID();
        }
        notificationReadPrefs = requireContext().getSharedPreferences("NotificationReadPrefs", Context.MODE_PRIVATE);
        readNotificationKeys = new HashSet<>(notificationReadPrefs.getStringSet(readPrefKey(), new HashSet<>()));

        goiDuLieuRetrofit();
    }

    private void goiDuLieuRetrofit() {
        authRepository.layDanhSachThongBao(userRole.name()).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response) {
                if (!isAdded() || layoutDanhSachThongBao == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<ThongBao> ds = response.body();
                    layoutDanhSachThongBao.removeAllViews();

                    Collections.reverse(ds);
                    String ngayGanNhat = "";
                    for (ThongBao tb : ds) {
                        String ngayThongBao = dinhDangNgay(tb.getCreated_at());
                        if (!ngayThongBao.isEmpty() && !ngayThongBao.equals(ngayGanNhat)) {
                            themNgayVaoDanhSach(ngayThongBao);
                            ngayGanNhat = ngayThongBao;
                        }
                        themVaoKhungXam(tb);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                    Log.d("Load notification error", t.getMessage());
                }
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }

    private void themVaoKhungXam(ThongBao thongBao) {
        if (layoutDanhSachThongBao == null) return;

        String tieuDe = thongBao.getTieu_de();
        String noiDung = thongBao.getNoi_dung();
        String thoiGianTrongNgay = dinhDangGioNeuTrongNgay(thongBao.getCreated_at());
        String notificationKey = taoNotificationKey(thongBao);
        boolean daDoc = readNotificationKeys.contains(notificationKey);

        MaterialCardView cardMoi = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCard.setMargins(0, 0, 0, dpToPx(16));
        cardMoi.setLayoutParams(paramsCard);
        cardMoi.setCardBackgroundColor(daDoc ? Color.WHITE : Color.parseColor("#EAF5FF"));
        cardMoi.setRadius(dpToPx(18));
        cardMoi.setCardElevation(dpToPx(2));
        cardMoi.setOnClickListener(v -> {
            danhDauDaDoc(notificationKey);
            cardMoi.setCardBackgroundColor(Color.WHITE);
            hienThiChiTietThongBao(tieuDe, noiDung, thoiGianTrongNgay);
        });

        LinearLayout lopNgang = new LinearLayout(requireContext());
        lopNgang.setOrientation(LinearLayout.HORIZONTAL);
        lopNgang.setGravity(Gravity.CENTER_VERTICAL);
        lopNgang.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout lopDoc = new LinearLayout(requireContext());
        lopDoc.setOrientation(LinearLayout.VERTICAL);
        lopDoc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvTieuDe = new TextView(requireContext());
        tvTieuDe.setText(tieuDe);
        tvTieuDe.setTextColor(Color.parseColor("#1E293B"));
        tvTieuDe.setTextSize(18);
        tvTieuDe.setTypeface(null, Typeface.BOLD);

        TextView tvNoiDung = new TextView(requireContext());
        tvNoiDung.setText(noiDung);
        tvNoiDung.setTextColor(Color.parseColor("#64748B"));
        tvNoiDung.setTextSize(14);
        tvNoiDung.setPadding(0, dpToPx(8), 0, 0);

        lopDoc.addView(tvTieuDe);
        lopDoc.addView(tvNoiDung);
        if (!thoiGianTrongNgay.isEmpty()) {
            TextView tvThoiGian = new TextView(requireContext());
            tvThoiGian.setText(thoiGianTrongNgay);
            tvThoiGian.setTextColor(Color.parseColor("#0D5FA8"));
            tvThoiGian.setTextSize(12);
            tvThoiGian.setTypeface(null, Typeface.BOLD);
            tvThoiGian.setPadding(0, dpToPx(8), 0, 0);
            lopDoc.addView(tvThoiGian);
        }
        lopNgang.addView(lopDoc);
        cardMoi.addView(lopNgang);

        layoutDanhSachThongBao.addView(cardMoi);
    }

    private void themNgayVaoDanhSach(String ngayThongBao) {
        TextView tvNgay = new TextView(requireContext());
        tvNgay.setText("  " + ngayThongBao);
        tvNgay.setTextColor(Color.parseColor("#2196F3"));
        tvNgay.setTextSize(16);
        tvNgay.setTypeface(null, Typeface.BOLD);
        tvNgay.setGravity(Gravity.CENTER_VERTICAL);
        tvNgay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_date_exam_wrapper, 0, 0, 0);
        tvNgay.setCompoundDrawablePadding(dpToPx(8));
        tvNgay.setPadding(dpToPx(16), 0, dpToPx(16), 0);
        tvNgay.setBackground(taoNenNgay());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(56)
        );
        params.setMargins(0, 0, 0, dpToPx(18));
        layoutDanhSachThongBao.addView(tvNgay, params);
    }

    private void hienThiChiTietThongBao(String tieuDe, String noiDung, String thoiGianTrongNgay)
    {
        if (!isAdded()) return;

        dialogChiTietThongBao = new AlertDialog.Builder(requireContext()).create();
        dialogChiTietThongBao.setView(taoCuaSoChiTietThongBao(tieuDe, noiDung, thoiGianTrongNgay));
        dialogChiTietThongBao.setOnShowListener(dialogInterface -> {
            if (dialogChiTietThongBao.getWindow() != null) {
                dialogChiTietThongBao.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialogChiTietThongBao.getWindow().setLayout(dpToPx(330), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        dialogChiTietThongBao.show();
    }

    private View taoCuaSoChiTietThongBao(String tieuDe, String noiDung, String thoiGianTrongNgay)
    {
        CardView contentCard = new CardView(requireContext());
        contentCard.setCardBackgroundColor(Color.WHITE);
        contentCard.setRadius(dpToPx(24));
        contentCard.setCardElevation(dpToPx(8));

        LinearLayout dialogRoot = new LinearLayout(requireContext());
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        contentCard.addView(dialogRoot, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18));
        header.setBackground(taoNenHeaderDialog());
        dialogRoot.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ImageView headerIcon = new ImageView(requireContext());
        headerIcon.setImageResource(R.drawable.ic_notification_bell);
        headerIcon.setColorFilter(Color.WHITE);
        headerIcon.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        headerIcon.setBackground(taoNenBoGoc(Color.parseColor("#2B7BBB"), dpToPx(18)));
        LinearLayout.LayoutParams headerIconParams = new LinearLayout.LayoutParams(dpToPx(42), dpToPx(42));
        headerIconParams.setMargins(0, 0, dpToPx(14), 0);
        header.addView(headerIcon, headerIconParams);

        LinearLayout headerText = new LinearLayout(requireContext());
        headerText.setOrientation(LinearLayout.VERTICAL);
        header.addView(headerText, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView tvHeader = new TextView(requireContext());
        tvHeader.setText("Chi tiết thông báo");
        tvHeader.setTextColor(Color.WHITE);
        tvHeader.setTextSize(20);
        tvHeader.setTypeface(null, Typeface.BOLD);
        headerText.addView(tvHeader);

        ScrollView scrollView = new ScrollView(requireContext());
        dialogRoot.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(22), dpToPx(20), dpToPx(22), dpToPx(18));
        content.setBackgroundColor(Color.WHITE);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(tieuDe);
        tvTitle.setTextColor(Color.parseColor("#0D3F6E"));
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setLineSpacing(dpToPx(3), 1f);
        content.addView(tvTitle);

        if (!thoiGianTrongNgay.isEmpty()) {
            TextView tvTime = new TextView(requireContext());
            tvTime.setText(thoiGianTrongNgay);
            tvTime.setTextColor(Color.parseColor("#0D5FA8"));
            tvTime.setTextSize(13);
            tvTime.setTypeface(null, Typeface.BOLD);
            tvTime.setPadding(0, dpToPx(8), 0, 0);
            content.addView(tvTime);
        }

        View line = new View(requireContext());
        line.setBackgroundColor(Color.parseColor("#E2E8F0"));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(1)
        );
        lineParams.setMargins(0, dpToPx(16), 0, dpToPx(16));
        content.addView(line, lineParams);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText("Nội dung");
        tvLabel.setTextColor(Color.parseColor("#0D5FA8"));
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, Typeface.BOLD);
        content.addView(tvLabel);

        TextView tvContent = new TextView(requireContext());
        tvContent.setText(noiDung);
        tvContent.setTextColor(Color.parseColor("#1E293B"));
        tvContent.setTextSize(17);
        tvContent.setLineSpacing(dpToPx(5), 1f);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dpToPx(8), 0, 0);
        content.addView(tvContent, contentParams);

        TextView btnDong = new TextView(requireContext());
        btnDong.setText("Đóng");
        btnDong.setTextColor(Color.WHITE);
        btnDong.setTextSize(15);
        btnDong.setTypeface(null, Typeface.BOLD);
        btnDong.setGravity(Gravity.CENTER);
        btnDong.setBackground(taoNenBoGoc(Color.parseColor("#0D5FA8"), dpToPx(10)));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(46)
        );
        closeParams.setMargins(0, dpToPx(24), 0, 0);
        content.addView(btnDong, closeParams);
        btnDong.setOnClickListener(v -> {
            if (dialogChiTietThongBao != null && dialogChiTietThongBao.isShowing()) {
                dialogChiTietThongBao.dismiss();
            }
        });

        return contentCard;
    }

    private GradientDrawable taoNenHeaderDialog()
    {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#021B33"),
                        Color.parseColor("#08335E"),
                        Color.parseColor("#0D5FA8")
                }
        );
        drawable.setCornerRadii(new float[]{
                dpToPx(24), dpToPx(24),
                dpToPx(24), dpToPx(24),
                0, 0,
                0, 0
        });
        return drawable;
    }

    private GradientDrawable taoNenBoGoc(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable taoNenNgay() {
        GradientDrawable drawable = taoNenBoGoc(Color.parseColor("#EAF5FF"), dpToPx(24));
        drawable.setStroke(dpToPx(2), Color.parseColor("#2196F3"));
        return drawable;
    }

    private String readPrefKey() {
        return "read_notifications_" + currentUserId;
    }

    private String taoNotificationKey(ThongBao thongBao) {
        if (thongBao.getId() != null) {
            return String.valueOf(thongBao.getId());
        }
        return layChuoi(thongBao.getTieu_de()) + "|" + layChuoi(thongBao.getCreated_at());
    }

    private String layChuoi(String value) {
        return value == null ? "" : value.trim();
    }

    private void danhDauDaDoc(String notificationKey) {
        if (notificationKey.isEmpty() || readNotificationKeys.contains(notificationKey)) {
            return;
        }

        readNotificationKeys.add(notificationKey);
        notificationReadPrefs.edit()
                .putStringSet(readPrefKey(), new HashSet<>(readNotificationKeys))
                .apply();
    }

    private String dinhDangNgay(String createdAt) {
        Date date = parseCreatedAt(createdAt);
        if (date != null) {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        }
        String rawDate = layPhanNgay(createdAt);
        return rawDate.isEmpty() ? "" : rawDate;
    }

    private String dinhDangGioNeuTrongNgay(String createdAt) {
        Date date = parseCreatedAt(createdAt);
        if (date != null) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        }
        return layPhanGio(createdAt);
    }

    private Date parseCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return null;
        }

        String normalized = chuanHoaCreatedAt(createdAt);

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.getDefault()).parse(normalized);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String chuanHoaCreatedAt(String createdAt) {
        String normalized = createdAt.trim().replace("T", " ").replace("Z", "+00:00");
        int dotIndex = normalized.indexOf('.');
        if (dotIndex > 0) {
            int timezoneIndex = Math.max(normalized.lastIndexOf('+'), normalized.lastIndexOf('-'));
            if (timezoneIndex <= dotIndex) {
                timezoneIndex = normalized.length();
            }
            String fraction = normalized.substring(dotIndex + 1, timezoneIndex);
            if (fraction.length() > 3) {
                normalized = normalized.substring(0, dotIndex + 4) + normalized.substring(timezoneIndex);
            }
        }
        return normalized;
    }

    private String layPhanNgay(String createdAt) {
        if (createdAt == null || createdAt.length() < 10) {
            return "";
        }
        String[] parts = createdAt.substring(0, 10).split("-");
        if (parts.length != 3) {
            return "";
        }
        return parts[2] + "/" + parts[1] + "/" + parts[0];
    }

    private String layPhanGio(String createdAt) {
        if (createdAt == null || createdAt.length() < 16) {
            return "";
        }
        return createdAt.substring(11, 16);
    }
}
