package dashboard_fragment;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.ThongBao;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private int idThongBaoDuocChon = -1;
    private View viewDangDuocChon = null;
    private TextView tvChon, tvXoa;

    public NotificationSettingsFragment_Admin() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provied parameters
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportsFragment_Admin.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportsFragment_Admin newInstance(String param1, String param2) {
        ReportsFragment_Admin fragment = new ReportsFragment_Admin();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        if (layoutDanhSach == null) {
            Toast.makeText(getContext(), "LỖI: Chưa gắn ID layout_danh_sach_thong_bao vào file XML!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Đã túm được khung xám! Đang kéo mây...", Toast.LENGTH_SHORT).show();
        }
        taiDuLieu();

        if (btnThem != null) {
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
                    Toast.makeText(getContext(), "THÀNH CÔNG: Tải được " + ds.size() + " thông báo!", Toast.LENGTH_LONG).show();
                    layoutDanhSach.removeAllViews();
                    Collections.reverse(ds);
                    soLuong = 0;

                    for (ThongBao tb : response.body()) {
                        themVaoKhungXam(tb.getTieu_de(), tb.getNoi_dung());
                    }
                } else {
                    Toast.makeText(getContext(), "LỖI SERVER: Không thể tải dữ liệu", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void hienThiDialogThem() {
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
        builder.setPositiveButton("Đăng bài", (dialog, which) -> {
            String t = inputTieuDe.getText().toString().trim();
            String n = inputNoiDung.getText().toString().trim();
            if (!t.isEmpty() && !n.isEmpty())
            {
                dayDuLieuLenRetrofitAdmin(t, n);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
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
                    if (getContext() != null)
                    {
                        Toast.makeText(getContext(), "Đã đăng thông báo thành công!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ThongBao>> call, Throwable t)
            {
                if (getContext() != null)
                {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void thucHienXoa(int id) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    authRepository.xoaThongBaoAdmin(id).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                                idThongBaoDuocChon = -1;
                                taiDuLieu(); // Tải lại danh sách
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi xóa dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private int dpToPx(int dp)
    {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }
    private void themVaoKhungXam(String tieuDe, String noiDung) {
        if (layoutDanhSach == null) return;

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

        LinearLayout lopNgang = new LinearLayout(getContext());
        lopNgang.setOrientation(LinearLayout.HORIZONTAL);
        lopNgang.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout lopDoc = new LinearLayout(getContext());
        lopDoc.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lopDocParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lopDocParams.setMarginStart(dpToPx(12));
        lopDoc.setLayoutParams(lopDocParams);

        lopDoc.addView(tvTieuDe);
        lopDoc.addView(tvNoiDung);
        lopNgang.addView(lopDoc);
        cardMoi.addView(lopNgang);

        layoutDanhSach.addView(cardMoi);
        soLuong++;
    }
}