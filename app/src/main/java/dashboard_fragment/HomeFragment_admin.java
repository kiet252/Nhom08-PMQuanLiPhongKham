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
import com.example.nhom08_quanlyphongkham.admin_manage_staff;
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
        return inflater.inflate(R.layout.fragment_home_admin, container, false);
    }
    private void LoadImage(UserProfile userProfile, ImageView avatar, View view)
    {
        avatar = view.findViewById(R.id.home_avatar_admin);
        ImageRequest request = new ImageRequest.Builder(getContext())
                .data(userProfile.getAnh_dai_dien())
                .target(avatar)
                .crossfade(true) // Hiệu ứng mờ dần khi hiện ảnh
                .build();

        Coil.imageLoader(getContext()).enqueue(request);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        txtName = view.findViewById(R.id.admin_home_name);
        mngStaff = view.findViewById(R.id.btn_mngStaff);

        // Lấy tên người dùng từ SharedPrefManager
        SharedPrefManager prefManager = SharedPrefManager.getInstance(requireContext());
        UserProfile profile = prefManager.getProfile();
        
        if (profile != null && profile.getHo_ten() != null) {
            txtName.setText("Chào mừng, " + profile.getHo_ten());
        } else {
            txtName.setText("Chào mừng, Quản trị viên");
        }

        mngStaff.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), admin_manage_staff.class);
            startActivity(intent);
        });
    }
}