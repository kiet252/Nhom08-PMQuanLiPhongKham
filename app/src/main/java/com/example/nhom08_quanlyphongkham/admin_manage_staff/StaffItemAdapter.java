package com.example.nhom08_quanlyphongkham.admin_manage_staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

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
                .inflate(R.layout.admin_item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        StaffItem staff = staffList.get(position);

        holder.tvName.setText(staff.getName());
        holder.tvRole.setText(staff.getRole());
        holder.tvId.setText("#" + staff.getId());

        loadAvatar(holder.imgAvatar, staff.getAvatar());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(staff);
            }
        });
    }


    private void loadAvatar(ImageView imageView, String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        String fullUrl = avatarPath;
        if (!avatarPath.startsWith("http")) {
            fullUrl = "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + avatarPath;
        }

        ImageRequest request = new ImageRequest.Builder(imageView.getContext())
                .data(fullUrl)
                .target(imageView)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .build();

        Coil.imageLoader(imageView.getContext()).enqueue(request);
    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvId;
        ImageView imgAvatar;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvId = itemView.findViewById(R.id.tvId);
            imgAvatar = itemView.findViewById(R.id.staffImgAvatar);
        }
    }

    public void updateList(List<StaffItem> newList) {
        this.staffList = newList;
        notifyDataSetChanged();
    }
}
