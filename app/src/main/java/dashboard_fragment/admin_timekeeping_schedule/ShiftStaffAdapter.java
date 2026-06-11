package dashboard_fragment.admin_timekeeping_schedule;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShiftStaffAdapter extends RecyclerView.Adapter<ShiftStaffAdapter.StaffViewHolder> {

    public enum Mode { ASSIGNED, AVAILABLE }

    public interface OnStaffActionListener {
        void onRemove(TimekeepingItem item);
        void onAdd(StaffOption staff);
    }

    public static class StaffOption {
        private final String id;
        private final String name;
        private final String role;

        public StaffOption(String id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }

    private final List<Object> items = new ArrayList<>();
    private Mode mode = Mode.ASSIGNED;
    private OnStaffActionListener listener;

    public OnStaffActionListener getListener() {
        return listener;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setAssignedItems(List<TimekeepingItem> assigned) {
        items.clear();
        items.addAll(assigned);
        notifyDataSetChanged();
    }

    public void setAvailableStaff(List<StaffOption> staff) {
        items.clear();
        items.addAll(staff);
        notifyDataSetChanged();
    }

    public void setOnStaffActionListener(OnStaffActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shift_staff_row, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        Object item = items.get(position);

        if (item instanceof TimekeepingItem) {
            bindAssigned(holder, (TimekeepingItem) item);
        } else if (item instanceof StaffOption) {
            bindAvailable(holder, (StaffOption) item);
        }
    }

    private void bindAssigned(StaffViewHolder holder, TimekeepingItem item) {
        holder.tvName.setText(item.getStaffName());
        holder.tvRole.setText(item.getStaffRole());
        holder.tvAvatarInitials.setText(getInitials(item.getStaffName()));
        holder.btnAdd.setVisibility(View.GONE);
        holder.btnRemove.setVisibility(mode == Mode.ASSIGNED ? View.VISIBLE : View.GONE);
        applyStatus(holder, item.getStatus());

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(item);
        });
    }

    private void bindAvailable(StaffViewHolder holder, StaffOption staff) {
        holder.tvName.setText(staff.getName());
        holder.tvRole.setText(staff.getRole());
        holder.tvAvatarInitials.setText(getInitials(staff.getName()));
        holder.layoutStatus.setVisibility(View.GONE);
        holder.btnRemove.setVisibility(View.GONE);
        holder.btnAdd.setVisibility(View.VISIBLE);
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAdd(staff);
        });
    }

    private void applyStatus(StaffViewHolder holder, String status) {
        holder.layoutStatus.setVisibility(View.VISIBLE);
        if ("Hoàn thành".equals(status) || "Chưa checkout".equals(status)) {
            holder.tvStatus.setText("Chưa checkout".equals(status) ? "Đã vào ca" : "Hoàn thành");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.dotStatus.setBackgroundResource(R.drawable.bg_schedule_dot_present);
            holder.layoutStatus.setBackgroundResource(R.drawable.bg_schedule_status_present);
        } else if ("Vắng".equals(status)) {
            holder.tvStatus.setText("Vắng");
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
            holder.dotStatus.setBackgroundResource(R.drawable.bg_schedule_dot_absent);
            holder.layoutStatus.setBackgroundResource(R.drawable.bg_schedule_status_absent);
        } else {
            holder.tvStatus.setText("Chưa checkin");
            holder.tvStatus.setTextColor(Color.parseColor("#64748B"));
            holder.dotStatus.setBackgroundResource(R.drawable.bg_schedule_dot_pending);
            holder.layoutStatus.setBackgroundResource(R.drawable.bg_schedule_status_pending);
        }
    }

    static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.getDefault());
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.getDefault());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitials;
        TextView tvName;
        TextView tvRole;
        View layoutStatus;
        View dotStatus;
        TextView tvStatus;
        ImageButton btnRemove;
        MaterialButton btnAdd;

        StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitials = itemView.findViewById(R.id.tvStaffAvatarInitials);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvRole = itemView.findViewById(R.id.tvStaffRole);
            layoutStatus = itemView.findViewById(R.id.layoutStaffStatus);
            dotStatus = itemView.findViewById(R.id.dotStaffStatus);
            tvStatus = itemView.findViewById(R.id.tvStaffStatus);
            btnRemove = itemView.findViewById(R.id.btnRemoveStaff);
            btnAdd = itemView.findViewById(R.id.btnAddStaff);
        }
    }
}
