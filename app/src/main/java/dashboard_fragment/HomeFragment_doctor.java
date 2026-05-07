package dashboard_fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.CountResponse;
import com.example.nhom08_quanlyphongkham.PatientApiService;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment_doctor extends Fragment {

    private TextView tvName;

    private CardView btnExaminationList, btnCreateAppointment, btnViewMedicalRecords;


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
        SetNumber(view);
        return view;
    }
    private void SetNumber(View view)
    {
        SetTotalNumber(view);
        SetWaitNumber(view);
        SetCheckingNumber(view);
        SetDoneNumber(view);
    }
    private void SetTotalNumber(View view) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String filter = "eq." + today;

        // Lấy instance của ApiService từ Provider
        PatientApiService apiService = SupabaseClientProvider.getClient(requireContext()).create(PatientApiService.class);

        // Gọi API (chỉ cần truyền filter và "count")
        apiService.getTodayCount(filter, "count")
                .enqueue(new Callback<List<CountResponse>>() {
                    @Override
                    public void onResponse(Call<List<CountResponse>> call, Response<List<CountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            long total = response.body().get(0).getCount();
                            // Hiển thị total lên TextView của bạn ở đây
                            TextView patient_total = view.findViewById(R.id.PatientTotal);
                            patient_total.setText(String.valueOf(total));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CountResponse>> call, Throwable t) {
                    }
                });
    }
    private void SetWaitNumber(View view) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String filter = "eq." + today;

        // Lấy instance của ApiService từ Provider
        PatientApiService apiService = SupabaseClientProvider.getClient(requireContext()).create(PatientApiService.class);

        // Gọi API (chỉ cần truyền filter và "count")
        apiService.getTodayWaitCount(filter, "Chờ khám","count")
                .enqueue(new Callback<List<CountResponse>>() {
                    @Override
                    public void onResponse(Call<List<CountResponse>> call, Response<List<CountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            long total = response.body().get(0).getCount();
                            // Hiển thị total lên TextView của bạn ở đây
                            TextView patient_total = view.findViewById(R.id.PatientTotal);
                            patient_total.setText(String.valueOf(total));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CountResponse>> call, Throwable t) {
                    }
                });
    }
    private void SetCheckingNumber(View view) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String filter = "eq." + today;

        // Lấy instance của ApiService từ Provider
        PatientApiService apiService = SupabaseClientProvider.getClient(requireContext()).create(PatientApiService.class);

        // Gọi API (chỉ cần truyền filter và "count")
        apiService.getTodayWaitCount(filter, "Đang khám","count")
                .enqueue(new Callback<List<CountResponse>>() {
                    @Override
                    public void onResponse(Call<List<CountResponse>> call, Response<List<CountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            long total = response.body().get(0).getCount();
                            // Hiển thị total lên TextView của bạn ở đây
                            TextView patient_total = view.findViewById(R.id.PatientTotal);
                            patient_total.setText(String.valueOf(total));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CountResponse>> call, Throwable t) {
                    }
                });
    }
    private void SetDoneNumber(View view) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String filter = "eq." + today;

        // Lấy instance của ApiService từ Provider
        PatientApiService apiService = SupabaseClientProvider.getClient(requireContext()).create(PatientApiService.class);

        // Gọi API (chỉ cần truyền filter và "count")
        apiService.getTodayWaitCount(filter, "Đã khám","count")
                .enqueue(new Callback<List<CountResponse>>() {
                    @Override
                    public void onResponse(Call<List<CountResponse>> call, Response<List<CountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            long total = response.body().get(0).getCount();
                            // Hiển thị total lên TextView của bạn ở đây
                            TextView patient_total = view.findViewById(R.id.PatientTotal);
                            patient_total.setText(String.valueOf(total));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CountResponse>> call, Throwable t) {
                    }
                });
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
            tvName.setText(profile.getHo_ten());
        }
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.doctor_home_name);
        btnExaminationList = view.findViewById(R.id.btnExaminationList);
        btnCreateAppointment = view.findViewById(R.id.btnCreateAppointment);
        btnViewMedicalRecords = view.findViewById(R.id.btnViewMedicalRecords);

    }

    private void setupListeners() {
        if (btnExaminationList != null) {
            btnExaminationList.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình danh sách khám
                Toast.makeText(getContext(), "Mở danh sách khám", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCreateAppointment != null) {
            btnCreateAppointment.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình lịch hẹn tái khám
                Toast.makeText(getContext(), "Mở lịch hẹn tái khám", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnViewMedicalRecords != null) {
            btnViewMedicalRecords.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình xem hồ sơ bệnh án
                Toast.makeText(getContext(), "Mở lịch hẹn tái khám", Toast.LENGTH_SHORT).show();
            });
        }
    }
}