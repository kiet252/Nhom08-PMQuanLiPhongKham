package dashboard_fragment.manage_examination_form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.ArrayList;
import java.util.List;

import dashboard_fragment.manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;
import dashboard_fragment.manage_examination_form.get_all_ex_form_logic.PatientBriefDto;

public class ExaminationFormGroupAdapter extends RecyclerView.Adapter<ExaminationFormGroupAdapter.GroupVH> {
    private final Context context;
    private List<ExaminationFormDateGroup> groups = new ArrayList<>();

    public ExaminationFormGroupAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<ExaminationFormDateGroup> newGroups) {
        groups = newGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_examination_group, parent, false);
        return new GroupVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVH holder, int position) {
        ExaminationFormDateGroup group = groups.get(position);
        holder.tvDateGroup.setText(group.getDate());

        holder.layoutPatientsContainer.removeAllViews();

        for (ExaminationFormWithPatientDto form : group.getForms()) {
            View row = LayoutInflater.from(context).inflate(
                    R.layout.item_examination_patient_row,
                    holder.layoutPatientsContainer,
                    false
            );

            TextView tvTime = row.findViewById(R.id.tvTime);
            TextView tvSequence = row.findViewById(R.id.tvSequenceNumber);
            TextView tvName = row.findViewById(R.id.tvPatientName);

            PatientBriefDto patient = form.getPatient();

            tvTime.setText(form.getGio_du_kien());
            tvSequence.setText(String.valueOf(form.getSo_tiep_nhan()));
            tvName.setText(patient != null ? patient.getHo_ten() : "--");

            row.setOnClickListener(v -> showExaminationFormDetails(form));

            holder.layoutPatientsContainer.addView(row);
        }
    }

    private void showExaminationFormDetails(ExaminationFormWithPatientDto form) {
        View dialogView = LayoutInflater.from(context).inflate(
                R.layout.examination_detail_dialog,
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

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        PatientBriefDto patient = form.getPatient();

        tvPatientName.setText("Họ tên: " + (patient != null ? patient.getHo_ten() : "--"));
        tvBirthday.setText("Ngày sinh: " + (patient != null && patient.getNgay_sinh() != null ? sdf.format(patient.getNgay_sinh()) : "--"));
        tvAddress.setText("Địa chỉ: " + (patient != null ? patient.getDia_chi() : "--"));

        tvExamDate.setText("Ngày khám: " + (form.getNgay_kham() != null ? sdf.format(form.getNgay_kham()) : "--"));
        tvExamTime.setText("Giờ dự kiến: " + (form.getGio_du_kien() != null ? form.getGio_du_kien() : "--"));
        tvSequence.setText("Số tiếp nhận: " + form.getSo_tiep_nhan());
        tvStatus.setText("Trạng thái: " + (form.getTrang_thai() != null ? form.getTrang_thai() : "--"));
        tvSymptoms.setText(form.getTrieu_chung_ban_dau() != null ? form.getTrieu_chung_ban_dau() : "--");

        androidx.appcompat.app.AlertDialog dialog =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                        .setView(dialogView)
                        .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
    static class GroupVH extends RecyclerView.ViewHolder {
        TextView tvDateGroup;
        LinearLayout layoutPatientsContainer;

        GroupVH(@NonNull View itemView) {
            super(itemView);
            tvDateGroup = itemView.findViewById(R.id.tvDateGroup);
            layoutPatientsContainer = itemView.findViewById(R.id.layoutPatientsContainer);
        }
    }
}
