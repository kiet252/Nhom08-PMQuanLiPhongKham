package dashboard_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;
import dashboard_fragment.doctor_examination_list.DoctorExaminationFormAdapter;
import dashboard_fragment.staff_add_update_patient.AddUpdatePatientInfo_staff;
import dashboard_fragment.staff_create_examination_form.CreateExaminationForm_staff;
import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import dashboard_fragment.staff_manage_bill.ManageBill_staff;
import dashboard_fragment.staff_manage_examination_form.ManageExaminationForm_staff;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import retrofit2.Call;

public class HomeFragment_staff extends Fragment {

    private static final String ARG_TOKEN = "token";

    private String currentToken;
    private CardView BtnCreateExForm, BtnManageMedReport, BtnAddUpdatePatientInfo, BtnManageBill;
    private LinearLayout layoutTodayExFormsHeader;
    private LinearLayout layoutTodayExFormsContainer;
    private TextView tvTodayExFormsEmpty;

    private ExaminationFormRepository repository;
    private final List<ExaminationFormWithPatientDto> todayForms = new ArrayList<>();

    public HomeFragment_staff() {
    }

    public static HomeFragment_staff newInstance(String token) {
        HomeFragment_staff fragment = new HomeFragment_staff();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentToken = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_staff, container, false);
        repository = new ExaminationFormRepository(requireContext());

        initializeViews(view);
        setupListeners(view);

        TextView tvName = view.findViewById(R.id.staff_home_name);
        SetAvatar(view, SharedPrefManager.getInstance(requireContext()).getProfile());
        UserProfile profile = SharedPrefManager.getInstance(requireContext()).getProfile();
        if (profile != null && profile.getHo_ten() != null) {
            tvName.setText(profile.getHo_ten());
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayExaminationForms();
    }

    private void initializeViews(View view) {
        BtnCreateExForm = view.findViewById(R.id.CardViewCreateExaminationForm);
        BtnManageMedReport = view.findViewById(R.id.CardViewManageMedReport);
        BtnAddUpdatePatientInfo = view.findViewById(R.id.CardViewAddUpdatePatientInfo);
        BtnManageBill = view.findViewById(R.id.CardViewManageBill);

        layoutTodayExFormsHeader = view.findViewById(R.id.layoutTodayExFormsHeader);
        layoutTodayExFormsContainer = view.findViewById(R.id.layoutTodayExFormsContainer);
        tvTodayExFormsEmpty = view.findViewById(R.id.tvTodayExFormsEmpty);
    }

    private void setupListeners(View view) {
        BtnAddUpdatePatientInfo.setOnClickListener(v -> startAddUpdatePatientIntent());
        BtnCreateExForm.setOnClickListener(v -> startCreateExaminationFormIntent());
        BtnManageMedReport.setOnClickListener(v -> startManageExaminationFormIntent());
        BtnManageBill.setOnClickListener(v -> startManageBillIntent());

        layoutTodayExFormsHeader.setOnClickListener(v -> startManageExaminationFormIntent());

        TextView tvViewAll = view.findViewById(R.id.tvTodayExFormsViewAll);
        tvViewAll.setOnClickListener(v -> startManageExaminationFormIntent());
    }

