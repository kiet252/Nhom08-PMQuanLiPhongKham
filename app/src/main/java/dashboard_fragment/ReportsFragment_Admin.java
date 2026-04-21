package dashboard_fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

//Thêm chức năng Tìm Kiếm
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

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
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //1. Lưu giao diện vào biến "view" thay vì return trực tiếp
        View view = inflater.inflate(R.layout.activity_reports_fragment_admin, container, false);
        //2. Tìm và ánh xạ các thành phần giao diện
        EditText etMaHoaDon = view.findViewById(R.id.et_ma_hoa_don);
        Button btnTimKiem = view.findViewById(R.id.btn_tim_kiem);
        TextView tvEmptyState = view.findViewById(R.id.tv_empty_state);
        RecyclerView rvDanhSach = view.findViewById(R.id.rv_danh_sach_doanh_thu);
        //3. Thêm sự kiện click cho nút tìm kiếm
        if (btnTimKiem != null)
        {
            //Kiểm tra null để tránh lỗi crash app nếu chưa có nút trên giao diện
            btnTimKiem.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String maHoaDonCanTim = etMaHoaDon.getText().toString().trim();
                    //Kiểm tra để trống
                    if (maHoaDonCanTim.isEmpty())
                    {
                        Toast.makeText(getContext(), "Vui lòng nhập Mã hóa đơn cần tìm!", Toast.LENGTH_LONG).show();
                        etMaHoaDon.requestFocus();
                        return;
                    }
                    //Kiểm tra khoảng trắng
                    if (maHoaDonCanTim.contains(" "))
                    {
                        Toast.makeText(getContext(), "Mã hóa đơn không được chứa khoảng trắng!", Toast.LENGTH_LONG).show();
                        etMaHoaDon.requestFocus();
                        return;
                    }
                    boolean found = false;
                    // TODO: Thêm code vòng lặp tìm kiếm

                    //Xử lí hiển thị sau khi tim kiếm
                    if (!found)
                    {
                        Toast.makeText(getContext(), "Không tìm thấy hóa đơn có mã: " + maHoaDonCanTim, Toast.LENGTH_SHORT).show();
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
}