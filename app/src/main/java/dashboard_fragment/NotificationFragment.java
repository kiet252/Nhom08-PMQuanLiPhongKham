package dashboard_fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.ThongBao;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment
{

    private LinearLayout layoutDanhSachThongBao;
    private FloatingActionButton btnThemThongBao;
    private AuthRepository authRepository;
    private int soLuongThongBao = 0;
    private TextView tvChon, tvXoa;
    private boolean isSelectionMode = false;
    private List<Integer> selectedIds = new ArrayList<>();
    private List<CheckBox> listCheckBoxes = new ArrayList<>();

    public NotificationFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(getString(R.string.abAIkey));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        btnThemThongBao = view.findViewById(R.id.btn_them_thong_bao);
        layoutDanhSachThongBao = view.findViewById(R.id.layout_danh_sach_thong_bao);

        tvChon = view.findViewById(R.id.tv_chon);
        tvXoa = view.findViewById(R.id.tv_xoa);

        //Xử lý nút chọn (Bật / Tắt)
        if (tvChon != null)
        {
            tvChon.setOnClickListener(v ->{
                isSelectionMode = !isSelectionMode;
                if (isSelectionMode)
                {
                    tvChon.setText("Hủy");
                    tvChon.setTextColor(Color.parseColor("#4A81B0"));
                    for (CheckBox cb : listCheckBoxes)
                    {
                        cb.setVisibility(View.VISIBLE);
                    }
                } else
                {
                    tvChon.setText("Chọn");
                    tvChon.setTextColor(Color.BLACK);
                    for (CheckBox cb : listCheckBoxes)
                    {
                        cb.setChecked(false);
                        cb.setVisibility(View.GONE);
                    }
                    selectedIds.clear();
                }
            });
        }
        // Nút Xóa
        if (tvXoa != null)
        {
            tvXoa.setOnClickListener(v -> thucHienXoaNhieu());
        }

        goiDuLieuRetrofit();

        if (btnThemThongBao != null) {
            btnThemThongBao.setOnClickListener(v -> hienThiHopThoaiThem());
        }
        return view;
    }
    private void goiDuLieuRetrofit()
    {
        authRepository.layDanhSachThongBao().enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    List<ThongBao> ds = response.body();
                    layoutDanhSachThongBao.removeAllViews();

                    listCheckBoxes.clear();
                    selectedIds.clear();
                    //Reset lại nút Chọn về mặc định
                    isSelectionMode = false;
                    if (tvChon != null)
                    {
                        tvChon.setText("Chọn");
                        tvChon.setTextColor(Color.BLACK);
                    }

                    Collections.reverse(ds);
                    soLuongThongBao = 0;
                    for (ThongBao tb : ds) {
                        themVaoKhungXam(tb.getId(), tb.getTieu_de(), tb.getNoi_dung());
                    }
                }
            }
            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t)
            {
            }
        });
    }
    private void thucHienXoaNhieu()
    {
        if (selectedIds.isEmpty())
        {
            return;
        }
        new AlertDialog.Builder(getContext())
                .setPositiveButton("Xóa", (dialog, which) ->
                {
                    final int[] count = {0};
                    final int total = selectedIds.size();

                    for (int id : selectedIds)
                    {
                        authRepository.xoaThongBaoAdmin(id).enqueue(new Callback<Void>()
                        {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response)
                            {
                                count[0]++;
                                if (count[0] == total)
                                {
                                    goiDuLieuRetrofit();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t)
                            {
                                count[0]++;
                                if (count[0] == total) goiDuLieuRetrofit();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void hienThiHopThoaiThem()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm thông báo mới");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(8));

        final EditText etTieuDe = new EditText(getContext());
        etTieuDe.setHint("Nhập tiêu đề...");
        layout.addView(etTieuDe);

        final EditText etNoiDung = new EditText(getContext());
        etNoiDung.setHint("Nhập nội dung chi tiết...");
        layout.addView(etNoiDung);

        builder.setView(layout);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String tieuDeStr = etTieuDe.getText().toString().trim();
            String noiDungStr = etNoiDung.getText().toString().trim();

            if (!tieuDeStr.isEmpty() && !noiDungStr.isEmpty())
            {
                dayDuLieuLenRetrofit(tieuDeStr, noiDungStr);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void dayDuLieuLenRetrofit(String tieuDe, String noiDung)
    {
        authRepository.themThongBao(tieuDe, noiDung).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response)
            {
                if (response.isSuccessful())
                {
                    goiDuLieuRetrofit();
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t)
            {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dpToPx(int dp)
    {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }

    private void themVaoKhungXam(Integer id, String tieuDe, String noiDung)
    {
        if (layoutDanhSachThongBao == null) return;

        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        listCheckBoxes.add(checkBox);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isChecked)
            {
                if (!selectedIds.contains(id)) selectedIds.add(id);
            } else
            {
                selectedIds.remove(Integer.valueOf(id));
            }
        });

        MaterialCardView cardMoi = new MaterialCardView(getContext());
        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCard.setMargins(0, 0, 0, dpToPx(16));
        cardMoi.setLayoutParams(paramsCard);
        cardMoi.setCardBackgroundColor(Color.parseColor("#F4F6FB"));
        cardMoi.setRadius(dpToPx(12));
        cardMoi.setCardElevation(0f);

        cardMoi.setOnClickListener(v ->
        {
            if (isSelectionMode)
            {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });

        LinearLayout lopNgang = new LinearLayout(getContext());
        lopNgang.setOrientation(LinearLayout.HORIZONTAL);
        lopNgang.setGravity(Gravity.CENTER_VERTICAL);
        lopNgang.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout lopDoc = new LinearLayout(getContext());
        lopDoc.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lopDocParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        lopDocParams.setMarginStart(dpToPx(12));
        lopDoc.setLayoutParams(lopDocParams);

        TextView tvTieuDe = new TextView(getContext());
        tvTieuDe.setText(tieuDe);
        tvTieuDe.setTextColor(Color.BLACK);
        tvTieuDe.setTextSize(18);
        tvTieuDe.setTypeface(null, Typeface.BOLD);

        TextView tvNoiDung = new TextView(getContext());
        tvNoiDung.setText(noiDung);
        tvNoiDung.setTextColor(Color.parseColor("#555555"));
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
}