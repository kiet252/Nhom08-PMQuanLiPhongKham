package doctor_patient_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.List;

public class doctor_patient_adapter extends RecyclerView.Adapter<doctor_patient_adapter.PatientViewHolder> {

    private List<doctor_patient_record> patientList;

    public doctor_patient_adapter(List<doctor_patient_record> patientList) {
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp giao diện item_patient vào RecyclerView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doctor_item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        // Lấy dữ liệu tại vị trí hiện tại
        doctor_patient_record record = patientList.get(position);

        // Đổ dữ liệu vào các TextView
        holder.tvName.setText(record.getPatientName());
        holder.tvId.setText("ID/Phone: " + record.getPatientId());
    }

    @Override
    public int getItemCount() {
        return (patientList != null) ? patientList.size() : 0;
    }

    // ViewHolder giúp giữ các View để tái sử dụng, tăng hiệu năng
    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.ptName);
            tvId = itemView.findViewById(R.id.ptPhone);
        }
    }
}