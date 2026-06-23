package dashboard_fragment.admin_manage_medicine_clinical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.text.DecimalFormat;
import java.util.List;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic.MedicineItem;

public class AdminMedicineAdapter extends RecyclerView.Adapter<AdminMedicineAdapter.MedicineViewHolder> {

    private final List<MedicineItem> medicineList;
    private OnItemClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public interface OnItemClickListener {
        void onItemClick(MedicineItem item);
    }

    public AdminMedicineAdapter(List<MedicineItem> medicineList) {
        this.medicineList = medicineList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineItem medicine = medicineList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvMedName.setText(buildMedicineName(medicine));
        holder.tvMedActive.setText(medicine.getHoat_chat() != null ? "Hoạt chất: " + medicine.getHoat_chat() : "Hoạt chất: --");
        holder.tvMedUnitTag.setText(medicine.getDon_vi() != null ? medicine.getDon_vi() : "Đơn vị");

        int stock = medicine.getTon_kho();
        holder.tvMedStockTag.setText("Tồn: " + stock);

        if (stock <= 0) {
            holder.tvMedStockTag.setText("Hết hàng");
            holder.tvMedStockTag.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.tvMedStockTag.setBackgroundResource(R.drawable.bg_badge_orange);
        } else if (stock <= 50) {
            holder.tvMedStockTag.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            holder.tvMedStockTag.setBackgroundResource(R.drawable.bg_badge_orange);
        } else {
            holder.tvMedStockTag.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            holder.tvMedStockTag.setBackgroundResource(R.drawable.bg_badge_green);
        }

        String formattedPrice = formatter.format(medicine.getDon_gia()) + "đ";
        holder.tvMedPrice.setText(formattedPrice);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(medicine);
            }
        });
    }

    private String buildMedicineName(MedicineItem medicine) {
        String name = medicine.getTen_thuoc() == null ? "" : medicine.getTen_thuoc().trim();
        String strength = medicine.getHam_luong() == null ? "" : medicine.getHam_luong().trim();

        if (name.isEmpty()) return "";

        if (strength.isEmpty()) {
            return name;
        }

        return name + " (" + strength + ")";
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedActive, tvMedUnitTag, tvMedStockTag, tvMedPrice;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedActive = itemView.findViewById(R.id.tvMedActive);
            tvMedUnitTag = itemView.findViewById(R.id.tvMedUnitTag);
            tvMedStockTag = itemView.findViewById(R.id.tvMedStockTag);
            tvMedPrice = itemView.findViewById(R.id.tvMedPrice);
        }
    }
}