package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.admin_manage_staff.admin_manage_staff;
import com.example.nhom08_quanlyphongkham.admin_reports.ReportsActivity_Admin;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;

import coil.Coil;
import coil.request.ImageRequest;

public class HomeFragment_admin extends Fragment {

    private Button mngStaff;
    private TextView txtName;

    public HomeFragment_admin() {
        // Required empty public constructor
    }

    public static HomeFragment_admin newInstance() {
        return new HomeFragment_admin();
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
                // Đang ở Home -> Chuyển sang ReportsActivity_Admin
                Intent intent = new Intent(getActivity(), ReportsActivity_Admin.class);
                startActivity(intent);
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
    public void SetAvatar(View view, UserProfile userprofile)
    {
        if (userprofile == null) return;
        ImageView avatar = view.findViewById(R.id.home_avatar_admin);
        String avatarUrl = userprofile.getAnh_dai_dien();

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + avatarUrl;
            }

            ImageRequest request = new ImageRequest.Builder(requireContext())
                    .data(avatarUrl)
                    .target(avatar)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build();

            Coil.imageLoader(requireContext()).enqueue(request);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        txtName = view.findViewById(R.id.admin_home_name);
        mngStaff = view.findViewById(R.id.btn_mngStaff);

        // Lấy tên người dùng từ SharedPrefManager
        SharedPrefManager prefManager = SharedPrefManager.getInstance(requireContext());
        UserProfile profile = prefManager.getProfile();
        SetAvatar(view, profile);
        if (profile != null && profile.getHo_ten() != null) {
            txtName.setText(profile.getHo_ten());
        } else {

        }

        mngStaff.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), admin_manage_staff.class);
            startActivity(intent);
        });
    }
}