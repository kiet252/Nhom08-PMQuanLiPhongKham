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
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationSettingsFragment_Admin#newInstance} factory method to.
 * create an instance of this fragment.
 */
public class NotificationSettingsFragment_Admin extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1= "param1";
    private static final String ARG_PARAM2= "param2";

    private String mParam1;
    private String mParam2;


    private LinearLayout layoutDanhSach;
    private FloatingActionButton btnThem;
    private AuthRepository authRepository;
    private int soLuong = 0;

    //Chức năng Chọn
    private TextView tvChon, tvXoa;
    private boolean isSelectionMode = false;
    private List<Integer> selectedIds = new ArrayList<>();
    private List<CheckBox> ListCheckBoxes = new ArrayList<>();
    //

    public NotificationSettingsFragment_Admin() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provied parameters
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportsFragment_Admin.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportsFragment_Admin newInstance(String param1, String param2)
    {
        ReportsFragment_Admin fragment = new ReportsFragment_Admin();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        authRepository = new AuthRepository(getString(R.string.abAIkey));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notification_settings_fragment_admin, container, false);

        layoutDanhSach = view.findViewById(R.id.layout_danh_sach_thong_bao_fragment_admin);
        btnThem = view.findViewById(R.id.btn_them_thong_bao_fragment_admin);
        tvChon = view.findViewById(R.id.tv_chon_fragment_admin);
        tvXoa = view.findViewById(R.id.tv_xoa_fragment_admin);

        //Xử lý nút chọn (Bật / Tắt)
        if (tvChon != null)
        {
            tvChon.setOnClickListener(v ->{
                isSelectionMode = !isSelectionMode;
                if (isSelectionMode)
                {
                    tvChon.setText("Hủy");
                    tvChon.setTextColor(Color.parseColor("#4A81B0"));
                    for (CheckBox cb : ListCheckBoxes)
                    {
                        cb.setVisibility(View.VISIBLE);
                    }
                } else
                    {
                        tvChon.setText("Chọn");
                        tvChon.setTextColor(Color.BLACK);
                        for (CheckBox cb : ListCheckBoxes)
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
        taiDuLieu();

        if (btnThem != null)
        {
            btnThem.setOnClickListener(v -> hienThiDialogThem());
        }
        return view;
    }
    private void taiDuLieu()
    {
        authRepository.layDanhSachThongBaoAdmin().enqueue(new Callback<List<ThongBao>>()
        {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    List<ThongBao> ds = response.body();
                    layoutDanhSach.removeAllViews();
                    ListCheckBoxes.clear();
                    selectedIds.clear();
                    //Reset lại nút Chọn về mặc định
                    isSelectionMode = false;
                    if (tvChon != null)
                    {
                        tvChon.setText("Chọn");
                        tvChon.setTextColor(Color.BLACK);
                    }

                    Collections.reverse(ds);
                    soLuong = 0;

                    for (ThongBao tb : response.body())
                    {
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
                        authRepository.xoaThongBao(id).enqueue(new Callback<Void>()
                        {
                           @Override
                           public void onResponse(Call<Void> call, Response<Void> response)
                           {
                               count[0]++;
                               if (count[0] == total)
                               {
                                   taiDuLieu();
                               }
                           }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t)
                            {
                                count[0]++;
                                if (count[0] == total) taiDuLieu();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void hienThiDialogThem()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tạo thông báo Admin");

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10));

        final EditText inputTieuDe = new EditText(getContext());
        inputTieuDe.setHint("Tiêu đề thông báo");
        container.addView(inputTieuDe);

        final EditText inputNoiDung = new EditText(getContext());
        inputNoiDung.setHint("Nội dung chi tiết");
        container.addView(inputNoiDung);

        builder.setView(container);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String t = inputTieuDe.getText().toString().trim();
            String n = inputNoiDung.getText().toString().trim();
            if (!t.isEmpty() && !n.isEmpty())
            {
                dayDuLieuLenRetrofitAdmin(t, n);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    private void dayDuLieuLenRetrofitAdmin(String tieuDe, String noiDung) {
        authRepository.themThongBaoAdmin(tieuDe, noiDung).enqueue(new Callback<List<ThongBao>>() {
            @Override
            public void onResponse(Call<List<ThongBao>> call, Response<List<ThongBao>> response) {
                if (response.isSuccessful())
                {
                    taiDuLieu();
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t)
            {

            }
        });
    }
    private int dpToPx(int dp)
    {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }
    private void themVaoKhungXam(Integer id, String tieuDe, String noiDung)
    {
        if (layoutDanhSach == null) return;

        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        ListCheckBoxes.add(checkBox);

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


        TextView tvTieuDe = new TextView(getContext());
        tvTieuDe.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvTieuDe.setText(tieuDe);
        tvTieuDe.setTextColor(Color.BLACK);
        tvTieuDe.setTextSize(18);
        tvTieuDe.setTypeface(null, Typeface.BOLD);

        TextView tvNoiDung = new TextView(getContext());
        tvNoiDung.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvNoiDung.setText(noiDung);
        tvNoiDung.setTextColor(Color.parseColor("#555555"));
        tvNoiDung.setTextSize(14);
        tvNoiDung.setPadding(0, dpToPx(8), 0, 0);

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

        lopDoc.addView(tvTieuDe);
        lopDoc.addView(tvNoiDung);

        lopNgang.addView(checkBox);
        lopNgang.addView(lopDoc);
        cardMoi.addView(lopNgang);

        layoutDanhSach.addView(cardMoi);
        soLuong++;
    }
}