package dashboard_fragment.admin_timekeeping_schedule;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.DayViewHolder> {

    public interface OnDaySelectedListener {
        void onDaySelected(int position, LocalDate date);
    }

    private final List<LocalDate> days = new ArrayList<>();
    private int selectedPosition = 0;
    private OnDaySelectedListener listener;

    public void setDays(List<LocalDate> weekDays, int selectedIndex) {
        days.clear();
        days.addAll(weekDays);
        selectedPosition = selectedIndex;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int old = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(old);
        notifyItemChanged(selectedPosition);
    }

    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_week_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        LocalDate date = days.get(position);
        boolean selected = position == selectedPosition;

        String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        holder.tvDayLabel.setText(dayLabels[position % 7]);
        holder.tvDayNumber.setText(String.format(Locale.getDefault(), "%02d", date.getDayOfMonth()));

        if (selected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_schedule_day_selected);
            holder.tvDayLabel.setTextColor(Color.WHITE);
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.dotSelected.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_schedule_day_default);
            holder.tvDayLabel.setTextColor(Color.parseColor("#64748B"));
            holder.tvDayNumber.setTextColor(Color.parseColor("#1E293B"));
            holder.dotSelected.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            setSelectedPosition(pos);
            if (listener != null) listener.onDaySelected(pos, days.get(pos));
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayLabel;
        TextView tvDayNumber;
        View dotSelected;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayLabel = itemView.findViewById(R.id.tvDayLabel);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            dotSelected = itemView.findViewById(R.id.dotDaySelected);
        }
    }
}
