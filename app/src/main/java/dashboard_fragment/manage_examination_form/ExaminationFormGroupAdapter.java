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

import dashboard_fragment.add_update_patient.PatientProfile;
import dashboard_fragment.create_examination_form.ExaminationForm;

public class ExaminationFormGroupAdapter extends RecyclerView.Adapter<ExaminationFormGroupAdapter.GroupVH> {
    private final Context context;
    private final String accessToken;
    private final dashboard_fragment.add_update_patient.PatientRepository patientRepository;
    private List<ExaminationFormGroup> groups = new ArrayList<>();

    public ExaminationFormGroupAdapter(Context context, String accessToken) {
        this.context = context;
        this.accessToken = accessToken;
        this.patientRepository = new dashboard_fragment.add_update_patient.PatientRepository(
                context.getString(com.example.nhom08_quanlyphongkham.R.string.abAIkey)
        );
    }

    public void submitList(List<ExaminationFormGroup> newGroups) {
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
        ExaminationFormGroup group = groups.get(position);
        holder.tvDateGroup.setText(group.getDate());

        holder.layoutPatientsContainer.removeAllViews();

        for (ExaminationForm form : group.getForms()) {
            View row = LayoutInflater.from(context).inflate(R.layout.item_examination_patient_row, holder.layoutPatientsContainer, false);

            TextView tvTime = row.findViewById(R.id.tvTime);
            TextView tvSequence = row.findViewById(R.id.tvSequenceNumber);
            TextView tvName = row.findViewById(R.id.tvPatientName);

            tvTime.setText(form.getGio_du_kien());
            tvSequence.setText(String.valueOf(form.getSo_tiep_nhan()));
            tvName.setText(form.getPatient_id());

            row.setOnClickListener(v -> loadPatientAndShowDialog(form));

            holder.layoutPatientsContainer.addView(row);
        }
    }

    private void loadPatientAndShowDialog(ExaminationForm form) {
        patientRepository.getProfileByIdOrCccd(accessToken, form.getPatient_id())
                .enqueue(new retrofit2.Callback<java.util.List<dashboard_fragment.add_update_patient.PatientProfile>>() {
                    @Override
                    public void onResponse(
                            @androidx.annotation.NonNull retrofit2.Call<java.util.List<dashboard_fragment.add_update_patient.PatientProfile>> call,
                            @androidx.annotation.NonNull retrofit2.Response<java.util.List<dashboard_fragment.add_update_patient.PatientProfile>> response
                    ) {
                        dashboard_fragment.add_update_patient.PatientProfile patient = null;
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            patient = response.body().get(0);
                        }
                        showExaminationFormDetails(form, patient);
                    }

                    @Override
                    public void onFailure(
                            @androidx.annotation.NonNull retrofit2.Call<java.util.List<dashboard_fragment.add_update_patient.PatientProfile>> call,
                            @androidx.annotation.NonNull Throwable t
                    ) {
                        showExaminationFormDetails(form, null);
                        android.widget.Toast.makeText(context, "Lỗi tải thông tin bệnh nhân: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showExaminationFormDetails(
            ExaminationForm form,
            @androidx.annotation.Nullable dashboard_fragment.add_update_patient.PatientProfile patient
    ) {
        View dialogView = LayoutInflater.from(context)
                .inflate(com.example.nhom08_quanlyphongkham.R.layout.examination_detail_dialog, null, false);

        TextView tvPatientName = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailPatientName);
        TextView tvBirthday = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailBirthday);
        TextView tvAddress = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailAddress);
        TextView tvExamDate = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailExamDate);
        TextView tvExamTime = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailExamTime);
        TextView tvSequence = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailSequence);
        TextView tvStatus = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailStatus);
        TextView tvSymptoms = dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.tvDetailSymptoms);
        com.google.android.material.button.MaterialButton btnClose =
                dialogView.findViewById(com.example.nhom08_quanlyphongkham.R.id.btnCloseDetail);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

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
