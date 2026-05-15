package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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


public class HomeFragment_doctor extends Fragment {

    private TextView tvName;
    TextView btn_KhamNgay;
    TextView ic_ChoKham;
    UserProfile profile;

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
        profile = SharedPrefManager.getInstance(requireContext()).getProfile();
        View view = inflater.inflate(R.layout.fragment_home_doctor, container, false);
        repository = new ExaminationFormRepository(requireContext());
        SetAvatar(view, SharedPrefManager.getInstance(requireContext()).getProfile());
        loadAllFormsAndPatientDto(view);
        SetNumber(view);
        return view;
    }

    private void loadAllFormsAndPatientDto(View headerView) {

        if(profile.getID() == null) return;
        repository.getAllFormsToday(profile.getID()).enqueue(new retrofit2.Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull retrofit2.Response<List<ExaminationFormWithPatientDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu dữ liệu vào biến toàn cục để dùng chung
                    allForms = response.body();
                    // Sau khi có dữ liệu, mới gọi hàm cập nhật các con số
                    SetNumber(headerView);
                    nextPatient = getPriorityPatient();
                    loadPatient(nextPatient, headerView);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull Throwable t) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }            }
        });
    }

    private void loadPatient(ExaminationFormWithPatientDto nextPatient, View view) {
        TextView Name = view.findViewById(R.id.nameNextPatient);
        TextView Time = view.findViewById(R.id.timeNextPatient);

        // Kiểm tra thêm điều kiện getPatient() != null để tránh crash
        if (nextPatient != null && nextPatient.getPatient() != null) {
            btn_KhamNgay.setVisibility(view.VISIBLE);
            ic_ChoKham.setVisibility(view.VISIBLE);
            Name.setText(nextPatient.getPatient().getHo_ten()); // Hiện tên thay vì ID
            Time.setText(nextPatient.getGio_du_kien());
        } else {
            btn_KhamNgay.setVisibility(view.INVISIBLE);
            ic_ChoKham.setVisibility(view.INVISIBLE);
            Time.setText("");
            Name.setText("Chưa có bệnh nhân");

        }
    }
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

            String formDateStr = (dateKham != null) ? sdf.format(dateKham) : "";
            boolean isToday = formDateStr.equals(today);

            if (status.equals("Đang khám")) {
                checkingCount++;
                total++;
            } else if (status.equals("Chờ khám") && isToday) {
                waitCount++;
                total++;
            } else if (status.equals("Đã khám") && isToday) {
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

    if (profile != null && profile.getHo_ten() != null) {
            tvName.setText(profile.getHo_ten());
        }
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.doctor_home_name);
        btnExaminationList = view.findViewById(R.id.btnExaminationList);
        btnCreateAppointment = view.findViewById(R.id.btnCreateAppointment);
        btnViewMedicalRecords = view.findViewById(R.id.btnViewMedicalRecords);
        btn_KhamNgay = view.findViewById(R.id.btnKhamNgay);
        ic_ChoKham = view.findViewById(R.id.txtChoKham);
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
    private ExaminationFormWithPatientDto getPriorityPatient() {
        if (allForms == null || allForms.isEmpty()) return null;

        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateSdf.format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String now = sdf.format(new Date());

        for (ExaminationFormWithPatientDto form : allForms) {
            String formDate = (form.getNgay_kham() != null) ? dateSdf.format(form.getNgay_kham()) : "";
            String formTime = (form.getGio_du_kien() != null) ? form.getGio_du_kien() : "";

            if (today.equals(formDate) && "Chờ khám".equals(form.getTrang_thai()) && formTime.compareTo(now) > 0) {
                return form;
            }
        }

        return null;
    }
}