    public void SetAvatar(View view, UserProfile userprofile) {
        if (userprofile == null) return;
        ImageView avatar = view.findViewById(R.id.home_avatar_staff);
        if (avatar == null) {
            return;
        }

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

    private void loadTodayExaminationForms() {
        repository.getAllFormsToday().enqueue(new retrofit2.Callback<List<ExaminationFormWithPatientDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExaminationFormWithPatientDto>> call,
                                   @NonNull retrofit2.Response<List<ExaminationFormWithPatientDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    todayForms.clear();
                    todayForms.addAll(filterTodayActiveForms(response.body()));
                    renderTodayExaminationForms();
                } else {
                    showEmptyState("Không tải được danh sách phiếu khám hôm nay");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ExaminationFormWithPatientDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showEmptyState("Không tải được danh sách phiếu khám hôm nay");
            }
        });
    }

    private List<ExaminationFormWithPatientDto> filterTodayActiveForms(List<ExaminationFormWithPatientDto> source) {
        List<ExaminationFormWithPatientDto> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        for (ExaminationFormWithPatientDto form : source) {
            if (form == null) continue;

            String status = form.getTrang_thai();
            Date examDate = form.getNgay_kham();
            String formDate = examDate != null ? sdf.format(examDate) : "";

            boolean isToday = today.equals(formDate);
            boolean isVisibleStatus = "Chờ khám".equals(status) || "Đang khám".equals(status);

            if (isToday && isVisibleStatus) {
                filtered.add(form);
            }
        }

        filtered.sort((a, b) -> {
            String timeA = a.getGio_du_kien() == null ? "" : a.getGio_du_kien();
            String timeB = b.getGio_du_kien() == null ? "" : b.getGio_du_kien();
            return timeA.compareTo(timeB);
        });

        return filtered;
    }

    private void renderTodayExaminationForms() {
        if (!isAdded()) return;

        layoutTodayExFormsContainer.removeAllViews();

        if (todayForms.isEmpty()) {
            showEmptyState("Hôm nay chưa có phiếu khám chờ xử lý");
            return;
        }

        tvTodayExFormsEmpty.setVisibility(View.GONE);
        layoutTodayExFormsContainer.setVisibility(View.VISIBLE);

        for (ExaminationFormWithPatientDto form : todayForms) {
            View row = DoctorExaminationFormAdapter.createBoundRow(
                    requireContext(),
                    layoutTodayExFormsContainer,
                    form,
                    this::showExaminationFormDetails
            );
            layoutTodayExFormsContainer.addView(row);
        }
    }
    private void showExaminationFormDetails(ExaminationFormWithPatientDto form) {
        if (!isAdded() || getContext() == null) return;

        View dialogView = getLayoutInflater().inflate(
                R.layout.staff_examination_detail_dialog,
                null,
                false
        );

        TextView tvPatientName = dialogView.findViewById(R.id.tvDetailPatientName);
        TextView tvBirthday = dialogView.findViewById(R.id.tvDetailBirthday);
        TextView tvAddress = dialogView.findViewById(R.id.tvDetailAddress);
        TextView tvExamDate = dialogView.findViewById(R.id.tvDetailExamDate);
        TextView tvExamTime = dialogView.findViewById(R.id.tvDetailExamTime);
        TextView tvSequence = dialogView.findViewById(R.id.tvDetailSequence);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailStatus);
        TextView tvSymptoms = dialogView.findViewById(R.id.tvDetailSymptoms);
        com.google.android.material.button.MaterialButton btnClose =
                dialogView.findViewById(R.id.btnCloseDetail);

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

        dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.PatientBriefDto patient = form.getPatient();

        tvPatientName.setText("Họ tên: " + (patient != null ? patient.getHo_ten() : "--"));
        tvBirthday.setText("Ngày sinh: " + (patient != null && patient.getNgay_sinh() != null
                ? sdf.format(patient.getNgay_sinh()) : "--"));
        tvAddress.setText("Địa chỉ: " + (patient != null ? patient.getDia_chi() : "--"));
        tvExamDate.setText("Ngày khám: " + (form.getNgay_kham() != null
                ? sdf.format(form.getNgay_kham()) : "--"));
        tvExamTime.setText("Giờ dự kiến: " + (form.getGio_du_kien() != null
                ? form.getGio_du_kien() : "--"));
        tvSequence.setText("Số tiếp nhận: " + form.getSo_tiep_nhan());
        tvStatus.setText("Trạng thái: " + (form.getTrang_thai() != null
                ? form.getTrang_thai() : "--"));
        tvSymptoms.setText(form.getTrieu_chung_ban_dau() != null
                ? form.getTrieu_chung_ban_dau() : "--");

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private void showEmptyState(String message) {
        if (!isAdded()) return;
        layoutTodayExFormsContainer.removeAllViews();
        layoutTodayExFormsContainer.setVisibility(View.GONE);
        tvTodayExFormsEmpty.setText(message);
        tvTodayExFormsEmpty.setVisibility(View.VISIBLE);
    }

    private void startAddUpdatePatientIntent() {
        Intent intentAddUpdatePatient = new Intent(getContext(), AddUpdatePatientInfo_staff.class);
        intentAddUpdatePatient.putExtra("accessToken", currentToken);
        startActivity(intentAddUpdatePatient);
    }

    private void startCreateExaminationFormIntent() {
        Intent intentCreateExaminationForm = new Intent(getContext(), CreateExaminationForm_staff.class);
        intentCreateExaminationForm.putExtra("accessToken", currentToken);
        startActivity(intentCreateExaminationForm);
    }

    private void startManageExaminationFormIntent() {
        Intent intentManageExaminationForm = new Intent(getContext(), ManageExaminationForm_staff.class);
        intentManageExaminationForm.putExtra("accessToken", currentToken);
        startActivity(intentManageExaminationForm);
    }

    private void startManageBillIntent() {
        Intent intentManageBill = new Intent(getContext(), ManageBill_staff.class);
        intentManageBill.putExtra("accessToken", currentToken);
        startActivity(intentManageBill);
    }
}