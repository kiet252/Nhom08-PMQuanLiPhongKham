package com.example.nhom08_quanlyphongkham.admin_reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MonthlyReportsAdapter extends RecyclerView.Adapter<MonthlyReportsAdapter.ViewHolder> {

    private static final int TIEU_CHI_DOANH_THU = 0;
    private static final int TIEU_CHI_BENH_NHAN_MOI = 1;
    private static final int TIEU_CHI_PHIEU_KHAM = 2;

    private final List<ReportsActivity_Admin.ThongKeThang> danhSach;
    private int criterion = TIEU_CHI_DOANH_THU;

    public MonthlyReportsAdapter(List<ReportsActivity_Admin.ThongKeThang> danhSach) {
        this.danhSach = danhSach;
    }

    public void setCriterion(int criterion) {
        this.criterion = criterion;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monthly_report_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportsActivity_Admin.ThongKeThang item = danhSach.get(position);
        holder.tvThang.setText(item.getThang());
        holder.tvNam.setText(String.valueOf(item.getNam()));
        holder.tvGiaTri.setText(formatValue(item));
    }

    @Override
    public int getItemCount() {
        return danhSach == null ? 0 : danhSach.size();
    }

    private String formatValue(ReportsActivity_Admin.ThongKeThang item) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        if (criterion == TIEU_CHI_DOANH_THU) {
            return fmt.format(item.getDoanhThu()) + " đ";
        }
        if (criterion == TIEU_CHI_BENH_NHAN_MOI) {
            return fmt.format(item.getSoBenhNhanMoi());
        }
        if (criterion == TIEU_CHI_PHIEU_KHAM) {
            return fmt.format(item.getSoPhieuKham());
        }
        return "0";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvThang, tvNam, tvGiaTri;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvThang = itemView.findViewById(R.id.item_thang);
            tvNam = itemView.findViewById(R.id.item_nam);
            tvGiaTri = itemView.findViewById(R.id.item_gia_tri);
        }
    }
}
