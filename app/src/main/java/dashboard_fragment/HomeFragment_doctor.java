package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;

import dashboard_fragment.doctor_create_appointment.CreateAppointment_doctor;
import dashboard_fragment.doctor_examination_list.ExaminationList_doctor;
import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;


public class HomeFragment_doctor extends Fragment {

    private TextView tvName;
    private TextView btn_KhamNgay;
    private TextView ic_ChoKham;
    private UserProfile profile;
    private View rootView;

    private List<ExaminationFormWithPatientDto> allForms = new ArrayList<>();
    private ExaminationFormRepository repository;
    private CardView btnExaminationList, btnCreateAppointment, btnViewMedicalRecords, btnTimekeeping;

    private ExaminationFormWithPatientDto nextPatient;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                loadAllFormsAndPatientDto();
                refreshHandler.postDelayed(this, 10000);
            }
        }
    };

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
        rootView = inflater.inflate(R.layout.fragment_home_doctor, container, false);
        repository = new ExaminationFormRepository(requireContext());
        
        initializeViews(rootView);
        setupListeners();
        SetAvatar(rootView, profile);
        
        return rootView;
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.doctor_home_name);
        btnExaminationList = view.findViewById(R.id.btnExaminationList);
        btnCreateAppointment = view.findViewById(R.id.btnCreateAppointment);
        btnViewMedicalRecords = view.findViewById(R.id.btnViewMedicalRecords);
        btn_KhamNgay = view.findViewById(R.id.btnKhamNgay);
        ic_ChoKham = view.findViewById(R.id.txtChoKham);
        btnTimekeeping = view.findViewById(R.id.btnTimekeeping);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.post(autoRefreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    private void loadAllFormsAndPatientDto() {
        if (profile == null || profile.getID() == null || rootView == null) return;

        repository.getAllFormsToday(profile.getID()).enqueue(new retrofit2.Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull retrofit2.Response<List<ExaminationFormWithPatientDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    allForms = response.body();
                    SetNumber(rootView);
                    nextPatient = getPriorityPatient();
                    loadPatient(nextPatient, rootView);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull Throwable t) {
            }
        });
    }

    private void loadPatient(ExaminationFormWithPatientDto nextPatient, View view) {
        TextView Name = view.findViewById(R.id.nameNextPatient);
        TextView Time = view.findViewById(R.id.timeNextPatient);

        if (nextPatient != null && nextPatient.getPatient() != null) {
            btn_KhamNgay.setVisibility(View.VISIBLE);
            ic_ChoKham.setVisibility(View.VISIBLE);
            Name.setText(nextPatient.getPatient().getHo_ten());
            Time.setText(nextPatient.getGio_du_kien());
        } else {
            btn_KhamNgay.setVisibility(View.INVISIBLE);
            ic_ChoKham.setVisibility(View.INVISIBLE);
            Time.setText("");
            Name.setText("Chưa có bệnh nhân");
        }
    }

    private void SetNumber(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        int total = 0, waitCount = 0, checkingCount = 0, doneCount = 0;

        for (ExaminationFormWithPatientDto form : allForms) {
            String status = form.getTrang_thai();
            if (status == null) continue;
            String formDateStr = (form.getNgay_kham() != null) ? sdf.format(form.getNgay_kham()) : "";
            boolean isToday = formDateStr.equals(today);

            if (status.equals("Đang khám")) { checkingCount++; total++; }
            else if (status.equals("Chờ khám") && isToday) { waitCount++; total++; }
            else if (status.equals("Đã khám") && isToday) { doneCount++; total++; }
        }
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

    public void SetAvatar(View view, UserProfile userprofile) {
        if (userprofile == null) return;
        ImageView avatar = view.findViewById(R.id.home_avatar_admin);
        String url = userprofile.getAnh_dai_dien();
        if (url != null && !url.isEmpty() && !url.startsWith("http")) {
            url = "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + url;
        }
        ImageRequest request = new ImageRequest.Builder(requireContext()).data(url).target(avatar).crossfade(true).build();
        Coil.imageLoader(requireContext()).enqueue(request);
    }

    private void setupListeners() {
        btnExaminationList.setOnClickListener(v -> startActivity(new Intent(getActivity(), ExaminationList_doctor.class)));
        btnCreateAppointment.setOnClickListener(v -> startActivity(new Intent(getActivity(), CreateAppointment_doctor.class)));
        btnViewMedicalRecords.setOnClickListener(v -> startActivity(new Intent(getActivity(), dashboard_fragment.doctor_view_medical_record.ViewMedicalRecord_doctor.class)));
        btn_KhamNgay.setOnClickListener(v -> {
            if (nextPatient == null) return;
            Intent intent = new Intent(getActivity(), ExaminationList_doctor.class);
            intent.putExtra("auto_open_form_id", nextPatient.getId());
            startActivity(intent);
        });
        
        // MỞ GIAO DIỆN CHẤM CÔNG
        if (btnTimekeeping != null) {
            btnTimekeeping.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), timekeeping.class));
            });
        }
    }

    private ExaminationFormWithPatientDto getPriorityPatient() {
        if (allForms == null || allForms.isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        for (ExaminationFormWithPatientDto form : allForms) {
            String fDate = (form.getNgay_kham() != null) ? sdf.format(form.getNgay_kham()) : "";
            if (today.equals(fDate) && "Chờ khám".equals(form.getTrang_thai())) return form;
        }
        return null;
    }
}
