package dashboard_fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//Thêm chức năng Tìm Kiếm
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import com.example.nhom08_quanlyphongkham.R;

//List
import java.util.ArrayList;

//Calendar
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
                    List<DonKham> danhSachKetQua = new ArrayList<>();
                    // 1. Ánh xạ thành chữ thường để dễ so sánh
                    String tukhoaMaDK = etMaDonKham.getText().toString().trim().toLowerCase();
                    String tukhoaTenBN = etTenBN.getText().toString().trim().toLowerCase();

                    for (DonKham dk : danhSachKetQua)
                    {
                        String maDonKhamGoc = dk.getMaDonKham().toLowerCase();
                        String maTenBNGoc = dk.getTenBN().toLowerCase();


                        if ((!tukhoaMaDK.isEmpty() && maDonKhamGoc.contains(tukhoaMaDK)) ||
                            (!tukhoaTenBN.isEmpty() && maTenBNGoc.contains(tukhoaTenBN)))
                        {
                            danhSachKetQua.add(dk);
                            found = true;
                        }
                    }

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

        public DonKham (String maHoaDon, String tenBenhNhan, String ngayKham)
        {
            this.MaDonKham = maHoaDon;
            this.TenBN = tenBenhNhan;
            this.NgayKham = ngayKham;
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
    }
    private void hienThiLich(TextView tv)
    {
        Calendar calendar = Calendar.getInstance();
        int nam = calendar.get(Calendar.YEAR);
        int thang = calendar.get(Calendar.MONTH);
        int ngay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dom)
                    {
                        String ngaydachon = String.format("%02d/%02d/%04d", dom, month + 1, year);
                        tv.setText(ngaydachon);
                        tv.setTextColor(Color.BLACK);
                    }
                }, nam, thang, ngay);
        dialog.show();
    }
}