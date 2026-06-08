package dashboard_fragment.admin_manage_medicine_clinical;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;

public class AdminClinicalAdapter extends RecyclerView.Adapter<AdminClinicalAdapter.ClinicalViewHolder> {

    private final List<ClinicalItem> clinicalList;
    private OnItemClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public interface OnItemClickListener {
        void onItemClick(ClinicalItem item);
    }

    public AdminClinicalAdapter(List<ClinicalItem> clinicalList) {
        this.clinicalList = clinicalList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClinicalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_clinical, parent, false);
        return new ClinicalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicalViewHolder holder, int position) {
        ClinicalItem item = clinicalList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvClinicalName.setText(item.getTen_dich_vu() != null ? item.getTen_dich_vu() : "");
        holder.tvClinicalType.setText(item.getLoai_dich_vu() != null ? "Loại: " + item.getLoai_dich_vu() : "Loại: --");
        holder.tvClinicalDesc.setText(item.getMo_ta() != null ? item.getMo_ta() : "Không có mô tả");

        // Format price
        double price = item.getDon_gia() != null ? item.getDon_gia() : 0.0;
        String formattedPrice = formatter.format(price) + "đ";
        holder.tvClinicalPrice.setText(formattedPrice);

        // Dynamically style icon and background based on service type
        String typeNorm = normalizeString(item.getLoai_dich_vu());

        if (typeNorm.contains("xet nghiem")) {
            holder.ivClinicalIcon.setImageResource(R.drawable.ic_cls_blood);
            holder.ivClinicalIcon.setColorFilter(Color.parseColor("#E53935"));
            holder.cvIconContainer.setCardBackgroundColor(Color.parseColor("#FCE8E6"));
        } else if (typeNorm.contains("sieu am")) {
            holder.ivClinicalIcon.setImageResource(R.drawable.ic_cls_ultrasound);
            holder.ivClinicalIcon.setColorFilter(Color.parseColor("#00897B"));
            holder.cvIconContainer.setCardBackgroundColor(Color.parseColor("#E4F7F6"));
        } else if (typeNorm.contains("x quang") || typeNorm.contains("hinh anh") || typeNorm.contains("chup")) {
            holder.ivClinicalIcon.setImageResource(R.drawable.ic_cls_imaging);
            holder.ivClinicalIcon.setColorFilter(Color.parseColor("#1E88E5"));
            holder.cvIconContainer.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        } else if (typeNorm.contains("noi soi")) {
            holder.ivClinicalIcon.setImageResource(R.drawable.ic_cls_endoscope);
            holder.ivClinicalIcon.setColorFilter(Color.parseColor("#8E24AA"));
            holder.cvIconContainer.setCardBackgroundColor(Color.parseColor("#F3E5F5"));
        } else {
            holder.ivClinicalIcon.setImageResource(R.drawable.ic_stethoscope_vector);
            holder.ivClinicalIcon.setColorFilter(Color.parseColor("#0FAFBF"));
            holder.cvIconContainer.setCardBackgroundColor(Color.parseColor("#E6F7F8"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    private String normalizeString(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
    }

    @Override
    public int getItemCount() {
        return clinicalList.size();
    }

    static class ClinicalViewHolder extends RecyclerView.ViewHolder {
        TextView tvClinicalName, tvClinicalType, tvClinicalDesc, tvClinicalPrice;
        ImageView ivClinicalIcon;
        MaterialCardView cvIconContainer;

        public ClinicalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClinicalName = itemView.findViewById(R.id.tvClinicalName);
            tvClinicalType = itemView.findViewById(R.id.tvClinicalType);
            tvClinicalDesc = itemView.findViewById(R.id.tvClinicalDesc);
            tvClinicalPrice = itemView.findViewById(R.id.tvClinicalPrice);
            ivClinicalIcon = itemView.findViewById(R.id.ivClinicalIcon);
            cvIconContainer = itemView.findViewById(R.id.cvClinicalIconContainer);
        }
    }
}
