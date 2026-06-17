package dashboard_fragment.admin_timekeeping_fix_request;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;

public class TimekeepingFixRequestAdapter extends RecyclerView.Adapter<TimekeepingFixRequestAdapter.RequestViewHolder> {
    public interface OnRequestClickListener {
        void onClick(TimekeepingFixRequestItem item);
    }

    private final List<TimekeepingFixRequestItem> items = new ArrayList<>();
    private OnRequestClickListener listener;

    public void setData(List<TimekeepingFixRequestItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void setOnRequestClickListener(OnRequestClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timekeeping_fix_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        TimekeepingFixRequestItem item = items.get(position);
        holder.tvName.setText(item.getStaffName());
        holder.tvRole.setText(item.getStaffRole());
        holder.tvReason.setText(item.getReason());
        holder.tvDate.setText(formatDate(item.getOriginalStartTime(), item.getRequestedCheckOut()));
        holder.tvTime.setText(formatTimeRange(item.getRequestedCheckIn(), item.getRequestedCheckOut()));
        holder.tvEvidenceCount.setText(item.getEvidenceUrls().size() + " ảnh minh chứng");
        holder.tvFooter.setText(getFooterText(item));
        applyStatus(holder, item.getStatus());
        loadAvatar(holder.ivAvatar, item.getStaffAvatar(), item.getStaffName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void loadAvatar(ImageView imageView, String avatarPath, String name) {
        String avatarUrl = avatarPath;
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + avatarUrl;
            }
            ImageRequest request = new ImageRequest.Builder(imageView.getContext())
                    .data(avatarUrl)
                    .target(imageView)
                    .crossfade(true)
                    .placeholder(R.drawable.dashboard_icon_avatar)
                    .error(R.drawable.dashboard_icon_avatar)
                    .build();
            Coil.imageLoader(imageView.getContext()).enqueue(request);
        } else {
            imageView.setImageResource(R.drawable.dashboard_icon_avatar);
        }
        imageView.setContentDescription(name);
    }

    private void applyStatus(RequestViewHolder holder, String status) {
        if ("Đã duyệt".equals(status)) {
            holder.tvStatus.setText("Đã duyệt");
            holder.tvStatus.setTextColor(Color.parseColor("#16884A"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_device_status_approved);
            holder.tvFooter.setTextColor(Color.parseColor("#16884A"));
        } else if ("Từ chối".equals(status)) {
            holder.tvStatus.setText("Từ chối");
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_device_status_rejected);
            holder.tvFooter.setTextColor(Color.parseColor("#C62828"));
        } else {
            holder.tvStatus.setText("Chờ duyệt");
            holder.tvStatus.setTextColor(Color.parseColor("#B56B00"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_device_status_pending);
            holder.tvFooter.setTextColor(Color.parseColor("#D98300"));
        }
    }

    private String getFooterText(TimekeepingFixRequestItem item) {
        String status = item.getStatus();
        if ("Đã duyệt".equals(status)) {
            return "Đã cập nhật ca: " + formatTimeRange(item.getRequestedCheckIn(), item.getRequestedCheckOut());
        }
        if ("Từ chối".equals(status)) {
            return "Yêu cầu đã bị từ chối";
        }
        return "Cần xem xét và phê duyệt";
    }

    public static String formatDate(String primary, String fallback) {
        Date date = parseDate(primary != null ? primary : fallback);
        if (date == null) return "--/--/----";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    public static String formatTimeRange(String start, String end) {
        return formatTime(start) + " - " + formatTime(end);
    }

    public static String formatTime(String value) {
        Date date = parseDate(value);
        if (date == null) return "--:--";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    private static Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String cleaned = value.trim().replace(" ", "T");
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.getDefault()).parse(cleaned);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvRole;
        TextView tvStatus;
        TextView tvDate;
        TextView tvTime;
        TextView tvEvidenceCount;
        TextView tvReason;
        TextView tvFooter;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivFixRequestAvatar);
            tvName = itemView.findViewById(R.id.tvFixRequestName);
            tvRole = itemView.findViewById(R.id.tvFixRequestRole);
            tvStatus = itemView.findViewById(R.id.tvFixRequestStatus);
            tvDate = itemView.findViewById(R.id.tvFixRequestDate);
            tvTime = itemView.findViewById(R.id.tvFixRequestTime);
            tvEvidenceCount = itemView.findViewById(R.id.tvFixRequestEvidenceCount);
            tvReason = itemView.findViewById(R.id.tvFixRequestReason);
            tvFooter = itemView.findViewById(R.id.tvFixRequestFooter);
        }
    }
}
