package dashboard_fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nhom08_quanlyphongkham.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment_admin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment_admin extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment_admin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment_admin newInstance(String param1, String param2) {
        HomeFragment_admin fragment = new HomeFragment_admin();
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
        // Nạp đúng giao diện của màn hình Home
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        // I. Nút XEM BÁO CÁO VÀ THỐNG KÊ
        // 1. Tìm nút bấm XEM BÁO CÁO trên màn hình Home
        View btnXemBaoCao = view.findViewById(R.id.btn_xem_bao_cao);
        // 2. Xử lý khi nhấn nút
        btnXemBaoCao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đang ở Home -> Tạo màn hình Báo Cáo mới để chuyển sang
                ReportsFragment_Admin reportsFragment = new ReportsFragment_Admin();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, reportsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        // II. Nút CÀI ĐẶT THÔNG BÁO CHUNG
        // 1. Tìm nút bấm CÀI ĐẶT THÔNG BÁO CHUNG trên màn hình Home
        View btnCaiDatThongBao = view.findViewById(R.id.btn_cai_dat_thong_bao);
        // 2. Xử lí khi nhấn nút
        btnCaiDatThongBao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationSettingsFragment_Admin notificationsettingsFragment = new NotificationSettingsFragment_Admin();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, notificationsettingsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }

}