package dashboard_fragment.admin_manage_device;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceApprovalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_REQUEST = 1;

    private final List<Row> rows = new ArrayList<>();
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(DeviceApprovalRequest request);
    }

    public void setOnRequestClickListener(OnRequestClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<DeviceApprovalRequest> requests) {
        rows.clear();
        if (requests == null) {
            notifyDataSetChanged();
            return;
        }

        String currentGroup = null;
        for (DeviceApprovalRequest request : requests) {
            if (request == null) continue;

            String group = formatGroupDate(request.getCreatedAt());
            // ĐÃ SỬA: Bảo vệ an toàn chống NullPointerException khi so sánh chuỗi
            if (group != null && !group.equals(currentGroup)) {
                currentGroup = group;
                rows.add(Row.header(group));
            }
            rows.add(Row.request(request));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_device_approval_date_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_device_approval_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvDateHeader.setText(row.header);
        } else if (holder instanceof RequestViewHolder) {
            ((RequestViewHolder) holder).bind(row.request);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static String formatGroupDate(String createdAt) {
        Date date = parseDate(createdAt);
        if (date == null) return "KHÁC";

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String requestKey = keyFormat.format(date);
        String todayKey = keyFormat.format(new Date());

        java.util.Calendar yesterday = java.util.Calendar.getInstance();
        yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1);
        String yesterdayKey = keyFormat.format(yesterday.getTime());

        if (requestKey.equals(todayKey)) return "HÔM NAY";
        if (requestKey.equals(yesterdayKey)) return "HÔM QUA";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    static String formatTime(String createdAt) {
        Date date = parseDate(createdAt);
        if (date == null) return "--:--";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    static String formatDetailDate(String createdAt) {
        Date date = parseDate(createdAt);
        if (date == null) return createdAt != null ? createdAt : "";
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    private static Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String normalized = value.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
        };

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.getDefault()).parse(normalized);
            } catch (ParseException ignored) {
            }
        }

        int plusIndex = normalized.indexOf('+');
        if (plusIndex > 0) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
                        .parse(normalized.substring(0, plusIndex));
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDeviceApprovalDateHeader);
        }
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvStaffName;
        TextView tvAndroidId;
        TextView tvStaffId;
        TextView tvTime;
        TextView tvStatus;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStaffName = itemView.findViewById(R.id.tvDeviceApprovalStaffName);
            tvAndroidId = itemView.findViewById(R.id.tvDeviceApprovalAndroidId);
            tvStaffId = itemView.findViewById(R.id.tvDeviceApprovalStaffId);
            tvTime = itemView.findViewById(R.id.tvDeviceApprovalTime);
            tvStatus = itemView.findViewById(R.id.tvDeviceApprovalStatus);
        }

        void bind(DeviceApprovalRequest request) {
            tvStaffName.setText(request.getStaffName());
            tvAndroidId.setText(request.getAndroidId());
            tvStaffId.setText("Mã NV: " + request.getStaffId());
            tvTime.setText(formatTime(request.getCreatedAt()));
            tvStatus.setText("Chờ duyệt");
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestClick(request);
                }
            });
        }
    }

    private static class Row {
        int type;
        String header;
        DeviceApprovalRequest request;

        static Row header(String header) {
            Row row = new Row();
            row.type = TYPE_HEADER;
            row.header = header;
            return row;
        }

        static Row request(DeviceApprovalRequest request) {
            Row row = new Row();
            row.type = TYPE_REQUEST;
            row.request = request;
            return row;
        }
    }
}
