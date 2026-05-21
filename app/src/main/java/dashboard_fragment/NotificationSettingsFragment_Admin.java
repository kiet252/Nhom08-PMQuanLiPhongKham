package dashboard_fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.ThongBao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationSettingsFragment_Admin extends Fragment {
    private static final long AUTO_REFRESH_DELAY_MS = 5000L;

    private View manHinhDanhSach;
    private View manHinhTao;
    private LinearLayout layoutDanhSach;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private TextView tvLoading;
    private TextView tvTabDaGui;
    private TextView tvTabNhap;
    private TextView tvChon;
    private TextView tvXoa;
    private View lineTabDaGui;
    private View lineTabNhap;
    private EditText edtTieuDe;
    private EditText edtNoiDung;
    private CheckBox cbTatCa;
    private CheckBox cbAdmin;
    private CheckBox cbBacSi;
    private CheckBox cbLeTan;
    private MaterialButton btnGuiThongBao;
    private MaterialButton btnVaoBanNhap;
    private AuthRepository authRepository;
    private boolean dangXemNhap = false;
    private boolean isSelectionMode = false;
    private final List<Integer> selectedIds = new ArrayList<>();
    private final List<CheckBox> listCheckBoxes = new ArrayList<>();
    private final List<ThongBao> danhSachBanNhap = new ArrayList<>();
    private final List<ThongBao> danhSachDaGui = new ArrayList<>();

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && manHinhDanhSach != null
                    && manHinhDanhSach.getVisibility() == View.VISIBLE
                    && !dangXemNhap) {
                goiDuLieuRetrofit(false);
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
        authRepository = new AuthRepository(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notification_settings_fragment_admin, container, false);
        anhXa(view);
        cauHinhSuKien();
        goiDuLieuRetrofit();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        datHienThiThanhDieuHuongDuoi(false);
        if (manHinhDanhSach != null && manHinhDanhSach.getVisibility() == View.VISIBLE && !dangXemNhap) {
            goiDuLieuRetrofit(false);
        }
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
        datHienThiThanhDieuHuongDuoi(true);
        refreshHandler.removeCallbacks(autoRefreshRunnable);
        manHinhDanhSach = null;
        manHinhTao = null;
        layoutDanhSach = null;
        layoutEmpty = null;
        super.onDestroyView();
    }

    private void datHienThiThanhDieuHuongDuoi(boolean hienThi) {
        if (getActivity() == null) return;

        View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(hienThi ? View.VISIBLE : View.GONE);
        }
    }

    private void anhXa(View view) {
        manHinhDanhSach = view.findViewById(R.id.layout_admin_notification_list_screen);
        manHinhTao = view.findViewById(R.id.layout_admin_notification_create_screen);
        layoutDanhSach = view.findViewById(R.id.layout_danh_sach_thong_bao_fragment_admin);
        layoutEmpty = view.findViewById(R.id.layout_admin_notification_empty);
        tvEmptyTitle = view.findViewById(R.id.tv_admin_notification_empty_title);
        tvEmptySubtitle = view.findViewById(R.id.tv_admin_notification_empty_subtitle);
        tvLoading = view.findViewById(R.id.tv_admin_notification_loading);
        tvTabDaGui = view.findViewById(R.id.tv_tab_da_gui_admin_notification);
        tvTabNhap = view.findViewById(R.id.tv_tab_nhap_admin_notification);
        tvChon = view.findViewById(R.id.btn_select_admin_notification);
        tvXoa = view.findViewById(R.id.btn_delete_admin_notification);
        lineTabDaGui = view.findViewById(R.id.line_tab_da_gui_admin_notification);
        lineTabNhap = view.findViewById(R.id.line_tab_nhap_admin_notification);
        edtTieuDe = view.findViewById(R.id.edt_admin_notification_title);
        edtNoiDung = view.findViewById(R.id.edt_admin_notification_content);
        cbTatCa = view.findViewById(R.id.cb_admin_notification_all);
        cbAdmin = view.findViewById(R.id.cb_admin_notification_admin);
        cbBacSi = view.findViewById(R.id.cb_admin_notification_doctor);
        cbLeTan = view.findViewById(R.id.cb_admin_notification_receptionist);
        btnGuiThongBao = view.findViewById(R.id.btn_admin_notification_send);
        btnVaoBanNhap = view.findViewById(R.id.btn_admin_notification_draft);
    }

    private void cauHinhSuKien() {
        ImageButton btnBackList = manHinhDanhSach.findViewById(R.id.btn_back_admin_notification);
        ImageButton btnBackCreate = manHinhTao.findViewById(R.id.btn_back_create_admin_notification);
        TextView btnThem = manHinhDanhSach.findViewById(R.id.btn_add_admin_notification);

        btnBackList.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        btnBackCreate.setOnClickListener(v -> hienThiManHinhDanhSach());
        btnThem.setOnClickListener(v -> hienThiManHinhTao());
        tvTabDaGui.setOnClickListener(v -> doiTab(false));
        tvTabNhap.setOnClickListener(v -> doiTab(true));
        tvChon.setOnClickListener(v -> doiCheDoChon());
        tvXoa.setOnClickListener(v -> thucHienXoaNhieu());
        btnGuiThongBao.setOnClickListener(v -> guiThongBao());
        btnVaoBanNhap.setOnClickListener(v -> duaVaoBanNhap());

        cbTatCa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbAdmin.setChecked(false);
                cbBacSi.setChecked(false);
                cbLeTan.setChecked(false);
            }
        });
        cbAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) cbTatCa.setChecked(false);
        });
        cbBacSi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) cbTatCa.setChecked(false);
        });
        cbLeTan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) cbTatCa.setChecked(false);
        });
    }

    private void hienThiManHinhTao() {
        manHinhDanhSach.setVisibility(View.GONE);
        manHinhTao.setVisibility(View.VISIBLE);
        edtTieuDe.setText("");
        edtNoiDung.setText("");
        cbTatCa.setChecked(false);
        cbAdmin.setChecked(false);
        cbBacSi.setChecked(true);
        cbLeTan.setChecked(false);
    }

    private void hienThiManHinhDanhSach() {
        manHinhTao.setVisibility(View.GONE);
        manHinhDanhSach.setVisibility(View.VISIBLE);
        doiTab(false);
    }

    private void doiTab(boolean xemNhap) {
        dangXemNhap = xemNhap;
        tvTabDaGui.setTextColor(Color.parseColor(xemNhap ? "#64748B" : "#0D3F6E"));
        tvTabNhap.setTextColor(Color.parseColor(xemNhap ? "#0D3F6E" : "#64748B"));
        tvTabDaGui.setTypeface(null, xemNhap ? Typeface.NORMAL : Typeface.BOLD);
        tvTabNhap.setTypeface(null, xemNhap ? Typeface.BOLD : Typeface.NORMAL);
        lineTabDaGui.setVisibility(xemNhap ? View.INVISIBLE : View.VISIBLE);
        lineTabNhap.setVisibility(xemNhap ? View.VISIBLE : View.INVISIBLE);
        tvChon.setVisibility(View.VISIBLE);
        tvXoa.setVisibility(View.VISIBLE);

        goiDuLieuRetrofit(true);
    }

    private void duaVaoBanNhap() {
        String tieuDe = edtTieuDe.getText().toString().trim();
        String noiDung = edtNoiDung.getText().toString().trim();

        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTatCa.isChecked() && !cbAdmin.isChecked() && !cbBacSi.isChecked() && !cbLeTan.isChecked()) {
            Toast.makeText(requireContext(), "Vui lòng chọn vai trò nhận thông báo", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetRoles = layVaiTroNhan();
        String encodedRole = encodeDraftRole(targetRoles);

        btnVaoBanNhap.setEnabled(false);
        btnVaoBanNhap.setText("Đang lưu...");

        authRepository.themThongBaoAdmin(tieuDe, noiDung, encodedRole).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(@NonNull Call<List<ThongBao>> call, @NonNull Response<List<ThongBao>> response) {
                if (!isAdded()) return;

                btnVaoBanNhap.setEnabled(true);
                btnVaoBanNhap.setText("Vào bản nháp");

                if (response.isSuccessful()) {
                    edtTieuDe.setText("");
                    edtNoiDung.setText("");
                    cbTatCa.setChecked(false);
                    cbAdmin.setChecked(false);
                    cbBacSi.setChecked(true);
                    cbLeTan.setChecked(false);

                    manHinhTao.setVisibility(View.GONE);
                    manHinhDanhSach.setVisibility(View.VISIBLE);
                    doiTab(true);
                } else {
                    Toast.makeText(requireContext(), layThongBaoLoi(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ThongBao>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnVaoBanNhap.setEnabled(true);
                btnVaoBanNhap.setText("Vào bản nháp");
                Toast.makeText(requireContext(), "Không thể kết nối máy chủ để lưu bản nháp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goiDuLieuRetrofit() {
        goiDuLieuRetrofit(true);
    }

    private void goiDuLieuRetrofit(boolean showLoading) {
        if (layoutDanhSach == null) {
            return;
        }

        if (showLoading) {
            tvLoading.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            layoutDanhSach.removeAllViews();
            datLaiCheDoChon();
        }

        authRepository.layDanhSachThongBaoAdmin().enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(@NonNull Call<List<ThongBao>> call, @NonNull Response<List<ThongBao>> response) {
                if (!isAdded() || layoutDanhSach == null) {
                    return;
                }

                tvLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    phanLoaiVaHienThi(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ThongBao>> call, @NonNull Throwable t) {
                if (!isAdded() || layoutDanhSach == null) {
                    return;
                }

                tvLoading.setVisibility(View.GONE);
            }
        });
    }

    private void phanLoaiVaHienThi(List<ThongBao> danhSach) {
        danhSachBanNhap.clear();
        danhSachDaGui.clear();

        if (danhSach != null) {
            for (ThongBao tb : danhSach) {
                if (tb.getVai_tro() != null && tb.getVai_tro().startsWith("DRAFT_")) {
                    danhSachBanNhap.add(tb);
                } else {
                    danhSachDaGui.add(tb);
                }
            }
        }

        Collections.reverse(danhSachBanNhap);
        Collections.reverse(danhSachDaGui);

        datLaiCheDoChon();
        capNhatGiaoDienTabHienTai();
    }

    private void capNhatGiaoDienTabHienTai() {
        if (layoutDanhSach == null) return;
        layoutDanhSach.removeAllViews();

        List<ThongBao> targetList = dangXemNhap ? danhSachBanNhap : danhSachDaGui;

        if (targetList.isEmpty()) {
            if (dangXemNhap) {
                hienThiRong("Chưa có bản nháp nào", "Nhấn + để tạo bản nháp mới");
            } else {
                hienThiRong("Chưa có thông báo nào", "Nhấn + để tạo thông báo mới");
            }
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        for (ThongBao thongBao : targetList) {
            layoutDanhSach.addView(taoCardThongBao(thongBao));
        }
    }

    private View taoCardThongBao(ThongBao thongBao) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(18));
        card.setCardElevation(dp(2));
        card.setStrokeWidth(0);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));

        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#14B8D4")));
        checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        listCheckBoxes.add(checkBox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Integer id = thongBao.getId();
            if (id == null) return;

            if (isChecked) {
                if (!selectedIds.contains(id)) selectedIds.add(id);
            } else {
                selectedIds.remove(id);
            }
        });

        LinearLayout contentColumn = new LinearLayout(requireContext());
        contentColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        contentParams.setMarginStart(dp(10));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(layChuoi(thongBao.getTieu_de(), "Không có tiêu đề"));
        tvTitle.setTextColor(Color.parseColor("#0D3F6E"));
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvContent = new TextView(requireContext());
        tvContent.setText(layChuoi(thongBao.getNoi_dung(), ""));
        tvContent.setTextColor(Color.parseColor("#64748B"));
        tvContent.setTextSize(14);
        tvContent.setMaxLines(2);
        tvContent.setPadding(0, dp(6), 0, 0);

        TextView tvRole = new TextView(requireContext());
        tvRole.setText("Gửi đến: " + hienThiVaiTro(thongBao.getVai_tro()));
        tvRole.setTextColor(Color.parseColor("#0D5FA8"));
        tvRole.setTextSize(12);
        tvRole.setTypeface(null, Typeface.BOLD);
        tvRole.setPadding(0, dp(8), 0, 0);

        contentColumn.addView(tvTitle);
        contentColumn.addView(tvContent);
        contentColumn.addView(tvRole);
        row.addView(checkBox);
        row.addView(contentColumn, contentParams);

        if (dangXemNhap) {
            MaterialButton btnChuyen = new MaterialButton(requireContext());
            btnChuyen.setText("Gửi");
            btnChuyen.setTextSize(12);
            btnChuyen.setTextColor(Color.WHITE);
            btnChuyen.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
            btnChuyen.setCornerRadius(dp(8));
            btnChuyen.setPadding(dp(8), dp(4), dp(8), dp(4));
            btnChuyen.setFocusable(true);
            btnChuyen.setClickable(true);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMarginStart(dp(8));
            btnParams.gravity = Gravity.CENTER_VERTICAL;

            row.addView(btnChuyen, btnParams);

            btnChuyen.setOnClickListener(v -> {
                chuyenNhapSangChinh(thongBao);
            });
        }

        card.addView(row);
        card.setOnClickListener(v -> {
            if (isSelectionMode) {
                checkBox.setChecked(!checkBox.isChecked());
            } else {
                hienThiChiTietThongBao(thongBao);
            }
        });
        return card;
    }

    private void hienThiChiTietThongBao(ThongBao thongBao) {
        if (!isAdded()) return;

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setView(taoCuaSoChiTietThongBaoDep(thongBao, dialog));
        dialog.setOnShowListener(dialogInterface -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setLayout(dp(330), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    private View taoCuaSoChiTietThongBao(ThongBao thongBao, AlertDialog dialog) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(22));
        card.setCardElevation(dp(8));
        card.setStrokeWidth(0);

        ScrollView scrollView = new ScrollView(requireContext());
        card.addView(scrollView, new ViewGroup.LayoutParams(
                dp(320),
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(22), dp(22), dp(18));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout titleRow = new LinearLayout(requireContext());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.TOP);
        content.addView(titleRow);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_notification);
        icon.setColorFilter(Color.parseColor("#0D5FA8"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        iconParams.setMargins(0, dp(2), dp(14), 0);
        titleRow.addView(icon, iconParams);

        LinearLayout titleColumn = new LinearLayout(requireContext());
        titleColumn.setOrientation(LinearLayout.VERTICAL);
        titleRow.addView(titleColumn, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView tvHeader = new TextView(requireContext());
        tvHeader.setText("Chi tiết thông báo");
        tvHeader.setTextColor(Color.parseColor("#64748B"));
        tvHeader.setTextSize(13);
        tvHeader.setTypeface(null, Typeface.BOLD);
        titleColumn.addView(tvHeader);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(layChuoi(thongBao.getTieu_de(), "Không có tiêu đề"));
        tvTitle.setTextColor(Color.parseColor("#0D3F6E"));
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, dp(4), 0, 0);
        titleColumn.addView(tvTitle);

        TextView tvRole = new TextView(requireContext());
        tvRole.setText("Gửi đến: " + hienThiVaiTro(thongBao.getVai_tro()));
        tvRole.setTextColor(Color.parseColor("#0D5FA8"));
        tvRole.setTextSize(13);
        tvRole.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams roleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        roleParams.setMargins(0, dp(20), 0, 0);
        content.addView(tvRole, roleParams);

        TextView tvContent = new TextView(requireContext());
        tvContent.setText(layChuoi(thongBao.getNoi_dung(), ""));
        tvContent.setTextColor(Color.parseColor("#1E293B"));
        tvContent.setTextSize(16);
        tvContent.setLineSpacing(dp(5), 1f);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dp(16), 0, 0);
        content.addView(tvContent, contentParams);

        TextView btnDong = new TextView(requireContext());
        btnDong.setText("Đóng");
        btnDong.setTextColor(Color.WHITE);
        btnDong.setTextSize(15);
        btnDong.setTypeface(null, Typeface.BOLD);
        btnDong.setGravity(Gravity.CENTER);
        btnDong.setBackgroundColor(Color.parseColor("#0D5FA8"));
        btnDong.setPadding(dp(18), dp(10), dp(18), dp(10));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        closeParams.gravity = Gravity.END;
        closeParams.setMargins(0, dp(24), 0, 0);
        content.addView(btnDong, closeParams);
        btnDong.setOnClickListener(v -> dialog.dismiss());

        return card;
    }

    private View taoCuaSoChiTietThongBaoDep(ThongBao thongBao, AlertDialog dialog) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(24));
        card.setCardElevation(dp(8));
        card.setStrokeWidth(0);

        LinearLayout dialogRoot = new LinearLayout(requireContext());
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        card.addView(dialogRoot, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(18), dp(20), dp(18));
        header.setBackground(taoNenHeaderDialog());
        dialogRoot.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ImageView headerIcon = new ImageView(requireContext());
        headerIcon.setImageResource(R.drawable.ic_notification_bell);
        headerIcon.setColorFilter(Color.WHITE);
        headerIcon.setPadding(dp(8), dp(8), dp(8), dp(8));
        headerIcon.setBackground(taoNenBoGoc(Color.parseColor("#2B7BBB"), dp(18)));
        LinearLayout.LayoutParams headerIconParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        headerIconParams.setMargins(0, 0, dp(14), 0);
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
        content.setPadding(dp(22), dp(20), dp(22), dp(18));
        content.setBackgroundColor(Color.WHITE);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(layChuoi(thongBao.getTieu_de(), "Không có tiêu đề"));
        tvTitle.setTextColor(Color.parseColor("#0D3F6E"));
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setLineSpacing(dp(3), 1f);
        content.addView(tvTitle);

        View line = new View(requireContext());
        line.setBackgroundColor(Color.parseColor("#E2E8F0"));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        lineParams.setMargins(0, dp(16), 0, dp(16));
        content.addView(line, lineParams);

        TextView tvRole = new TextView(requireContext());
        tvRole.setText("Gửi đến: " + hienThiVaiTro(thongBao.getVai_tro()));
        tvRole.setTextColor(Color.parseColor("#0D5FA8"));
        tvRole.setTextSize(13);
        tvRole.setTypeface(null, Typeface.BOLD);
        tvRole.setPadding(dp(12), dp(7), dp(12), dp(7));
        tvRole.setBackground(taoNenBoGoc(Color.parseColor("#E0F2FE"), dp(12)));
        content.addView(tvRole);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText("Nội dung");
        tvLabel.setTextColor(Color.parseColor("#0D5FA8"));
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, dp(18), 0, 0);
        content.addView(tvLabel, labelParams);

        TextView tvContent = new TextView(requireContext());
        tvContent.setText(layChuoi(thongBao.getNoi_dung(), ""));
        tvContent.setTextColor(Color.parseColor("#1E293B"));
        tvContent.setTextSize(16);
        tvContent.setLineSpacing(dp(5), 1f);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dp(8), 0, 0);
        content.addView(tvContent, contentParams);

        if (dangXemNhap) {
            LinearLayout buttonLayout = new LinearLayout(requireContext());
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonLayoutParams.setMargins(0, dp(24), 0, 0);

            TextView btnChuyen = new TextView(requireContext());
            btnChuyen.setText("Gửi thông báo");
            btnChuyen.setTextColor(Color.WHITE);
            btnChuyen.setTextSize(15);
            btnChuyen.setTypeface(null, Typeface.BOLD);
            btnChuyen.setGravity(Gravity.CENTER);
            btnChuyen.setBackground(taoNenBoGoc(Color.parseColor("#10B981"), dp(10)));
            LinearLayout.LayoutParams chuyenParams = new LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1.2f
            );
            chuyenParams.setMarginEnd(dp(8));
            buttonLayout.addView(btnChuyen, chuyenParams);
            btnChuyen.setOnClickListener(v -> {
                dialog.dismiss();
                chuyenNhapSangChinh(thongBao);
            });

            TextView btnClose = new TextView(requireContext());
            btnClose.setText("Đóng");
            btnClose.setTextColor(Color.WHITE);
            btnClose.setTextSize(15);
            btnClose.setTypeface(null, Typeface.BOLD);
            btnClose.setGravity(Gravity.CENTER);
            btnClose.setBackground(taoNenBoGoc(Color.parseColor("#64748B"), dp(10)));
            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1f
            );
            buttonLayout.addView(btnClose, closeParams);
            btnClose.setOnClickListener(v -> dialog.dismiss());

            content.addView(buttonLayout, buttonLayoutParams);
        } else {
            TextView btnDong = new TextView(requireContext());
            btnDong.setText("Đóng");
            btnDong.setTextColor(Color.WHITE);
            btnDong.setTextSize(15);
            btnDong.setTypeface(null, Typeface.BOLD);
            btnDong.setGravity(Gravity.CENTER);
            btnDong.setBackground(taoNenBoGoc(Color.parseColor("#0D5FA8"), dp(10)));
            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(46)
            );
            closeParams.setMargins(0, dp(24), 0, 0);
            content.addView(btnDong, closeParams);
            btnDong.setOnClickListener(v -> dialog.dismiss());
        }

        return card;
    }

    private GradientDrawable taoNenHeaderDialog() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#021B33"),
                        Color.parseColor("#08335E"),
                        Color.parseColor("#0D5FA8")
                }
        );
        drawable.setCornerRadii(new float[]{
                dp(24), dp(24),
                dp(24), dp(24),
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

    private void doiCheDoChon() {
        isSelectionMode = !isSelectionMode;
        tvChon.setText(isSelectionMode ? "Hủy" : "Chọn");

        for (CheckBox checkBox : listCheckBoxes) {
            checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            if (!isSelectionMode) {
                checkBox.setChecked(false);
            }
        }

        if (!isSelectionMode) {
            selectedIds.clear();
        }
    }

    private void datLaiCheDoChon() {
        isSelectionMode = false;
        selectedIds.clear();
        listCheckBoxes.clear();
        if (tvChon != null) {
            tvChon.setText("Chọn");
        }
    }

    private void thucHienXoaNhieu() {
        if (selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn thông báo cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = dangXemNhap ? "Xóa bản nháp" : "Xóa thông báo";
        String message = dangXemNhap ? "Bạn có chắc muốn xoá các bản nháp đã chọn ?" : "Bạn có chắc muốn xoá các thông báo đã chọn ?";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    final int[] count = {0};
                    final int total = selectedIds.size();

                    for (int id : new ArrayList<>(selectedIds)) {
                        authRepository.xoaThongBaoAdmin(id).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                count[0]++;
                                if (count[0] == total) {
                                    Toast.makeText(requireContext(), "Đã xóa các mục đã chọn", Toast.LENGTH_SHORT).show();
                                    goiDuLieuRetrofit(true);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                count[0]++;
                                if (count[0] == total) {
                                    Toast.makeText(requireContext(), "Đã xóa các mục đã chọn", Toast.LENGTH_SHORT).show();
                                    goiDuLieuRetrofit(true);
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void chuyenNhapSangChinh(ThongBao thongBao) {
        if (thongBao == null) return;

        String decodedRole = decodeDraftRole(thongBao.getVai_tro());

        authRepository.themThongBaoAdmin(
                thongBao.getTieu_de(),
                thongBao.getNoi_dung(),
                decodedRole
        ).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(@NonNull Call<List<ThongBao>> call, @NonNull Response<List<ThongBao>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    authRepository.xoaThongBaoAdmin(thongBao.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> deleteResponse)
                        {
                            if (!isAdded()) return;
                            doiTab(false);
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            doiTab(false);
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), layThongBaoLoi(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ThongBao>> call, @NonNull Throwable t)
            {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Không thể kết nối máy chủ để gửi thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiRong(String title, String subtitle) {
        layoutDanhSach.removeAllViews();
        datLaiCheDoChon();
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(layChuoi(subtitle, ""));
    }

    private void guiThongBao() {
        String tieuDe = edtTieuDe.getText().toString().trim();
        String noiDung = edtNoiDung.getText().toString().trim();

        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTatCa.isChecked() && !cbAdmin.isChecked() && !cbBacSi.isChecked() && !cbLeTan.isChecked()) {
            Toast.makeText(requireContext(), "Vui lòng chọn vai tro nhân thông báo", Toast.LENGTH_SHORT).show();
            return;
        }

        String vaiTroNhan = layVaiTroNhan();
        btnGuiThongBao.setEnabled(false);
        btnGuiThongBao.setText("Đang gửi...");

        authRepository.themThongBaoAdmin(tieuDe, noiDung, vaiTroNhan).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(@NonNull Call<List<ThongBao>> call, @NonNull Response<List<ThongBao>> response) {
                if (!isAdded()) {
                    return;
                }

                btnGuiThongBao.setEnabled(true);
                btnGuiThongBao.setText("Gửi thông báo");
                if (response.isSuccessful())
                {
                    edtTieuDe.setText("");
                    edtNoiDung.setText("");
                    hienThiManHinhDanhSach();
                } else {
                    Toast.makeText(requireContext(), layThongBaoLoi(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ThongBao>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }

                btnGuiThongBao.setEnabled(true);
                btnGuiThongBao.setText("Gửi thông báo");
            }
        });
    }

    private String layVaiTroNhan() {
        if (cbTatCa.isChecked()) {
            return "TAT_CA";
        }

        List<String> roles = new ArrayList<>();
        if (cbAdmin.isChecked()) roles.add(UserRole.ADMIN.name());
        if (cbBacSi.isChecked()) roles.add(UserRole.BAC_SI.name());
        if (cbLeTan.isChecked()) roles.add(UserRole.NHAN_VIEN.name());

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < roles.size(); i++) {
            if (i > 0) result.append(",");
            result.append(roles.get(i));
        }
        return result.toString();
    }

    private String hienThiVaiTro(String vaiTro) {
        String value = layChuoi(vaiTro, "TAT_CA");
        if (value.startsWith("DRAFT_")) {
            value = decodeDraftRole(value);
        }
        if ("TAT_CA".equals(value)) return "Tất cả";

        return value
                .replace(UserRole.ADMIN.name(), "Admin")
                .replace(UserRole.BAC_SI.name(), "Bác sĩ")
                .replace(UserRole.NHAN_VIEN.name(), "Nhân viên")
                .replace(",", ", ");
    }

    private String encodeDraftRole(String vaiTro) {
        if (vaiTro == null) return "DRAFT_T";
        if (vaiTro.equals("TAT_CA")) return "DRAFT_T";

        List<String> encoded = new ArrayList<>();
        if (vaiTro.contains(UserRole.ADMIN.name())) encoded.add("A");
        if (vaiTro.contains(UserRole.BAC_SI.name())) encoded.add("B");
        if (vaiTro.contains(UserRole.NHAN_VIEN.name())) encoded.add("N");

        StringBuilder sb = new StringBuilder("DRAFT_");
        for (int i = 0; i < encoded.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(encoded.get(i));
        }
        return sb.toString();
    }

    private String decodeDraftRole(String encodedVaiTro) {
        if (encodedVaiTro == null || !encodedVaiTro.startsWith("DRAFT_")) {
            return "TAT_CA";
        }

        String rolesPart = encodedVaiTro.substring(6); // Remove "DRAFT_"
        if (rolesPart.equals("T")) return "TAT_CA";

        List<String> decoded = new ArrayList<>();
        if (rolesPart.contains("A")) decoded.add(UserRole.ADMIN.name());
        if (rolesPart.contains("B")) decoded.add(UserRole.BAC_SI.name());
        if (rolesPart.contains("N")) decoded.add(UserRole.NHAN_VIEN.name());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < decoded.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(decoded.get(i));
        }
        return sb.toString();
    }

    private String layChuoi(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String layThongBaoLoi(Response<?> response) {
        String message = "Không gửi được thông báo. Mã lỗi: " + response.code();
        try {
            if (response.errorBody() != null) {
                message += " - " + response.errorBody().string();
            }
        } catch (IOException ignored) {
            return message;
        }
        return message;
    }
    
    private int dp(int value)
    {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
