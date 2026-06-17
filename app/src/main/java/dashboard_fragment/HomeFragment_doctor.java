package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
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

import dashboard_fragment.account_chatbot.ChatbotBottomSheetFragment;
import dashboard_fragment.doctor_create_appointment.CreateAppointment_doctor;
import dashboard_fragment.doctor_examination_list.ExaminationList_doctor;
import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import dashboard_fragment.timekeeping.Timekeeping;
import dashboard_fragment.timekeeping.TimekeepingRepository;
import dashboard_fragment.timekeeping.ca_lam_viec;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class HomeFragment_doctor extends Fragment {

    private TextView tvName;
    private TextView btn_KhamNgay;
    private UserProfile profile;
    private View rootView;

    private List<ExaminationFormWithPatientDto> allForms = new ArrayList<>();
    private ExaminationFormRepository repository;
    private CardView btnExaminationList, btnCreateAppointment, btnViewMedicalRecords, btnTimekeeping;
    private TextView tvStatus;
    private TextView tvWorkTime;
    private TimekeepingRepository timekeepingRepository;

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
        timekeepingRepository = new TimekeepingRepository(requireContext());

        initializeViews(rootView);
        loadTimekeepingForHome();
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
        btnTimekeeping = view.findViewById(R.id.btnTimekeeping);
        tvStatus = view.findViewById(R.id.status);
        tvWorkTime = view.findViewById(R.id.work_time);

        View chatbotView = rootView.findViewById(R.id.chatbot_floating_button);
        if (chatbotView != null) {
            chatbotView.setOnClickListener(v ->
                    ChatbotBottomSheetFragment.newInstance(UserRole.BAC_SI.name())
                            .show(getParentFragmentManager(), "chatbot")
            );
        }

    }

    private void loadTimekeepingForHome() {
        if (profile == null || profile.getID() == null || timekeepingRepository == null) return;
        timekeepingRepository.getCaLamViecList(profile.getID()).enqueue(new Callback<java.util.List<ca_lam_viec>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<ca_lam_viec>> call, retrofit2.Response<java.util.List<ca_lam_viec>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    java.util.List<ca_lam_viec> shifts = response.body();
                    java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat outFmt = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                    java.util.Date now = new java.util.Date();

                    ca_lam_viec current = null;
                    ca_lam_viec next = null;
                    java.util.Date nextStart = null;

                    for (ca_lam_viec s : shifts) {
                        try {
                            String st = s.getStart_time();
                            String et = s.getEnd_time();
                            java.util.Date dStart = st == null ? null : inFmt.parse(st);
                            java.util.Date dEnd = et == null ? null : inFmt.parse(et);
                            if (dStart != null && dEnd != null) {
                                if (!now.before(dStart) && !now.after(dEnd)) {
                                    // now in [start, end]
                                    current = s;
                                    break;
                                }
                                if (dStart.after(now)) {
                                    if (next == null || (nextStart != null && dStart.before(nextStart))) {
                                        next = s;
                                        nextStart = dStart;
                                    } else if (next == null) {
                                        next = s;
                                        nextStart = dStart;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            // ignore parse errors
                        }
                    }

                    if (current != null) {
                        tvStatus.setText("●");
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // green
                        tvWorkTime.setText(current.getDisplayTime());
                        tvWorkTime.setTextColor(android.graphics.Color.parseColor("#E67E22"));
                    } else if (next != null) {
                        tvStatus.setText("●");
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#2563EB")); // blue
                        tvWorkTime.setText(next.getDisplayTime());
                        tvWorkTime.setTextColor(android.graphics.Color.parseColor("#E67E22"));
                    } else {
                        tvStatus.setText("●");
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#9CA3AF")); // gray
                        tvWorkTime.setText("--:-- - --:--");
                        tvWorkTime.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                    }
                } else {
                    // no shifts
                    tvStatus.setText("●");
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                    tvWorkTime.setText("--:-- - --:--");
                    tvWorkTime.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<ca_lam_viec>> call, Throwable t) {
                if (!isAdded()) return;
                tvStatus.setText("●");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                tvWorkTime.setText("--:-- - --:--");
                tvWorkTime.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
            }
        });
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
            Name.setText(nextPatient.getPatient().getHo_ten());
            Time.setText(nextPatient.getGio_du_kien());
        } else {
            btn_KhamNgay.setVisibility(View.GONE);
            Time.setVisibility(View.GONE);
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
                startActivity(new Intent(getActivity(), Timekeeping.class));
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
