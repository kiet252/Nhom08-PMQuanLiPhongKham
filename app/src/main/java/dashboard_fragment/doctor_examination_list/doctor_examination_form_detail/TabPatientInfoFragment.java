package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;

public class TabPatientInfoFragment extends Fragment {
    private View rootView;

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

        ExaminationFormDetail_doctor activity = (ExaminationFormDetail_doctor) requireActivity();
        bindFormData(activity);
        applyStatus(activity.getCurrentStatus());
        updateBottomButton(activity);
        setupStatusClicks(activity);
        setupContinueButton(activity);
    }

    private void setupStatusClicks(ExaminationFormDetail_doctor activity) {
        View btnWaiting = rootView.findViewById(R.id.btnStatusWaiting);
        View btnInProgress = rootView.findViewById(R.id.btnStatusInProgress);
        View btnDone = rootView.findViewById(R.id.btnStatusDone);

        btnWaiting.setOnClickListener(v -> onStatusSelected(activity, DoctorExaminationStatus.WAITING));
        btnInProgress.setOnClickListener(v -> onStatusSelected(activity, DoctorExaminationStatus.IN_PROGRESS));
        btnDone.setOnClickListener(v -> onStatusSelected(activity, DoctorExaminationStatus.DONE));
    }

    private void onStatusSelected(ExaminationFormDetail_doctor activity, DoctorExaminationStatus status) {
        activity.updateCurrentStatus(status);
        applyStatus(status);
        updateBottomButton(activity);
    }

    private void applyStatus(DoctorExaminationStatus status) {
        DoctorExaminationStatusStyle.applyStatusSectionStyle(
                rootView,
                status == null ? DoctorExaminationStatus.WAITING.getDisplayName() : status.getDisplayName()
        );
    }

    private void bindFormData(ExaminationFormDetail_doctor activity) {
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

        EditText edtSymptoms = rootView.findViewById(R.id.edtDetailSymptoms);
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
            if (activity.isDoneLocked()) {
                return;
            }
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
}
