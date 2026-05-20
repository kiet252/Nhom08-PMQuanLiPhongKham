package dashboard_fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private LinearLayout layoutDanhSachThongBao;
    private FrameLayout rootContainer;
    private View listView;
    private AuthRepository authRepository;
    private UserRole userRole = UserRole.NHAN_VIEN;

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
                    for (ThongBao tb : ds) {
                        themVaoKhungXam(tb.getTieu_de(), tb.getNoi_dung());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Loi tai thong bao: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }

    private void themVaoKhungXam(String tieuDe, String noiDung) {
        if (layoutDanhSachThongBao == null) return;

        MaterialCardView cardMoi = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCard.setMargins(0, 0, 0, dpToPx(16));
        cardMoi.setLayoutParams(paramsCard);
        cardMoi.setCardBackgroundColor(Color.WHITE);
        cardMoi.setRadius(dpToPx(18));
        cardMoi.setCardElevation(dpToPx(2));
        cardMoi.setOnClickListener(v -> hienThiChiTietThongBao(tieuDe, noiDung));

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
        lopNgang.addView(lopDoc);
        cardMoi.addView(lopNgang);

        layoutDanhSachThongBao.addView(cardMoi);
    }

    private void hienThiChiTietThongBao(String tieuDe, String noiDung) {
        if (rootContainer == null) return;

        rootContainer.removeAllViews();
        rootContainer.addView(taoManHinhChiTietThongBao(tieuDe, noiDung));
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
        btnBack.setContentDescription("Quay lai");
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(dpToPx(48), dpToPx(48));
        backParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        backParams.setMargins(dpToPx(20), 0, 0, 0);
        header.addView(btnBack, backParams);

        TextView tvHeaderTitle = new TextView(requireContext());
        tvHeaderTitle.setText("Chi tiet thong bao");
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

    private void quayLaiDanhSachThongBao() {
        if (rootContainer == null || listView == null) return;

        rootContainer.removeAllViews();
        rootContainer.addView(listView);
    }
}
