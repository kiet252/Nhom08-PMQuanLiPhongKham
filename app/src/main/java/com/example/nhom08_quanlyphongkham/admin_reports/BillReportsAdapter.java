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

public class BillReportsAdapter extends RecyclerView.Adapter<BillReportsAdapter.ViewHolder> {

    private final List<ReportsActivity_Admin.BillThongKe> danhSach;

    public BillReportsAdapter(List<ReportsActivity_Admin.BillThongKe> danhSach) {
        this.danhSach = danhSach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportsActivity_Admin.BillThongKe bill = danhSach.get(position);
        holder.tvMaBill.setText(bill.getMaBill());
        holder.tvNgayTao.setText(bill.getNgayTao());
        holder.tvTrangThai.setText(bill.getTrangThai().isEmpty() ? "--" : bill.getTrangThai());
        holder.tvTongTien.setText(formatCurrency(bill.getTongTien()));
    }

    @Override
    public int getItemCount() {
        return danhSach == null ? 0 : danhSach.size();
    }

    private String formatCurrency(long amount) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        return fmt.format(amount) + " \u0111";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBill, tvNgayTao, tvTrangThai, tvTongTien;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBill = itemView.findViewById(R.id.item_ngay);
            tvNgayTao = itemView.findViewById(R.id.item_benh_nhan);
            tvTrangThai = itemView.findViewById(R.id.item_trang_thai);
            tvTongTien = itemView.findViewById(R.id.item_tong_tien);
        }
    }
}
