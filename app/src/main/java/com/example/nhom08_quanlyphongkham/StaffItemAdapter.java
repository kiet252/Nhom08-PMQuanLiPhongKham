package com.example.nhom08_quanlyphongkham;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffItemAdapter extends RecyclerView.Adapter<StaffItemAdapter.StaffViewHolder> {

    private List<StaffItem> staffList;
    private OnItemClickListener listener;
    public StaffItemAdapter(List<StaffItem> staffList) {
        this.staffList = staffList;
    }
    public interface OnItemClickListener
    {
        void onItemClick(StaffItem item);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @Override
    public StaffViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Nạp layout Card của bạn vào
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        StaffItem staff = staffList.get(position);

        holder.tvName.setText(staff.getName());
        holder.tvRole.setText(staff.getRole());
        holder.tvId.setText("#" + staff.getId());

        // Nếu sau này dùng Glide để load ảnh:
        // Glide.with(holder.itemView.getContext()).load(staff.getUrl()).into(holder.imgAvatar);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(staff);
            }
        });

    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    // Lớp ViewHolder để giữ các tham chiếu View
    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvId;
        ImageView imgAvatar;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvId = itemView.findViewById(R.id.tvId);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }

    public void updateList(List<StaffItem> newList) {
        this.staffList = newList;
        notifyDataSetChanged();
    }
}