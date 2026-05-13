package dashboard_fragment;

import android.content.Intent;
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
import com.example.nhom08_quanlyphongkham.admin_manage_staff;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;

import dashboard_fragment.doctor_examination_list.ExaminationList_doctor;
import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import doctor_patient_list.doctor_patient_list;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment_doctor extends Fragment {

    private TextView tvName;
    private List<ExaminationFormWithPatientDto> allForms = new ArrayList<>();
    private ExaminationFormRepository repository;
    private CardView btnExaminationList, btnCreateAppointment, btnViewMedicalRecords;

    private ExaminationFormWithPatientDto nextPatient;

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
        repository = new ExaminationFormRepository(requireContext());
        SetAvatar(view, SharedPrefManager.getInstance(requireContext()).getProfile());
        loadAllFormsAndPatientDto(view);
        nextPatient = getPriorityPatient();
//        loadPatient(nextPatient, view);
        SetNumber(view);
        return view;
    }

    private void loadAllFormsAndPatientDto(View headerView) {
        repository.getAllFormsToday().enqueue(new retrofit2.Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull retrofit2.Response<List<ExaminationFormWithPatientDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu dữ liệu vào biến toàn cục để dùng chung
                    allForms = response.body();

                    // Sau khi có dữ liệu, mới gọi hàm cập nhật các con số
                    SetNumber(headerView);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
//    private void loadPatient(ExaminationFormWithPatientDto nextPatient, View view)
//    {
//        TextView Name = view.findViewById(R.id.nameNextPatient);
//        TextView Time = view.findViewById(R.id.timeNextPatient);
//        if(nextPatient != null)
//        {
//            Name.setText(nextPatient.getPatient_id());
//            Time.setText(nextPatient.getGio_du_kien());
//        }
//        else
//        {
//            Name.setText("");
//            Time.setText("");
//            Toast.makeText(getContext(), "Không có lịch hẹn nào", Toast.LENGTH_SHORT).show();
//        }
//    }
    private void SetNumber(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        int total = 0;
        int waitCount = 0;
        int checkingCount = 0;
        int doneCount = 0;

        for (ExaminationFormWithPatientDto form : allForms) {
            Date dateKham = form.getNgay_kham();
            String status = form.getTrang_thai();

            if (status == null) continue;

            // Chuyển ngày khám sang String để so sánh (nếu có)
            String formDateStr = (dateKham != null) ? sdf.format(dateKham) : "";
            boolean isToday = formDateStr.equals(today);

            if (status.equals("Đang khám")) {
                // "Đang khám": Đếm tất cả, không check ngày
                checkingCount++;
                total++;
            } else if (status.equals("Chờ khám") && isToday) {
                // "Chờ khám": Chỉ đếm hôm nay
                waitCount++;
                total++;
            } else if (status.equals("Đã khám") && isToday) {
                // "Đã khám": Chỉ đếm hôm nay
                doneCount++;
                total++;
            }
        }

        // Cập nhật UI
        updateUI(view, total, waitCount, checkingCount, doneCount);
    }
    private void updateUI(View v, int total, int wait, int checking, int done) {
        ((TextView) v.findViewById(R.id.PatientTotal)).setText(String.valueOf(total));

        ((TextView) v.findViewById(R.id.PatientWaiting)).setText(String.valueOf(wait));
        ((TextView) v.findViewById(R.id.num_cho_kham)).setText(wait + " chờ khám");

        ((TextView) v.findViewById(R.id.PatientChecking)).setText(String.valueOf(checking));
        ((TextView) v.findViewById(R.id.num_dang_kham)).setText(checking + " đang khám");

        ((TextView) v.findViewById(R.id.PatientDone)).setText(String.valueOf(done));
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
            btnExaminationList.setOnClickListener(v -> openExFormsLists());

        }

        if (btnCreateAppointment != null) {
            btnCreateAppointment.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình lịch hẹn tái khám
                Toast.makeText(getContext(), "Mở lịch hẹn tái khám", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnViewMedicalRecords != null) {
            btnViewMedicalRecords.setOnClickListener(v -> openPatientList());
        }
    }

    private void openExFormsLists() {
        Intent intent = new Intent(getActivity(), ExaminationList_doctor.class);
        startActivity(intent);
    }
    private void openPatientList() {
        Intent intent = new Intent(getActivity(), doctor_patient_list.class);
        startActivity(intent);
    }
    private List<ExaminationFormWithPatientDto> filterByTimeCondition(boolean isBefore) {
        List<ExaminationFormWithPatientDto> result = new ArrayList<>();

        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Date now = new Date();
        String today = dateSdf.format(now);
        String currentTime = timeSdf.format(now);

        for (ExaminationFormWithPatientDto form : allForms) {
            if (form.getNgay_kham() == null || form.getGio_du_kien() == null) continue;

            if ("Chờ khám".equals(form.getTrang_thai()) && dateSdf.format(form.getNgay_kham()).equals(today)) {

                String formTime = form.getGio_du_kien(); // Đây là String "HH:mm"

                if (isBefore) {
                    // Giờ hẹn > Giờ hiện tại (Chưa tới)
                    if (formTime.compareTo(currentTime) > 0) result.add(form);
                } else {
                    // Giờ hẹn <= Giờ hiện tại (Đã quá)
                    if (formTime.compareTo(currentTime) <= 0) result.add(form);
                }
            }
        }
        return result;
    }
    private ExaminationFormWithPatientDto getPriorityPatient() {
        long nowMs = System.currentTimeMillis();
        long fiveMinsMs = 5 * 60 * 1000;

        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        String todayStr = dateSdf.format(new Date());
        ExaminationFormWithPatientDto priorityPatient = null;
        long minDiff = Long.MAX_VALUE;

        for (ExaminationFormWithPatientDto form : allForms) {
            if ("Đã khám".equals(form.getTrang_thai())) {
                return form;
            }

            if ("Chờ khám".equals(form.getTrang_thai()) && form.getNgay_kham() != null && form.getGio_du_kien() != null) {
                try {
                    Date apptDate = fullSdf.parse(todayStr + " " + form.getGio_du_kien());
                    long apptMs = apptDate.getTime();

                    if (nowMs >= apptMs && nowMs <= (apptMs + fiveMinsMs)) {
                        long diff = nowMs - apptMs;
                        if (diff < minDiff) {
                            minDiff = diff;
                            priorityPatient = form;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return priorityPatient;
    }
}