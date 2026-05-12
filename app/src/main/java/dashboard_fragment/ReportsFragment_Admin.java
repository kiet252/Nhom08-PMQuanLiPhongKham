package dashboard_fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.ContextThemeWrapper;

//Thêm chức năng Tìm Kiếm
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

import com.example.nhom08_quanlyphongkham.R;

//List
import java.util.ArrayList;

//Calendar
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportsFragment_Admin#newInstance} factory method to.
 * create an instance of this fragment.
 */
public class ReportsFragment_Admin extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1= "param1";
    private static final String ARG_PARAM2= "param2";

    private String mParam1;
    private String mParam2;

    public ReportsFragment_Admin() {

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
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) 
    {
        //1. Lưu giao diện vào biến "view" thay vì return trực tiếp
        View view = inflater.inflate(R.layout.activity_reports_fragment_admin, container, false);
        //2. Tìm và ánh xạ các thành phần giao diện
        EditText etMaDonKham = view.findViewById(R.id.et_ma_don_kham);
        EditText etTenBN = view.findViewById(R.id.et_ten_benh_nhan);
        TextView tvNgayBD = view.findViewById(R.id.tv_tu_ngay);
        TextView tvNgayKT = view.findViewById(R.id.tv_den_ngay);
        Button btnTimKiem = view.findViewById(R.id.btn_tim_kiem);
        TextView tvEmptyState = view.findViewById(R.id.tv_empty_state);
        RecyclerView rvDanhSach = view.findViewById(R.id.rv_danh_sach_doanh_thu);
        TextView tvTongSoHoaDon = view.findViewById(R.id.tv_tong_so_hoa_don);
        TextView tvTongDoanhThu = view.findViewById(R.id.tv_tong_doanh_thu);


        capNhatTongKet(new ArrayList<>(), tvTongSoHoaDon, tvTongDoanhThu);
        //3. Thêm sự kiện click bật lịch
        tvNgayBD.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hienThiLich(tvNgayBD);
            }
        });
        tvNgayKT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hienThiLich(tvNgayKT);
            }
        });
        //4. Thêm sự kiện click cho nút tìm kiếm và thuật toán
        if (btnTimKiem != null)
        {
            //Kiểm tra null để tránh lỗi crash app nếu chưa có nút trên giao diện
            btnTimKiem.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String maDonKhamCanTim = etMaDonKham.getText().toString().trim();
                    String tenBNCanTim = etTenBN.getText().toString().trim();
                    String ngayBDCanTim = tvNgayBD.getText().toString().trim();
                    String ngayKTCanTim = tvNgayKT.getText().toString().trim();

                    String regex = "^[a-zA-ZÀ-ỹ\\s]+$";
                    //KIỂM TRA INPUT.
                    //1. Kiểm tra input của các ô, cần input ít nhất 1 ô.
                    if (maDonKhamCanTim.isEmpty() && tenBNCanTim.isEmpty() &&
                        (ngayBDCanTim.equals("Ngày bắt đầu") || ngayBDCanTim.isEmpty()) &&
                        (ngayKTCanTim.equals("Ngày kết thúc") || ngayKTCanTim.isEmpty()))
                    {
                        Toast.makeText(getContext(), "Vui lòng nhập thông tin cần tìm!", Toast.LENGTH_LONG).show();
                        etMaDonKham.requestFocus();
                        return;
                    }
                    //2. Kiểm tra input ô Đơn Khám không được có khoảng trắng.
                    if (maDonKhamCanTim.contains(" "))
                    {
                        Toast.makeText(getContext(), "Mã hóa đơn không được chứa khoảng trắng!", Toast.LENGTH_LONG).show();
                        etMaDonKham.requestFocus();
                        return;
                    }
                    //3. Kiểm tra input ô Tên BN.
                    if (!tenBNCanTim.isEmpty())
                    {
                        if (!tenBNCanTim.matches(regex))
                        {
                            Toast.makeText(getContext(), "Tên bệnh nhân không được chứa số hoặc ký tự đặc biệt!", Toast.LENGTH_LONG).show();
                            etTenBN.requestFocus();
                            return;
                        }
                    }
                    //4. Kiểm tra input "Từ ngày" và "Đến ngày"
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date dateNgayBD = null;
                    Date dateNgayKT = null;
                    try
                    {
                        if (!ngayBDCanTim.equals("Ngày bắt đầu") && !ngayBDCanTim.isEmpty())
                        {
                            dateNgayBD = sdf.parse(ngayBDCanTim);
                        }
                        if (!ngayKTCanTim.equals("Ngày kết thúc") && !ngayKTCanTim.isEmpty())
                        {
                            dateNgayKT = sdf.parse(ngayKTCanTim);
                        }
                        if (dateNgayBD != null && dateNgayKT != null && dateNgayBD.after(dateNgayKT))
                        {
                            Toast.makeText(getContext(), "Ngày bắt đầu không được lớn hơn ngày kết thúc!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (Exception e)
                    {
                        Toast.makeText(getContext(), "Định dạng ngày không hợp lệ!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    boolean found = false;
                    // TODO: Thêm code vòng lặp tìm kiếm
                    // THUẬT TOÁN TÌM KIẾM.
                    List<DonKham> danhSachTatCa = layDanhSachDonKham();
                    List<DonKham> danhSachKetQua = new ArrayList<>();
                    // 1. Ánh xạ thành chữ thường để dễ so sánh
                    String tukhoaMaDK = etMaDonKham.getText().toString().trim().toLowerCase();
                    String tukhoaTenBN = etTenBN.getText().toString().trim().toLowerCase();

                    for (DonKham dk : danhSachTatCa)
                    {
                        String maDonKhamGoc = dk.getMaDonKham().toLowerCase();
                        String maTenBNGoc = dk.getTenBN().toLowerCase();
                        Date ngayKhamGoc = null;
                        try {
                            ngayKhamGoc = sdf.parse(dk.getNgayKham());
                        } catch (Exception ignored) {
                        }

                        boolean dungMa = tukhoaMaDK.isEmpty() || maDonKhamGoc.contains(tukhoaMaDK);
                        boolean dungTen = tukhoaTenBN.isEmpty() || maTenBNGoc.contains(tukhoaTenBN);
                        boolean dungNgayBatDau = dateNgayBD == null || (ngayKhamGoc != null && !ngayKhamGoc.before(dateNgayBD));
                        boolean dungNgayKetThuc = dateNgayKT == null || (ngayKhamGoc != null && !ngayKhamGoc.after(dateNgayKT));

                        if (dungMa && dungTen && dungNgayBatDau && dungNgayKetThuc)
                        {
                            danhSachKetQua.add(dk);
                            found = true;
                        }
                    }

                    capNhatTongKet(danhSachKetQua, tvTongSoHoaDon, tvTongDoanhThu);
                    //Xử lí hiển thị sau khi tim kiếm
                    if (!found)
                    {
                        Toast.makeText(getContext(), "Không tìm thấy hóa đơn có thông tin này!", Toast.LENGTH_SHORT).show();
                        if (rvDanhSach != null) rvDanhSach.setVisibility(View.GONE);
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                        if (rvDanhSach != null) rvDanhSach.setVisibility(View.VISIBLE);
                    }

                }
            });
        }
        return view;
    }
    public class DonKham
    {
        private String MaDonKham;
        private String TenBN;
        private String NgayKham;
        private long TongTien;

        public DonKham (String maHoaDon, String tenBenhNhan, String ngayKham, long tongTien)
        {
            this.MaDonKham = maHoaDon;
            this.TenBN = tenBenhNhan;
            this.NgayKham = ngayKham;
            this.TongTien = tongTien;
        }

        public String getMaDonKham()
        {
            return MaDonKham;
        }

        public String getTenBN()
        {
            return TenBN;
        }
        public String getNgayKham() {return NgayKham;}
        public long getTongTien() { return TongTien; }
    }

    private void capNhatTongKet(List<DonKham> danhSach, TextView tvTongSoHoaDon, TextView tvTongDoanhThu)
    {
        int soHoaDon = danhSach == null ? 0 : danhSach.size();
        long tongDoanhThu = 0;

        if (danhSach != null)
        {
            for (DonKham donKham : danhSach)
            {
                tongDoanhThu += donKham.getTongTien();
            }
        }

        if (tvTongSoHoaDon != null)
        {
            tvTongSoHoaDon.setText(String.valueOf(soHoaDon));
        }

        if (tvTongDoanhThu != null)
        {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTongDoanhThu.setText(formatter.format(tongDoanhThu) + " đ");
        }
    }

    private List<DonKham> layDanhSachDonKham()
    {
        List<DonKham> danhSach = new ArrayList<>();

        // TODO: Thay danh sách mẫu này bằng dữ liệu lấy từ API/database khi có nguồn dữ liệu thật.
        danhSach.add(new DonKham("DK001", "Nguyen Van A", "10/05/2026", 250000));
        danhSach.add(new DonKham("DK002", "Tran Thi B", "11/05/2026", 320000));
        danhSach.add(new DonKham("DK003", "Le Van C", "11/05/2026", 180000));

        return danhSach;
    }
    private void hienThiLich(TextView tv)
    {
        Context context = getContext();
        if (context == null) return;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);

        String ngayDangChon = tv.getText().toString().trim();
        try
        {
            if (!ngayDangChon.isEmpty())
            {
                Date date = sdf.parse(ngayDangChon);
                if (date != null)
                {
                    calendar.setTime(date);
                }
            }
        } catch (Exception ignored) {
        }

        int nam = calendar.get(Calendar.YEAR);
        int thang = calendar.get(Calendar.MONTH);
        int ngay = calendar.get(Calendar.DAY_OF_MONTH);

        AlertDialog dialog = new AlertDialog.Builder(context).create();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(context, 24), dp(context, 22), dp(context, 24), dp(context, 18));
        layout.setBackground(taoNenBoGoc(context, "#FFFFFF", 24, "#FFFFFF", 0));

        TextView tvTieuDe = new TextView(context);
        tvTieuDe.setText("Chọn ngày");
        tvTieuDe.setTextColor(Color.parseColor("#0D3F6E"));
        tvTieuDe.setTextSize(20);
        tvTieuDe.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        layout.addView(tvTieuDe);

        TextView tvMoTa = new TextView(context);
        tvMoTa.setText("Chọn ngày theo định dạng dd/MM/yyyy.");
        tvMoTa.setTextColor(Color.parseColor("#64748B"));
        tvMoTa.setTextSize(14);
        LinearLayout.LayoutParams moTaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        moTaParams.setMargins(0, dp(context, 6), 0, dp(context, 18));
        tvMoTa.setLayoutParams(moTaParams);
        layout.addView(tvMoTa);

        TextView tvNgayXemTruoc = new TextView(context);
        tvNgayXemTruoc.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", ngay, thang + 1, nam));
        tvNgayXemTruoc.setTextColor(Color.parseColor("#1E293B"));
        tvNgayXemTruoc.setTextSize(15);
        tvNgayXemTruoc.setGravity(Gravity.CENTER_VERTICAL);
        tvNgayXemTruoc.setPadding(dp(context, 14), 0, dp(context, 14), 0);
        tvNgayXemTruoc.setBackground(taoNenBoGoc(context, "#F8FAFC", 12, "#D6E4F0", 1));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 52)
        );
        previewParams.setMargins(0, 0, 0, dp(context, 12));
        tvNgayXemTruoc.setLayoutParams(previewParams);
        layout.addView(tvNgayXemTruoc);

        DatePicker datePicker = new DatePicker(new ContextThemeWrapper(context, R.style.ReportDatePickerTheme));
        datePicker.setBackground(taoNenBoGoc(context, "#F8FAFC", 12, "#D6E4F0", 1));
        datePicker.init(nam, thang, ngay, new DatePicker.OnDateChangedListener()
        {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                tvNgayXemTruoc.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year));
            }
        });
        layout.addView(datePicker);

        LinearLayout actionLayout = new LinearLayout(context);
        actionLayout.setGravity(Gravity.END);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        actionParams.setMargins(0, dp(context, 12), 0, 0);
        actionLayout.setLayoutParams(actionParams);

        TextView btnHuy = taoNutLich(context, "Hủy", "#EAF6FF", "#0D5FA8");
        btnHuy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        TextView btnApDung = taoNutLich(context, "Áp dụng", "#0D3F6E", "#FFFFFF");
        btnApDung.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String ngayDaChon = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        datePicker.getDayOfMonth(),
                        datePicker.getMonth() + 1,
                        datePicker.getYear());
                tv.setText(ngayDaChon);
                tv.setTextColor(Color.parseColor("#1E293B"));
                dialog.dismiss();
            }
        });

        actionLayout.addView(btnHuy);
        LinearLayout.LayoutParams applyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(context, 48)
        );
        applyParams.setMargins(dp(context, 8), 0, 0, 0);
        actionLayout.addView(btnApDung, applyParams);
        layout.addView(actionLayout);

        dialog.setView(layout);
        dialog.show();

        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    private int dp(Context context, int value)
    {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private TextView taoNutLich(Context context, String text, String backgroundColor, String textColor)
    {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextColor(Color.parseColor(textColor));
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(dp(context, 96));
        button.setMinHeight(dp(context, 48));
        button.setPadding(dp(context, 16), 0, dp(context, 16), 0);
        button.setClickable(true);
        button.setFocusable(true);
        button.setBackground(taoNenBoGoc(context, backgroundColor, 12, backgroundColor, 0));
        return button;
    }

    private GradientDrawable taoNenBoGoc(Context context, String solidColor, int radiusDp, String strokeColor, int strokeDp)
    {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(solidColor));
        drawable.setCornerRadius(dp(context, radiusDp));
        if (strokeDp > 0)
        {
            drawable.setStroke(dp(context, strokeDp), Color.parseColor(strokeColor));
        }
        return drawable;
    }
}
