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

import dashboard_fragment.create_examination_form.ExaminationForm;

public class ExaminationFormGroupAdapter extends RecyclerView.Adapter<ExaminationFormGroupAdapter.GroupVH> {
    private final Context context;
    private List<ExaminationFormGroup> groups = new ArrayList<>();

    public ExaminationFormGroupAdapter(Context context) {
        this.context = context;
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

            holder.layoutPatientsContainer.addView(row);
        }
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
