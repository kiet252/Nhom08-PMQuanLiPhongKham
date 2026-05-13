package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

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
        applyStatus(activity.getCurrentStatus());
        setupStatusClicks(activity);
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
    }

    private void applyStatus(DoctorExaminationStatus status) {
        DoctorExaminationStatusStyle.applyStatusSectionStyle(
                rootView,
                status == null ? DoctorExaminationStatus.WAITING.getDisplayName() : status.getDisplayName()
        );
    }
}
