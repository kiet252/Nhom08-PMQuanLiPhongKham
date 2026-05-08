package dashboard_fragment.doctor_examination_list;

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

import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;

public class DoctorExaminationGroupAdapter extends RecyclerView.Adapter<DoctorExaminationGroupAdapter.GroupViewHolder> {
    private final Context context;
    private final List<DoctorExaminationDateGroup> groups = new ArrayList<>();

    public DoctorExaminationGroupAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<DoctorExaminationDateGroup> newGroups) {
        groups.clear();
        if (newGroups != null) {
            groups.addAll(newGroups);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_examination_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        DoctorExaminationDateGroup group = groups.get(position);
        holder.tvDoctorDateGroup.setText(group.getDate());
        holder.layoutDoctorExaminationContainer.removeAllViews();

        for (ExaminationFormWithPatientDto form : group.getForms()) {
            View row = DoctorExaminationFormAdapter.createBoundRow(
                    context,
                    holder.layoutDoctorExaminationContainer,
                    form
            );
            holder.layoutDoctorExaminationContainer.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDoctorDateGroup;
        private final LinearLayout layoutDoctorExaminationContainer;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorDateGroup = itemView.findViewById(R.id.tvDoctorDateGroup);
            layoutDoctorExaminationContainer = itemView.findViewById(R.id.layoutDoctorExaminationContainer);
        }
    }
}
