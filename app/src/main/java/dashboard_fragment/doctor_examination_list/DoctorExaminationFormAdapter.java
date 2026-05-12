package dashboard_fragment.doctor_examination_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.ArrayList;
import java.util.List;

import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.PatientBriefDto;

public class DoctorExaminationFormAdapter extends RecyclerView.Adapter<DoctorExaminationFormAdapter.DoctorExaminationFormViewHolder> {
    private enum ViewType { WAITING, IN_PROGRESS, DONE }

    public interface OnFormClickListener {
        void onFormClick(ExaminationFormWithPatientDto form);
    }

    private final List<ExaminationFormWithPatientDto> forms = new ArrayList<>();
    private final OnFormClickListener onFormClickListener;

    public DoctorExaminationFormAdapter(OnFormClickListener onFormClickListener) {
        this.onFormClickListener = onFormClickListener;
    }

    public void submitList(List<ExaminationFormWithPatientDto> newForms) {
        forms.clear();
        if (newForms != null) {
            forms.addAll(newForms);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return resolveViewType(forms.get(position)).ordinal();
    }

    @NonNull
    @Override
    public DoctorExaminationFormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateRow(parent.getContext(), parent, ViewType.values()[viewType]);
        return new DoctorExaminationFormViewHolder(view, onFormClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorExaminationFormViewHolder holder, int position) {
        holder.bind(forms.get(position));
    }

    @Override
    public int getItemCount() {
        return forms.size();
    }

    public static View createBoundRow(Context context, ViewGroup parent, ExaminationFormWithPatientDto form,
                                      OnFormClickListener onFormClickListener) {
        View view = inflateRow(context, parent, resolveViewType(form));
        new DoctorExaminationFormViewHolder(view, onFormClickListener).bind(form);
        return view;
    }

    private static View inflateRow(Context context, ViewGroup parent, ViewType viewType) {
        int layoutResId;
        if (viewType == ViewType.DONE) {
            layoutResId = R.layout.doctor_item_examined_doctor_examination_form;
        } else if (viewType == ViewType.IN_PROGRESS) {
            layoutResId = R.layout.doctor_item_examining_doctor_examination_form;
        } else {
            layoutResId = R.layout.doctor_item_wait_exam_doctor_examination_form;
        }
        return LayoutInflater.from(context).inflate(layoutResId, parent, false);
    }

    private static ViewType resolveViewType(ExaminationFormWithPatientDto form) {
        String status = form.getTrang_thai();
        if ("Đã khám".equalsIgnoreCase(status)) {
            return ViewType.DONE;
        }
        if ("Đang khám".equalsIgnoreCase(status)) {
            return ViewType.IN_PROGRESS;
        }
        return ViewType.WAITING;
    }

    static class DoctorExaminationFormViewHolder extends RecyclerView.ViewHolder {
        private final OnFormClickListener onFormClickListener;
        private final TextView tvDoctorExaminationTime, tvDoctorExaminationPatientName, tvDoctorExaminationStatus, tvDoctorExaminationPatientCode, tvDoctorExaminationSymptoms;
        private final ImageView ivDoctorExaminationMore;

        DoctorExaminationFormViewHolder(@NonNull View itemView, OnFormClickListener onFormClickListener) {
            super(itemView);
            this.onFormClickListener = onFormClickListener;
            tvDoctorExaminationTime = itemView.findViewById(R.id.tvDoctorExaminationTime);
            tvDoctorExaminationPatientName = itemView.findViewById(R.id.tvDoctorExaminationPatientName);
            tvDoctorExaminationStatus = itemView.findViewById(R.id.tvDoctorExaminationStatus);
            tvDoctorExaminationPatientCode = itemView.findViewById(R.id.tvDoctorExaminationPatientCode);
            tvDoctorExaminationSymptoms = itemView.findViewById(R.id.tvDoctorExaminationSymptoms);
            ivDoctorExaminationMore = itemView.findViewById(R.id.ivDoctorExaminationMore);
        }

        void bind(ExaminationFormWithPatientDto form) {
            PatientBriefDto patient = form.getPatient();

            tvDoctorExaminationTime.setText(formatDisplayTime(form.getGio_du_kien()));
            tvDoctorExaminationPatientName.setText(patient != null
                    ? getDisplayText(patient.getHo_ten(), "--")
                    : "--");
            tvDoctorExaminationStatus.setText("• " + getDisplayText(form.getTrang_thai(), "Chờ khám"));
            tvDoctorExaminationPatientCode.setText(buildPatientCode(form, patient));
            tvDoctorExaminationSymptoms.setText(getDisplayText(
                    form.getTrieu_chung_ban_dau(),
                    "Không có triệu chứng ban đầu"
            ));

            if (ivDoctorExaminationMore != null) {
                ivDoctorExaminationMore.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(v -> {
                if (onFormClickListener != null) {
                    onFormClickListener.onFormClick(form);
                }
            });
        }

        private String buildPatientCode(ExaminationFormWithPatientDto form, PatientBriefDto patient) {
            String patientId = patient != null ? getDisplayText(patient.getId(), "--") : "--";
            return "Mã BN: " + patientId + " - Số tiếp nhận: " + form.getSo_tiep_nhan();
        }

        private String formatDisplayTime(String rawTime) {
            String displayTime = getDisplayText(rawTime, "--");
            if ("--".equals(displayTime)) {
                return displayTime;
            }

            String[] parts = displayTime.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1];
            }

            return displayTime;
        }

        private String getDisplayText(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
