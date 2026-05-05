package dashboard_fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;

import coil.Coil;
import coil.request.ImageRequest;

public class HomeFragment_doctor extends Fragment {

    private TextView tvName;
    private MaterialButton btnExaminationList, btnReExaminationList;

    public HomeFragment_doctor() {
        // Required empty public constructor
    }

    public static HomeFragment_doctor newInstance() {
        return new HomeFragment_doctor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_doctor, container, false);
        SetAvatar(view, SharedPrefManager.getInstance(requireContext()).getProfile());
        return inflater.inflate(R.layout.fragment_home_doctor, container, false);
    }
    public void SetAvatar(View view, UserProfile userprofile)
    {
        ImageView avatar;
        avatar = view.findViewById(R.id.home_avatar_admin);
        ImageRequest request = new ImageRequest.Builder(getContext())
                .data(userprofile.getAnh_dai_dien())
                .target(avatar)
                .crossfade(true) // Hiệu ứng mờ dần khi hiện ảnh
                .build();

        Coil.imageLoader(getContext()).enqueue(request);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupListeners();
        
        // Hiển thị thông tin bác sĩ
        UserProfile profile = SharedPrefManager.getInstance(requireContext()).getProfile();
        if (profile != null && profile.getHo_ten() != null) {
            tvName.setText("Chào mừng, " + profile.getHo_ten());
        }
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.doctor_home_name);
        btnExaminationList = view.findViewById(R.id.btnExaminationList);
        btnReExaminationList = view.findViewById(R.id.btnReExaminationList);
    }

    private void setupListeners() {
        if (btnExaminationList != null) {
            btnExaminationList.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình danh sách khám
                Toast.makeText(getContext(), "Mở danh sách khám", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnReExaminationList != null) {
            btnReExaminationList.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình lịch hẹn tái khám
                Toast.makeText(getContext(), "Mở lịch hẹn tái khám", Toast.LENGTH_SHORT).show();
            });
        }
    }
}