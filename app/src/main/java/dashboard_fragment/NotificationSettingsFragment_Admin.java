package dashboard_fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.LinearLayout;
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
    private AuthRepository authRepository;
    private boolean dangXemNhap = false;
    private boolean isSelectionMode = false;
    private final List<Integer> selectedIds = new ArrayList<>();
    private final List<CheckBox> listCheckBoxes = new ArrayList<>();

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
        super.onDestroyView();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
        manHinhDanhSach = null;
        manHinhTao = null;
        layoutDanhSach = null;
        layoutEmpty = null;
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
        tvChon.setVisibility(xemNhap ? View.GONE : View.VISIBLE);
        tvXoa.setVisibility(xemNhap ? View.GONE : View.VISIBLE);

        if (xemNhap)
        {
            datLaiCheDoChon();
            hienThiRong("Chưa có bản nháp nào", "Nhấn + để tạo thông báo mới");
        } else
        {
            goiDuLieuRetrofit();
        }
    }

    private void goiDuLieuRetrofit() {
        goiDuLieuRetrofit(true);
    }

    private void goiDuLieuRetrofit(boolean showLoading)
    {
        if (dangXemNhap || layoutDanhSach == null) {
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
                    hienThiDanhSach(response.body());
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

    private void hienThiDanhSach(List<ThongBao> danhSach)
    {
        layoutDanhSach.removeAllViews();
        datLaiCheDoChon();

        if (danhSach == null || danhSach.isEmpty())
        {
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        Collections.reverse(danhSach);
        for (ThongBao thongBao : danhSach) {
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
        tvRole.setText("Gui den: " + hienThiVaiTro(thongBao.getVai_tro()));
        tvRole.setTextColor(Color.parseColor("#0D5FA8"));
        tvRole.setTextSize(12);
        tvRole.setTypeface(null, Typeface.BOLD);
        tvRole.setPadding(0, dp(8), 0, 0);

        contentColumn.addView(tvTitle);
        contentColumn.addView(tvContent);
        contentColumn.addView(tvRole);
        row.addView(checkBox);
        row.addView(contentColumn, contentParams);
        card.addView(row);
        card.setOnClickListener(v -> {
            if (isSelectionMode) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
        return card;
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

        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xoá các thông báo đã chọn ?")
                .setPositiveButton("Xoa", (dialog, which) -> {
                    final int[] count = {0};
                    final int total = selectedIds.size();

                    for (int id : new ArrayList<>(selectedIds)) {
                        authRepository.xoaThongBaoAdmin(id).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                count[0]++;
                                if (count[0] == total) {
                                    goiDuLieuRetrofit();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                count[0]++;
                                if (count[0] == total) {
                                    goiDuLieuRetrofit();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
        btnGuiThongBao.setText("Dang gui...");

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
        if ("TAT_CA".equals(value)) return "Tat ca";

        return value
                .replace(UserRole.ADMIN.name(), "Admin")
                .replace(UserRole.BAC_SI.name(), "Bac si")
                .replace(UserRole.NHAN_VIEN.name(), "Nhan vien")
                .replace(",", ", ");
    }

    private String layChuoi(String value, String fallback) {
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
