package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicineSelectAdapter extends RecyclerView.Adapter<MedicineSelectAdapter.MedicineViewHolder> {

    private final List<MedicineItem> originalItems;
    private final List<MedicineItem> displayedItems;

    public MedicineSelectAdapter(List<MedicineItem> items) {
        this.originalItems = items;
        this.displayedItems = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doctor_item_select_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineItem item = displayedItems.get(position);

        holder.tvName.setText(item.getTen_thuoc());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.tvStock.setText(item.getStockText());
        holder.tvFunction.setText(item.getFunctionText());

        updateSelectedUI(holder, item.isSelected());

        holder.itemView.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            updateSelectedUI(holder, item.isSelected());
        });
    }

    private void updateSelectedUI(MedicineViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.layoutRoot.setBackgroundResource(R.drawable.bg_medicine_select_item_selected);
            holder.imgCheck.setImageDrawable(createCheckedCircleDrawable(holder.itemView, 0xFF7A8B9D));
        } else {
            holder.layoutRoot.setBackgroundResource(R.drawable.bg_medicine_select_item);
            holder.imgCheck.setImageResource(R.drawable.bg_clinical_circle_unchecked);
        }
    }

    private Drawable createCheckedCircleDrawable(View view, int color) {
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(
                view.getContext(),
                R.drawable.bg_clinical_circle_checked
        ).mutate();

        GradientDrawable circle = (GradientDrawable) layerDrawable.getDrawable(0);
        circle.setColor(color);

        return layerDrawable;
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public List<MedicineItem> getItems() {
        return originalItems;
    }

    public void filter(String keyword) {
        displayedItems.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            displayedItems.addAll(originalItems);
        } else {
            String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);

            for (MedicineItem item : originalItems) {
                String tenThuoc = item.getTen_thuoc() == null ? "" : item.getTen_thuoc().toLowerCase(Locale.ROOT);
                String hoatChat = item.getHoat_chat() == null ? "" : item.getHoat_chat().toLowerCase(Locale.ROOT);

                if (tenThuoc.contains(normalizedKeyword) || hoatChat.contains(normalizedKeyword)) {
                    displayedItems.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutRoot;
        ImageView imgCheck;
        TextView tvName;
        TextView tvSubtitle;
        TextView tvStock;
        TextView tvFunction;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.layoutMedicineItem);
            imgCheck = itemView.findViewById(R.id.imgMedicineCheck);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvSubtitle = itemView.findViewById(R.id.tvMedicineSubtitle);
            tvStock = itemView.findViewById(R.id.tvMedicineStock);
            tvFunction = itemView.findViewById(R.id.tvMedicineFunction);
        }
    }
}
