package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

import dashboard_fragment.staff_create_examination_form.ExaminationFormRepository;
import retrofit2.Call;

public class TabPatientInfoFragment extends Fragment {
    private View rootView;
    private ExaminationFormRepository ExFormRepository;
    private DoctorExaminationStatus currentExaminationStatus;
    private String currentFormId;
    private EditText edtSymptoms;

    private View btnWaiting;
    private View btnInProgress;
    private View btnDone;

    public TabPatientInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_patient_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        ExFormRepository = new ExaminationFormRepository(getContext());

        btnWaiting = rootView.findViewById(R.id.btnStatusWaiting);
        btnInProgress = rootView.findViewById(R.id.btnStatusInProgress);
        btnDone = rootView.findViewById(R.id.btnStatusDone);

        ExaminationFormDetail_doctor activity = (ExaminationFormDetail_doctor) requireActivity();
        bindFormData(activity);
        currentExaminationStatus = activity.getCurrentStatus();
        applyStatus(currentExaminationStatus);
        updateBottomButton(activity);
        setupStatusClicks(activity);
        setupContinueButton(activity);
    }

    private void setupStatusClicks(ExaminationFormDetail_doctor activity) {
        if (btnWaiting != null) {
            btnWaiting.setOnClickListener(v -> onStatusSelected(activity, DoctorExaminationStatus.WAITING));
        }
        if (btnInProgress != null) {
            btnInProgress.setOnClickListener(v -> onStatusSelected(activity, DoctorExaminationStatus.IN_PROGRESS));
        }
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> onStatusSelected(activity, DoctorExaminationStatus.DONE));
        }
    }

    private void onStatusSelected(ExaminationFormDetail_doctor activity, DoctorExaminationStatus status) {
        if (currentExaminationStatus == status) return;

        currentExaminationStatus = status;
        activity.updateCurrentStatus(status);
        applyStatus(status);
        updateBottomButton(activity);

        updateExaminationFormInfo(activity, false);
    }

    private void applyStatus(DoctorExaminationStatus status) {
        DoctorExaminationStatusStyle.applyStatusSectionStyle(
                rootView,
                status == null ? DoctorExaminationStatus.WAITING.getDisplayName() : status.getDisplayName()
        );
    }

    private void bindFormData(ExaminationFormDetail_doctor activity) {
        currentFormId = activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_FORM_ID);
        setText(R.id.tvDetailPatientFullName,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_NAME),
                "--");
        setText(R.id.tvDetailPatientCode,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_ID),
                "--");
        setText(R.id.tvDetailPatientBirthday,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_BIRTHDAY),
                "--");
        setText(R.id.tvDetailPatientAddress,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_ADDRESS),
                "--");
        setText(R.id.tvDetailPatientCCCD,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_CCCD),
                "--");
        setText(R.id.tvDetailPatientGender,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_GENDER),
                "--");
        setText(R.id.tvDetailPatientPhone,
                activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_PATIENT_PHONE),
                "--");

        edtSymptoms = rootView.findViewById(R.id.edtDetailSymptoms);
        if (edtSymptoms != null) {
            edtSymptoms.setText(ExaminationFormDetail_doctor.safeText(
                    activity.getIntent().getStringExtra(ExaminationFormDetail_doctor.EXTRA_SYMPTOMS),
                    "Không có triệu chứng ban đầu"
            ));
        }
    }

    private void setupContinueButton(ExaminationFormDetail_doctor activity) {
        MaterialButton btnContinue = rootView.findViewById(R.id.btnContinueTab1);
        if (btnContinue == null) {
            return;
        }
        btnContinue.setOnClickListener(v -> {
            updateExaminationFormInfo(activity, true);
            activity.findViewById(R.id.tabCanLamSang).performClick();
        });
    }

    private void updateBottomButton(ExaminationFormDetail_doctor activity) {
        MaterialButton btnContinue = rootView.findViewById(R.id.btnContinueTab1);
        if (btnContinue == null) {
            return;
        }

        if (activity.isDoneLocked()) {
            btnContinue.setText("Hoàn thành");
            btnContinue.setIcon(null);
        } else {
            btnContinue.setText("Tiếp tục — Chỉ định cận lâm sàng");
            btnContinue.setIconResource(R.drawable.ic_doctor_tab_cls);
        }
    }

    private void setText(int viewId, String value, String fallback) {
        TextView textView = rootView.findViewById(viewId);
        if (textView != null) {
            textView.setText(ExaminationFormDetail_doctor.safeText(value, fallback));
        }
    }
    private void setStatusButtonsEnabled(boolean enabled) {
        if (btnWaiting != null) btnWaiting.setEnabled(enabled);
        if (btnInProgress != null) btnInProgress.setEnabled(enabled);
        if (btnDone != null) btnDone.setEnabled(enabled);
    }

    private void updateExaminationFormInfo(ExaminationFormDetail_doctor activity, boolean formCompleted) {
        setStatusButtonsEnabled(false);

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(requireContext());
        progressBar.setPadding(0, 50, 0, 50);
        androidx.appcompat.app.AlertDialog loadingDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đang xử lý...")
                .setView(progressBar)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        String currentStatus = currentExaminationStatus.getDisplayName();
        String currentSymptom = edtSymptoms.getText().toString();

        ExFormRepository.patchExaminationForm(currentFormId, currentStatus, currentSymptom).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    if (!isAdded()) return;
                    
                    setStatusButtonsEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        activity.requestListReload(formCompleted);
                    } else {
                        try {
                            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Toast.makeText(requireContext(), "Lỗi cập nhật: " + response.code() + " " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 500);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    if (!isAdded()) return;
                    
                    setStatusButtonsEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }, 500);
            }
        });
    }
}