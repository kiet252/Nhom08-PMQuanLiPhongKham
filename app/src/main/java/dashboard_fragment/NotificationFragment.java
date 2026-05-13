package dashboard_fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.ThongBao;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private LinearLayout layoutDanhSachThongBao;
    private FloatingActionButton btnThemThongBao;
    private FrameLayout rootContainer;
    private View listView;
    private AuthRepository authRepository;
    private int soLuongThongBao = 0;
    private TextView tvChon, tvXoa;
    private boolean isSelectionMode = false;
    private final List<Integer> selectedIds = new ArrayList<>();
    private final List<CheckBox> listCheckBoxes = new ArrayList<>();

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
        btnThemThongBao = view.findViewById(R.id.btn_them_thong_bao);
        layoutDanhSachThongBao = view.findViewById(R.id.layout_danh_sach_thong_bao);
        tvChon = view.findViewById(R.id.tv_chon);
        tvXoa = view.findViewById(R.id.tv_xoa);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String roleSaved = prefs.getString("ROLE", UserRole.NHAN_VIEN.name());

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(roleSaved);
        } catch (Exception e) {
            userRole = UserRole.NHAN_VIEN;
        }

        if (userRole == UserRole.ADMIN) {
            btnThemThongBao.setVisibility(View.VISIBLE);
            tvChon.setVisibility(View.VISIBLE);
            tvXoa.setVisibility(View.VISIBLE);
        } else {
            btnThemThongBao.setVisibility(View.GONE);
            tvChon.setVisibility(View.GONE);
            tvXoa.setVisibility(View.GONE);
        }

        if (tvChon != null) {
            tvChon.setOnClickListener(v -> {
                isSelectionMode = !isSelectionMode;
                if (isSelectionMode) {
                    tvChon.setText("Hủy");
                    tvChon.setTextColor(Color.parseColor("#0D5FA8"));
                    for (CheckBox cb : listCheckBoxes) {
                        cb.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvChon.setText("Chọn");
                    tvChon.setTextColor(Color.parseColor("#0D5FA8"));
                    for (CheckBox cb : listCheckBoxes) {
                        cb.setChecked(false);
                        cb.setVisibility(View.GONE);
                    }
                    selectedIds.clear();
                }
            });
        }

        if (tvXoa != null) {
            tvXoa.setOnClickListener(v -> thucHienXoaNhieu());
        }

        goiDuLieuRetrofit();

        if (btnThemThongBao != null) {
            btnThemThongBao.setOnClickListener(v -> hienThiHopThoaiThem());
        }
    }

    private void goiDuLieuRetrofit() {
        authRepository.layDanhSachThongBao().enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ThongBao> ds = response.body();
                    layoutDanhSachThongBao.removeAllViews();

                    listCheckBoxes.clear();
                    selectedIds.clear();
                    isSelectionMode = false;

                    if (tvChon != null) {
                        tvChon.setText("Chọn");
                        tvChon.setTextColor(Color.parseColor("#0D5FA8"));
                    }

                    Collections.reverse(ds);
                    soLuongThongBao = 0;
                    for (ThongBao tb : ds) {
                        themVaoKhungXam(tb.getId(), tb.getTieu_de(), tb.getNoi_dung());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải thông báo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void thucHienXoaNhieu() {
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn thông báo cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa các thông báo đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    final int[] count = {0};
                    final int total = selectedIds.size();

                    for (int id : selectedIds) {
                        authRepository.xoaThongBaoAdmin(id).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                count[0]++;
                                if (count[0] == total) {
                                    goiDuLieuRetrofit();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
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

    private void hienThiHopThoaiThem() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));
        layout.setBackground(taoNenBoGoc("#FFFFFF", 24, "#FFFFFF", 0));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText("Thêm thông báo mới");
        tvTitle.setTextColor(Color.parseColor("#0D3F6E"));
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        layout.addView(tvTitle);

        TextView tvSubtitle = new TextView(requireContext());
        tvSubtitle.setText("Nhập tiêu đề và nội dung để gửi thông báo.");
        tvSubtitle.setTextColor(Color.parseColor("#64748B"));
        tvSubtitle.setTextSize(14);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dpToPx(6), 0, dpToPx(18));
        tvSubtitle.setLayoutParams(subtitleParams);
        layout.addView(tvSubtitle);

        final EditText etTieuDe = new EditText(requireContext());
        etTieuDe.setHint("Nhập tiêu đề");
        etTieuDe.setSingleLine(true);
        etTieuDe.setTextColor(Color.parseColor("#1E293B"));
        etTieuDe.setHintTextColor(Color.parseColor("#94A3B8"));
        etTieuDe.setTextSize(15);
        etTieuDe.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        etTieuDe.setMinHeight(dpToPx(52));
        etTieuDe.setBackground(taoNenBoGoc("#F8FAFC", 12, "#D6E4F0", 1));
        layout.addView(etTieuDe, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        ));

        final EditText etNoiDung = new EditText(requireContext());
        etNoiDung.setHint("Nhập nội dung chi tiết");
        etNoiDung.setTextColor(Color.parseColor("#1E293B"));
        etNoiDung.setHintTextColor(Color.parseColor("#94A3B8"));
        etNoiDung.setTextSize(15);
        etNoiDung.setGravity(Gravity.TOP | Gravity.START);
        etNoiDung.setMinLines(4);
        etNoiDung.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etNoiDung.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
        etNoiDung.setBackground(taoNenBoGoc("#F8FAFC", 12, "#D6E4F0", 1));

        LinearLayout.LayoutParams noiDungParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(120)
        );
        noiDungParams.setMargins(0, dpToPx(12), 0, 0);
        layout.addView(etNoiDung, noiDungParams);

        LinearLayout actionLayout = new LinearLayout(requireContext());
        actionLayout.setGravity(Gravity.END);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionParams.setMargins(0, dpToPx(18), 0, 0);
        actionLayout.setLayoutParams(actionParams);

        TextView btnHuy = taoNutHopThoai("Hủy", "#EAF6FF", "#0D5FA8");
        TextView btnThem = taoNutHopThoai("Thêm", "#0D3F6E", "#FFFFFF");

        LinearLayout.LayoutParams btnThemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(48)
        );
        btnThemParams.setMargins(dpToPx(10), 0, 0, 0);

        actionLayout.addView(btnHuy);
        actionLayout.addView(btnThem, btnThemParams);
        layout.addView(actionLayout);

        btnHuy.setOnClickListener(v -> dialog.dismiss());
        btnThem.setOnClickListener(v -> {
            String tieuDeStr = etTieuDe.getText().toString().trim();
            String noiDungStr = etNoiDung.getText().toString().trim();

            if (!tieuDeStr.isEmpty() && !noiDungStr.isEmpty()) {
                dayDuLieuLenRetrofit(tieuDeStr, noiDungStr);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setView(layout);
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });
        dialog.show();
    }

    private void dayDuLieuLenRetrofit(String tieuDe, String noiDung) {
        authRepository.themThongBao(tieuDe, noiDung).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response) {
                if (response.isSuccessful()) {
                    goiDuLieuRetrofit();
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TextView taoNutHopThoai(String text, String backgroundColor, String textColor)
    {
        TextView button = new TextView(requireContext());
        button.setText(text);
        button.setTextColor(Color.parseColor(textColor));
        button.setTextSize(14);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(dpToPx(86));
        button.setMinHeight(dpToPx(48));
        button.setPadding(dpToPx(16), 0, dpToPx(16), 0);
        button.setClickable(true);
        button.setFocusable(true);
        button.setBackground(taoNenBoGoc(backgroundColor, 12, backgroundColor, 0));
        return button;
    }

    private GradientDrawable taoNenBoGoc(String solidColor, int radiusDp, String strokeColor, int strokeDp)
    {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(solidColor));
        drawable.setCornerRadius(dpToPx(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dpToPx(strokeDp), Color.parseColor(strokeColor));
        }
        return drawable;
    }

    private int dpToPx(int dp) {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }

    private void themVaoKhungXam(Integer id, String tieuDe, String noiDung) {
        if (layoutDanhSachThongBao == null) return;

        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#14B8D4")));
        checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        listCheckBoxes.add(checkBox);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedIds.contains(id)) selectedIds.add(id);
            } else {
                selectedIds.remove(Integer.valueOf(id));
            }
        });

        MaterialCardView cardMoi = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCard.setMargins(0, 0, 0, dpToPx(16));
        cardMoi.setLayoutParams(paramsCard);
        cardMoi.setCardBackgroundColor(Color.WHITE);
        cardMoi.setRadius(dpToPx(18));
        cardMoi.setCardElevation(dpToPx(2));

        cardMoi.setOnClickListener(v -> {
            if (isSelectionMode) {
                checkBox.setChecked(!checkBox.isChecked());
            } else {
                hienThiChiTietThongBao(tieuDe, noiDung);
            }
        });

        LinearLayout lopNgang = new LinearLayout(requireContext());
        lopNgang.setOrientation(LinearLayout.HORIZONTAL);
        lopNgang.setGravity(Gravity.CENTER_VERTICAL);
        lopNgang.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout lopDoc = new LinearLayout(requireContext());
        lopDoc.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lopDocParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        lopDocParams.setMarginStart(dpToPx(12));
        lopDoc.setLayoutParams(lopDocParams);

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
        lopNgang.addView(checkBox);
        lopNgang.addView(lopDoc);
        cardMoi.addView(lopNgang);

        layoutDanhSachThongBao.addView(cardMoi);
        soLuongThongBao++;
    }

    private void hienThiChiTietThongBao(String tieuDe, String noiDung) {
        if (rootContainer == null) return;

        rootContainer.removeAllViews();
        rootContainer.addView(taoManHinhChiTietThongBao(tieuDe, noiDung));
        if (btnThemThongBao != null) {
            btnThemThongBao.setVisibility(View.GONE);
        }
    }

    private View taoManHinhChiTietThongBao(String tieuDe, String noiDung) {
        FrameLayout root = new FrameLayout(requireContext());
        root.setBackgroundColor(Color.parseColor("#F0F7FF"));

        LinearLayout page = new LinearLayout(requireContext());
        page.setOrientation(LinearLayout.VERTICAL);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        FrameLayout header = new FrameLayout(requireContext());
        header.setBackgroundResource(R.drawable.bg_gradient_dark_blue);
        page.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(170)
        ));

        ImageButton btnBack = new ImageButton(requireContext());
        btnBack.setImageResource(R.drawable.ic_back_white);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setContentDescription("Quay lại");
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(dpToPx(48), dpToPx(48));
        backParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        backParams.setMargins(dpToPx(20), 0, 0, 0);
        header.addView(btnBack, backParams);

        TextView tvHeaderTitle = new TextView(requireContext());
        tvHeaderTitle.setText("Chi tiết thông báo");
        tvHeaderTitle.setTextColor(Color.WHITE);
        tvHeaderTitle.setTextSize(26);
        tvHeaderTitle.setTypeface(null, Typeface.BOLD);
        tvHeaderTitle.setGravity(Gravity.CENTER);
        header.addView(tvHeaderTitle, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        CardView contentCard = new CardView(requireContext());
        contentCard.setCardBackgroundColor(Color.WHITE);
        contentCard.setRadius(dpToPx(28));
        contentCard.setCardElevation(0);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        cardParams.setMargins(0, -dpToPx(28), 0, 0);
        page.addView(contentCard, cardParams);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setFillViewport(true);
        contentCard.addView(scrollView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(28), dpToPx(36), dpToPx(28), dpToPx(36));
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
        icon.setColorFilter(Color.parseColor("#4F5BEA"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(34), dpToPx(34));
        iconParams.setMargins(0, dpToPx(2), dpToPx(16), 0);
        titleRow.addView(icon, iconParams);

        LinearLayout titleColumn = new LinearLayout(requireContext());
        titleColumn.setOrientation(LinearLayout.VERTICAL);
        titleRow.addView(titleColumn, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(tieuDe);
        tvTitle.setTextColor(Color.parseColor("#4F5BEA"));
        tvTitle.setTextSize(21);
        tvTitle.setTypeface(null, Typeface.BOLD);
        titleColumn.addView(tvTitle);

        TextView tvSource = new TextView(requireContext());
        tvSource.setTextColor(Color.parseColor("#1E293B"));
        tvSource.setTextSize(15);
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sourceParams.setMargins(0, dpToPx(4), 0, 0);
        titleColumn.addView(tvSource, sourceParams);

        TextView tvContent = new TextView(requireContext());
        tvContent.setText(noiDung);
        tvContent.setTextColor(Color.parseColor("#1E293B"));
        tvContent.setTextSize(17);
        tvContent.setLineSpacing(dpToPx(5), 1f);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dpToPx(24), 0, 0);
        content.addView(tvContent, contentParams);

        btnBack.setOnClickListener(v -> quayLaiDanhSachThongBao());
        return root;
    }

    private void quayLaiDanhSachThongBao()
    {
        if (rootContainer == null || listView == null) return;

        rootContainer.removeAllViews();
        rootContainer.addView(listView);
        if (btnThemThongBao != null) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String roleSaved = prefs.getString("ROLE", UserRole.NHAN_VIEN.name());
            btnThemThongBao.setVisibility(UserRole.ADMIN.name().equals(roleSaved) ? View.VISIBLE : View.GONE);
        }
    }
}
